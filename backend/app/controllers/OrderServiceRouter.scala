package controllers



import akka.Done
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.dispatch.Futures
import grpcOrder._
import account._
import grpcOrder.AbstractOrderServiceRouter
import akka.grpc.GrpcClientSettings
import javax.inject.Inject
import javax.inject.Singleton
import scala.concurrent.Future
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement

/** GRPC Service */
@Singleton
class OrderServiceRouter @Inject() (
    implicit val actorSystem: ActorSystem,
     configuration: play.api.Configuration,
) extends AbstractOrderServiceRouter(actorSystem) {

  val dbuser = configuration.underlying.getString("myPOSTGRES_USER")
  val dbpw = configuration.underlying.getString("myPOSTGRES_PASSWORD")
  val url = configuration.underlying.getString("myPOSTGRES_DB")
  val dbURL = s"jdbc:postgresql://localhost:5432/$url"

  //val dbuser="postgres"
  //val dbpw ="postgres"
  //val url="smartmarkt"
  //val dbURL = "jdbc:postgresql://database:5432/smartmarkt"

  val dbc = new DBController(dbuser, dbpw, dbURL)

  override def makeOrder(in: OrderInformation): Future[OrderID] = {
    val marktIBAN = "DE 23 1520 0000 7845 2945 55"
    val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)

    val userID = in.userID
    val articleID = in.articleID
    val number = in.howMany

    //summe berechnen
    var priceRes = dbc.executeSQL(s"SELECT price FROM article WHERE id=$articleID")
    priceRes.next()
    var price =
      priceRes.getFloat("price") * number
    var summe = price

    //Bank verbindung einfügen
      implicit val mat = ActorMaterializer()
      implicit val ec = actorSystem.dispatcher
      val bank = AccountServiceClient(GrpcClientSettings.fromConfig("account.AccountService"))
      val ibanRequest = bank.getIban(User_Id(userID))
      var iban = "EMPTY"
      var orderID = -1
 
      ibanRequest.map(res => {
        iban = res.getFieldByNumber(2).toString
      })

      Thread.sleep(1000)
      if (!iban.equals("EMPTY")) {
        var transfer =
          Transfer(userID, iban, "Smartmarkt", marktIBAN, summe.toString)
        val transferrequest = bank.transfer(transfer)
        var transferresult = "ERROR"
        transferrequest.map(res => {
          transferresult = res.getFieldByNumber(1).toString
        })
        Thread.sleep(1000)
        if (transferresult.equals("200")) {


    orderID = dbc.executeUpdate(s"INSERT INTO markt_order (userID)  VALUES ('$userID')").toInt
    dbc.executeUpdate(
      s"INSERT INTO order_article (articleID,orderID,number) VALUES($articleID,$orderID,$number)"
    )
    val currentStock =
      dbc.executeSQL(s"SELECT stock FROM article WHERE id=$articleID")
    currentStock.next()
    var stock = currentStock.getInt("stock")
    stock = stock - number.toString.toInt
    dbc.executeUpdate(s"UPDATE article SET stock=$stock WHERE id=$articleID")

    }}
    Future.successful(OrderID(orderID.toInt))
  }
  override def trackOrder(in: OrderID): Future[OrderState] = {
    val id = in.orderID
    var resultSet =
      dbc.executeSQL(s"SELECT status FROM markt_order WHERE id = $id")
    resultSet.next()
    Future.successful(OrderState(resultSet.getString("status")))
  }
}
