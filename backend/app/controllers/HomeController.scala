package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Failure
import scala.util.Success
import akka.Done
import akka.NotUsed
import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import user._
import akka.dispatch.Futures
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject() (
    configuration: play.api.Configuration,
    val controllerComponents: ControllerComponents
) extends BaseController {

  val dbuser = configuration.underlying.getString("myPOSTGRES_USER")
  val dbpw = configuration.underlying.getString("myPOSTGRES_PASSWORD")
  val url = configuration.underlying.getString("myPOSTGRES_DB")
  //val dbURL = "jdbc:postgresql://database:5432/smartmarkt"
  val dbURL = s"jdbc:postgresql://localhost:5432/$url"
  var res = "START"

  val dbc = new DBController(dbuser, dbpw, dbURL)

  //nicht ausgelager, da 1. route 2. auf admin ebene
  def createDB = Action { _ =>
    try {
      val rootConnection = DriverManager
        .getConnection("jdbc:postgresql://database:5432", dbuser, dbpw)
      var rootStatement = rootConnection.createStatement()
      var sql =
        s"SELECT 'CREATE DATABASE $url' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$url');"
      val affectedRows = rootStatement.executeUpdate(sql)
      // 0 affected Rows -> DB already exists
      if (affectedRows != 0) {
        sql = "CREATE DATABASE smartmarkt;"
        rootStatement.executeUpdate(sql)
      }
      rootConnection.close()

    } catch {
      case e: Exception => Ok("Create Database failed: " + e.toString())
    }
    try {
      dbc.dropDB();
      dbc.setupDB();
      dbc.fillDB()
      Ok("DB CREATED")
    } catch {
      case e: Exception => Ok(e.toString())
    }
  }

  def login(token: String) =
    Action //.async
    { _ =>
      verifyUser(token)
      // val uid = await(verifyUser(token))
      // val user=await(getUser(uid))
      //  Ok(user)
      Ok(res)
    }

  def verifyUser(token: String): String = {
    implicit val sys = ActorSystem("SmartMarkt")
    implicit val mat = ActorMaterializer()
    implicit val ec = sys.dispatcher
    val client =
      UserServiceClient(GrpcClientSettings.fromConfig("user.UserService"))
    val reply = client.verifyUser(UserToken(token))
    reply.onComplete {
      case Success(msg) =>
        res = "MSG: " + msg + "Result: " + msg.getFieldByNumber(1)
      case Failure(exception) => res = exception.toString()
      case _                  => res = "Unknown ERROR on verifyUser"
    }

    return "test"
  }

  def getAllCategorys = Action { _ =>
    val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
    var statement = connection.createStatement()
    var resultSet = statement.executeQuery("SELECT c_name FROM category")
    var categorys = new JsArray()
    while (resultSet.next()) {
      var category = Json.obj("name" -> resultSet.getString("c_name"))
      categorys = categorys.append(category)
    }
    connection.close()
    Ok(Json.toJson(categorys))
  }

  def getAllArticle = Action { _ =>
    val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
    var statement = connection.createStatement()
    var resultSet = statement.executeQuery("SELECT * FROM Article")
    var articleList = new JsArray()
    while (resultSet.next()) {
      var article = Json.obj(
        "id" -> resultSet.getInt("id"),
        "manufacture" -> resultSet.getString("manufacture"),
        "name" -> resultSet.getString("name"),
        "description" -> resultSet.getString("description"),
        "price" -> resultSet.getFloat("price"),
        "stock" -> resultSet.getInt("stock")
      )
      articleList = articleList.append(article)
    }
    connection.close()
    Ok(articleList)
  }

  def getArticle(category: String, name: String) = Action { _ =>
    val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
    var statement = connection.createStatement()
    var resultSet: ResultSet = null
    //Nur Kategorie angegeben
    if (!category.equals("_") && name.equals("_")) {
      resultSet = statement.executeQuery(
        s"SELECT * FROM article INNER JOIN (category INNER JOIN article_category ON article_category.categoryID = category.id) ON article.id=article_category.articleID WHERE c_name='$category'"
      )
    }
    // Nur Name angegeben
    else if (category.equals("_") && !name.equals("_")) {
      resultSet = statement.executeQuery(
        s"SELECT * FROM article WHERE name LIKE'$name%%'"
      )
    }
    //Beides angegeben
    else if (!category.equals("_") && !name.equals("_")) {
      resultSet = statement.executeQuery(
        s"SELECT * FROM article INNER JOIN (category INNER JOIN article_category ON article_category.categoryID = category.id) ON article.id=article_category.articleID WHERE c_name='$category' AND name LIKE '$name%%'"
      )
    }
    //Nichts angegeben -> All Article
    else {
      resultSet = statement.executeQuery("SELECT * FROM article")
    }

    var articleList = new JsArray()
    while (resultSet.next()) {
      var article = Json.obj(
        "id" -> resultSet.getInt("id"),
        "manufacture" -> resultSet.getString("manufacture"),
        "name" -> resultSet.getString("name"),
        "description" -> resultSet.getString("description"),
        "price" -> resultSet.getFloat("price"),
        "stock" -> resultSet.getInt("stock")
      )
      articleList = articleList.append(article)
    }
    connection.close()
    Ok(articleList)
  }

  def getArticleComments(id: Int) = Action { _ =>
    val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
    var statement = connection.createStatement()
    var resultSet =
      statement.executeQuery(s"SELECT * FROM rating WHERE articleID = $id")
    var comments = new JsArray()
    while (resultSet.next()) {
      var comment = Json.obj(
        "id" -> resultSet.getInt("id"),
        "text" -> resultSet.getString("text"),
        "rating" -> resultSet.getString("rating"),
        "userID" -> resultSet.getString("userID"),
        "articleID" -> resultSet.getInt("articleID")
      )
      comments = comments.append(comment)
    }
    connection.close()
    Ok(comments)
  }

  def getCustomerByID(id: String) = Action { _ =>
    val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
    var statement = connection.createStatement()

    var resultSet =
      statement.executeQuery(s"SELECT * FROM markt_user WHERE id = '$id'")
    var user = Json.obj()
    if (resultSet.next()) {
      user = Json.obj(
        "id" -> resultSet.getString("id"),
        "points" -> resultSet.getInt("points"),
        "isWorker" -> resultSet.getString("isWorker"),
        "name" -> "Beispiel Nutzer",
        "adress" -> "PLZ123 Beispielweg 22"
      )
    }
    //neue User kriegen 500 Startpunkte
    else {
      var statement2 = connection.prepareStatement(
        s"INSERT INTO markt_user (id,points,isWorker) VALUES ($id,500,false)"
      )
      statement2.executeUpdate()
      user = Json.obj(
        "points" -> 500,
        "isWorker" -> false,
        "id" -> id
      )
    }
    connection.close()
    Ok(new JsArray().append(user))
  }

  def getAllOrder = Action { _ =>
    val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
    var statement = connection.createStatement()
    var getOrder = statement.executeQuery("SELECT * FROM markt_order")
    var orderList = new JsArray()
    while (getOrder.next()) {
      var statement2 = connection.createStatement()
      var getArticle = statement2.executeQuery(
        "SELECT * FROM  article INNER JOIN order_article ON article.id = order_article.articleID WHERE order_article.orderID=" + getOrder
          .getInt(
            "id"
          )
      )
      var articleInOrder = new JsArray()
      while (getArticle.next()) {
        var article = Json.obj(
          "id" -> getArticle.getInt("id"),
          "manufacture" -> getArticle.getString("manufacture"),
          "name" -> getArticle.getString("name"),
          "description" -> getArticle.getString("description"),
          "price" -> getArticle.getFloat("price"),
          "stock" -> getArticle.getInt("stock"),
          "number" -> getArticle.getInt("number")
        )
        articleInOrder = articleInOrder.append(article)

      }
      var order = Json.obj(
        "id" -> getOrder.getInt("id"),
        "userID" -> getOrder.getString("userID"),
        "state" -> getOrder.getString("state"),
        "date" -> getOrder.getString("date"),
        "article" -> articleInOrder
      )
      orderList = orderList.append(order)
    }
    connection.close()
    Ok(orderList)

  }

  def getOrderByCustomerID(cid: String) = Action { _ =>
    val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
    val statement = connection.createStatement()
    val getOrder =
      statement.executeQuery(s"SELECT * FROM markt_order WHERE userID='$cid'")
    var orderList = new JsArray()
    while (getOrder.next()) {
      val statement2 = connection.createStatement()
      val getArticle = statement2.executeQuery(
        "SELECT * FROM  article INNER JOIN order_article ON article.id = order_article.articleID WHERE order_article.orderID=" + getOrder
          .getInt("id")
      )
      var articleList = new JsArray()
      while (getArticle.next()) {
        val article = Json.obj(
          "id" -> getArticle.getInt("id"),
          "manufacture" -> getArticle.getString("manufacture"),
          "name" -> getArticle.getString("name"),
          "description" -> getArticle.getString("description"),
          "price" -> getArticle.getFloat("price"),
          "stock" -> getArticle.getInt("stock"),
          "number" -> getArticle.getInt("number")
        )
        articleList = articleList.append(article)

      }
      var order = Json.obj(
        "id" -> getOrder.getInt("id"),
        "userID" -> getOrder.getString("userID"),
        "state" -> getOrder.getString("state"),
        "date" -> getOrder.getString("date"),
        "article" -> articleList
      )
      orderList = orderList.append(order)
    }
    connection.close()
    Ok(orderList)
  }

  def newOrder = Action(parse.json) { implicit request =>
    var connection: Connection = null
    try {
      val order = Json.toJson(request.body)
      val userID = order("userID")
      val summe = order("summe") //später für bank
      val article = order("article").as[JsArray]
      connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
      var sql = s"INSERT INTO markt_order (userID)  VALUES ($userID)"
      var statement =
        connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
      statement.executeUpdate()
      val generatedKey = statement.getGeneratedKeys()
      generatedKey.next()
      val orderID = generatedKey.getLong(1)

      for (i <- 0 to article.value.size - 1) {
        val articleID = article.apply(i)("id")
        val number = article.apply(i)("number")
        sql =
          s"INSERT INTO order_article (articleID,orderID,number) VALUES($articleID,$orderID,$number)"
        statement = connection.prepareStatement(sql)
        statement.executeUpdate()

        sql = s"SELECT stock FROM article WHERE id=$articleID"
        val statement2 = connection.createStatement()
        val currentStock = statement2.executeQuery(sql)
        currentStock.next()
        var stock = currentStock.getInt("stock")
        stock = stock - number.toString.toInt
        sql = s"UPDATE article SET stock=$stock WHERE id=$articleID"

      }
      Ok(Json.toJson(orderID))
    } catch {
      case e: Exception => Ok("ERROR")
    } finally {
      connection.close()
    }

  }

  def newComment = Action(parse.json) { implicit request =>
    var connection: Connection = null
    try {
      val comment = Json.toJson(request.body)
      val text = comment("text").toString().replace('\"', '\'')
      val rating = comment("rating")
      val userID = comment("userID")
      val articleID = comment("articleID")

      connection = DriverManager.getConnection(dbURL, dbuser, dbpw)

      val sql =
        s"INSERT INTO rating (text,rating,userID,articleID) VALUES ($text,$rating,$userID,$articleID)"
      val statement =
        connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)

      val affectedRows = statement.executeUpdate()
      if (affectedRows == 0)
        Ok("ERROR")
      else {
        val generatedKey = statement.getGeneratedKeys()
        generatedKey.next()
        Ok(Json.toJson(generatedKey.getLong(1)))
      }

    } catch {
      case e: Exception => Ok("ERROR")
    } finally {
      connection.close()
    }

  }

  def newArticle = Action(parse.json) { implicit request =>
    var connection: Connection = null
    try {
      val article = Json.toJson(request.body)
      val manufacture = article("manufacture").toString().replace('\"', '\'')
      val name = article("name").toString().replace('\"', '\'')
      val description = article("description").toString().replace('\"', '\'')
      val price = article("price")
      val cat = article("cat")
      connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
      val sql =
        s"INSERT INTO article (manufacture,name,description,price,stock) VALUES ($manufacture,$name,$description,$price,0)"
      val statement =
        connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
      val affectedRows = statement.executeUpdate()

      if (affectedRows == 0)
        Ok("ERROR")
      else {
        val generatedKey = statement.getGeneratedKeys()
        generatedKey.next()
        Ok(Json.toJson(generatedKey.getLong(1)))
      }

    } catch {
      case e: Exception => Ok("ERROR")
    } finally {
      connection.close()
    }

  }

  def updateOrder = Action(parse.json) { implicit request =>
    var connection: Connection = null
    try {
      val order = Json.toJson(request.body)
      val state = order("state").toString().replace('\"', '\'')
      val id = order("id")

      connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
      val sql = s"UPDATE markt_order SET state=$state WHERE id=$id"
      val statement =
        connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
      val affectedRows =
        statement.executeUpdate(
          )

      if (affectedRows == 0)
        Ok("ERROR")
      else {
        val generatedKey = statement.getGeneratedKeys()
        generatedKey.next()
        Ok(Json.toJson(generatedKey.getLong(1)))
      }

    } catch {
      case e: Exception => Ok("ERROR")
    } finally {
      connection.close()
    }
  }

  def alterArticle = Action(parse.json) { implicit request =>
    var connection: Connection = null
    try {
      val article = Json.toJson(request.body)
      val id = article("id")
      val price = article("price")
      val manufacture = article("manufacture").toString().replace('\"', '\'')
      val name = article("name").toString().replace('\"', '\'')
      val description = article("description").toString().replace('\"', '\'')
      val stock = article("stock")

      connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
      val sql =
        s"UPDATE article SET price=$price, name=$name, manufacture=$manufacture, description=$description, stock=$stock  WHERE id=$id"
      val statement =
        connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
      val affectedRows = statement.executeUpdate()
      if (affectedRows == 0)
        Ok("ERROR")
      else {
        val generatedKey = statement.getGeneratedKeys()
        generatedKey.next()
        Ok(Json.toJson(generatedKey.getLong(1)))
      }
    } catch {
      case e: Exception => Ok("ERROR")
    } finally {
      connection.close()
    }
  }

  def alterUser = Action(parse.json) { implicit request =>
    var connection: Connection = null
    try {
      val user = Json.toJson(request.body)
      val id = user("id")
      val isWorker = user("isWorker")
      val points = user("points")

      connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
      val sql =
        s"UPDATE markt_user SET points=$points, isWorker=$isWorker  WHERE id=$id"
      val statement =
        connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
      val affectedRows = statement.executeUpdate()
      if (affectedRows == 0)
        Ok("ERROR")
      else {
        val generatedKey = statement.getGeneratedKeys()
        generatedKey.next()
        Ok(Json.toJson(generatedKey.getLong(1)))
      }
    } catch {
      case e: Exception => Ok("ERROR")
    } finally {
      connection.close()
    }
  }

}
