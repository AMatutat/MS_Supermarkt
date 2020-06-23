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

/** User implementation, with support for dependency injection etc */
@Singleton
class OrderServiceRouter @Inject() (implicit actorSystem: ActorSystem)
    extends AbstractOrderServiceRouter(actorSystem) {

  val dbuser = "postgres"
  val dbpw = "postgres"
  val dbURL = "jdbc:postgresql://localhost:5432/smartmarkt"

  override def makeOrder(in: OrderInformation): Future[OrderID] = {
    val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)

    val userID = in.userID
    val articleID = in.articleID
    val number = in.howMany

    var sql = s"INSERT INTO markt_order (userID)  VALUES ($userID)"
    var statement =
      connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
    statement.executeUpdate()
    val generatedKey = statement.getGeneratedKeys()
    generatedKey.next()
    val orderID = generatedKey.getLong(1)
    sql =
      s"INSERT INTO order_article (articleID,orderID,number) VALUES($articleID,$orderID,$number)"
    statement = connection.prepareStatement(sql)
    statement.executeUpdate()
    sql = s"SELECT stock FROM article WHERE id=$articleID"
    val statement2 = connection.createStatement()
    val currentStock = statement2.executeQuery(sql)
    currentStock.next()
    var stock = currentStock.getInt("stock")
    stock = stock - number
    sql = s"UPDATE article SET stock=$stock WHERE id=$articleID"
    Future.successful(OrderID(orderID.toInt))
  }

  override def trackOrder(in: OrderID): Future[OrderState] = {
    val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
    var statement = connection.createStatement()
    val id = in.orderID
    var resultSet =
      statement.executeQuery(s"SELECT status FROM markt_order WHERE id = $id")
    resultSet.next()
    Future.successful(OrderState(resultSet.getString("status")))
  }
}
