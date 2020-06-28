package controllers
import akka.actor.ActorSystem
import akka.stream.Materializer
import grpcOrder._
import grpcOrder.AbstractOrderServiceRouter
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
    implicit actorSystem: ActorSystem,
    configuration: play.api.Configuration
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
    val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)

    val userID = in.userID
    val articleID = in.articleID
    val number = in.howMany

    val orderID =
      dbc.executeUpdate(s"INSERT INTO markt_order (userID)  VALUES ('$userID')")
    dbc.executeUpdate(
      s"INSERT INTO order_article (articleID,orderID,number) VALUES($articleID,$orderID,$number)"
    )
    val currentStock =
      dbc.executeSQL(s"SELECT stock FROM article WHERE id=$articleID")
    currentStock.next()
    var stock = currentStock.getInt("stock")
    stock = stock - number.toString.toInt
    dbc.executeUpdate(s"UPDATE article SET stock=$stock WHERE id=$articleID")

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
