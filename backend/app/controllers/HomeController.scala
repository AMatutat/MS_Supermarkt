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

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject() (val controllerComponents: ControllerComponents)
    extends BaseController {

  val dbuser = "postgres"
  val dbpw = "postgres"
  val dbURL = "jdbc:postgresql://database:5432/smartmarkt2"

  val tmpArticle = Json.obj(
    "id" -> 1,
    "manufacutre" -> "LeckerSchmecker",
    "name" -> "Bratwurst",
    "decription" -> "100% reinste Bratwurst perfekt auf den Grill oder in der Pfane.",
    "price" -> 1.5f,
    "picture" -> "tbd",
    "stock" -> 55
  )

  def login(name: String, pw: String) = Action { _ => Ok(tmpArticle) }

  def getAllCategorys = Action { _ =>
    val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
    var statement = connection.createStatement()
    var resultSet = statement.executeQuery("SELECT c_name FROM category")
    var js = new JsArray()
    while (resultSet.next()) {
      var r = Json.obj("name" -> resultSet.getString("c_name"))
      js = js.append(r)
    }
    Ok(Json.toJson(js))
  }

  def getAllArticle = Action { _ =>
    val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
    var statement = connection.createStatement()
    var resultSet = statement.executeQuery("SELECT * FROM Article")
    var js = new JsArray()
    while (resultSet.next()) {
      var r = Json.obj(
        "id" -> resultSet.getInt("id"),
        "manufacutre" -> resultSet.getString("manufacture"),
        "name" -> resultSet.getString("name"),
        "decription" -> resultSet.getString("description"),
        "price" -> resultSet.getFloat("price"),
        "stock" -> resultSet.getInt("stock")
      )
      js = js.append(r)
    }
    Ok(Json.toJson(js))
  }

  def getArticleByID(id: Int) = Action { _ =>
    val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
    var statement = connection.createStatement()
    var resultSet =
      statement.executeQuery(s"SELECT * FROM Article WHERE id = $id")
    var js = new JsArray()
    while (resultSet.next()) {
      var r = Json.obj(
        "id" -> resultSet.getInt("id"),
        "manufacutre" -> resultSet.getString("manufacture"),
        "name" -> resultSet.getString("name"),
        "decription" -> resultSet.getString("description"),
        "price" -> resultSet.getFloat("price"),
        "stock" -> resultSet.getInt("stock")
      )
      js = js.append(r)
    }
    Ok(Json.toJson(js))
  }

  def getArticle(category: String, name: String) = Action { _ =>
    val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
    var statement = connection.createStatement()
    //JOINTS
    var resultSet = statement.executeQuery("SELECT * FROM Article WHERE ")
    var js = new JsArray()
    while (resultSet.next()) {
      var r = Json.obj(
        "id" -> resultSet.getInt("id"),
        "manufacutre" -> resultSet.getString("manufacture"),
        "name" -> resultSet.getString("name"),
        "decription" -> resultSet.getString("description"),
        "price" -> resultSet.getFloat("price"),
        "stock" -> resultSet.getInt("stock")
      )
      //Gutes design ist, wenn dein Array ein neues Array return wenn du etwas hinzufügst ..... SCALA
      js = js.append(r)
    }
    Ok(Json.toJson(js))
  }

  def getArticleComments(id: Int) = Action { _ =>
    val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
    var statement = connection.createStatement()
    var resultSet =
      statement.executeQuery(s"SELECT * FROM rating WHERE articleID = $id")
    var js = new JsArray()
    while (resultSet.next()) {
      var r = Json.obj(
        "id" -> resultSet.getInt("id"),
        "text" -> resultSet.getString("text"),
        "rating" -> resultSet.getString("rating"),
        "userID" -> resultSet.getInt("userID"),
        "articleID" -> resultSet.getInt("articleID")
      )
      js = js.append(r)
    }
    Ok(Json.toJson(js))

  }

  def getCustomerByID(id: Int) = Action { _ =>
    val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
    var statement = connection.createStatement()
    var resultSet =
      statement.executeQuery(s"SELECT * FROM markt_user WHERE id = $id")
    var js = new JsArray()
    while (resultSet.next()) {
      var r = Json.obj(
        "id" -> resultSet.getInt("id"),
        "points" -> resultSet.getInt("points"),
        "isWorker" -> resultSet.getInt("isWorker")
      )
      js = js.append(r)
    }
    Ok(Json.toJson(js))
  }

  def getAllOrder = Action { _ =>
    val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
    var statement = connection.createStatement()
    var getOrder = statement.executeQuery("SELECT * FROM markt_order")
    var js = new JsArray()
    while (getOrder.next()) {
      var statement2 = connection.createStatement()
      var getArticle = statement2.executeQuery(
        "SELECT * FROM  article INNER JOIN order_article ON article.id = order_article.articleID WHERE order_article.orderID=" + getOrder
          .getInt(
            "id"
          )
      )
      var js2 = new JsArray()
      while (getArticle.next()) {
        var a = Json.obj(
          "id" -> getArticle.getInt("id"),
          "manufacutre" -> getArticle.getString("manufacture"),
          "name" -> getArticle.getString("name"),
          "decription" -> getArticle.getString("description"),
          "price" -> getArticle.getFloat("price"),
          "stock" -> getArticle.getInt("stock"),
          "number" -> getArticle.getInt("number")
        )
        js2 = js2.append(a)

      }
      var r = Json.obj(
        "id" -> getOrder.getInt("id"),
        "userID" -> getOrder.getInt("userID"),
        "state" -> getOrder.getString("state"),
        "date" -> getOrder.getString("date"),
        "article" -> js2
      )
      js = js.append(r)
    }
    Ok(Json.toJson(js))

  }

  def getOrderByID(id: Int) = Action { _ => Ok(tmpArticle) }

  def getOrderByCustomerID(cid: Int) = Action { _ => Ok(tmpArticle) }

  def newOrder = Action(parse.json) { implicit request =>
    Ok(Json.toJson(request.body))
  }

  def newComment = Action(parse.json) { implicit request =>
    Ok(Json.toJson(request.body))
  }

  def newArticle = Action(parse.json) { implicit request =>
    Ok(Json.toJson(request.body))
  }

  def updateOrder = Action(parse.json) { implicit request =>
    Ok(Json.toJson(request.body))
  }

  def fillStock = Action(parse.json) { implicit request =>
    Ok(Json.toJson(request.body))
  }

  def updatePrice = Action(parse.json) { implicit request =>
    Ok(Json.toJson(request.body))
  }
}
