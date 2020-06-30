package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._
import java.sql.ResultSet
import java.sql.SQLTimeoutException
import java.sql.SQLException
import buerger._
import account._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.util.Failure
import scala.util.Success

import akka.Done
import akka.NotUsed
import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.dispatch.Futures

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
  val dbURL = s"jdbc:postgresql://localhost:5432/$url"

  val marktIBAN = "DE 23 1520 0000 2404 6596 49"

  //val dbuser="postgres"
  //val dbpw ="postgres"
  //val url="smartmarkt"
  //val dbURL = "jdbc:postgresql://database:5432/smartmarkt"

  //Database Controller
  val dbc = new DBController(dbuser, dbpw, dbURL)
  var res = "START"

  /**
    * Löscht bestehende Datenbank und initialisiert eine neue DB.
    */
  def createDB = Action { _ =>
    Ok(dbc.createDB("jdbc:postgresql://localhost:5432/", url))
  //Ok(dbc.createDB("jdbc:postgresql://database:5432/", url))

  }

  def login(token: String) =
    Action.async { _ =>
      implicit val sys = ActorSystem("SmartMarkt")
      implicit val mat = ActorMaterializer()
      implicit val ec = sys.dispatcher

      val client =
        UserServiceClient(GrpcClientSettings.fromConfig("user.UserService"))
      val userrequest: Future[buerger.UserId] = client.verifyUser(UserToken(token))
      userrequest.map(msg =>
        if (msg.getFieldByNumber(1) == null) Ok("Login Failed")
        else Ok(getUserByID(msg.getFieldByNumber(1).toString()))
      )
    }

  def getUserByID(id: String): JsObject = {
    implicit val sys = ActorSystem("SmartMarkt")
    implicit val mat = ActorMaterializer()
    implicit val ec = sys.dispatcher
    val client = UserServiceClient(
      GrpcClientSettings.fromConfig("user.UserService")
    )
  
    val grpcuser = client.getUser(buerger.UserId(id))
    var adress = ""
    var name = ""
    grpcuser.map(res => {
      adress =
        res.getFieldByNumber(9) + " " + res.getFieldByNumber(10) + " " + res
          .getFieldByNumber(8)
      name = res.getFieldByNumber(3) + " " + res.getFieldByNumber(4)
    })

    var resultSet = dbc.executeSQL(s"SELECT * FROM markt_user WHERE id = '$id'")
    var user = Json.obj()
    if (resultSet.next()) {
      Thread.sleep(1000)
      user = Json.obj(
        "id" -> resultSet.getString("id"),
        "points" -> resultSet.getInt("points"),
        "isWorker" -> resultSet.getString("isWorker"),
        "name" -> name,
        "adress" -> adress,
        "note" -> "User already exist"
      )
    }
    //neue User kriegen 500 Startpunkte
    else {
      dbc.executeUpdate(
        s"INSERT INTO markt_user (id,points,isWorker) VALUES ('$id',500,false)"
      )
      user = Json.obj(
        "points" -> 500,
        "isWorker" -> false,
        "id" -> id,
        "name" -> name,
        "adress" -> adress,
        "note" -> "userCreated"
      )
    }
    return user

  }

  /**
    * Gibt den Inhalt der Tabelle Category zurück
    */
  def getAllCategorys = Action { _ =>
    try {
      var resultSet = dbc.executeSQL("SELECT c_name FROM category")
      var categorys = new JsArray()
      while (resultSet.next()) {
        var category = Json.obj("name" -> resultSet.getString("c_name"))
        categorys = categorys.append(category)
      }
      Ok(Json.toJson(categorys))
    } catch {
      case e: SQLTimeoutException =>
        InternalServerError("SQL-Timeout Exception: " + e.toString())
      case e: SQLException =>
        InternalServerError("SQL Exception: " + e.toString())

      case e: Exception => InternalServerError("Exception: " + e.toString())
    }

  }

  /**
    * Gibt den Inhalt der Tablle Article zurück
    */
  def getAllArticle = Action { _ =>
    try {
      var resultSet = dbc.executeSQL("SELECT * FROM Article")
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
      Ok(articleList)
    } catch {
      case e: SQLTimeoutException =>
        InternalServerError("SQL-Timeout Exception: " + e.toString())
      case e: SQLException =>
        InternalServerError("SQL Exception: " + e.toString())

      case e: Exception => InternalServerError("Exception: " + e.toString())
    }
  }

  /**
    * Gibt alle Artikle zurück, die den Filteroptionen entsprechen
    *
    * @param category Filter Kategorie (_ -> null)
    * @param name Filter Zeichenkette (_ -> null)
    */
  def getArticle(category: String, name: String) = Action { _ =>
    try {
      var resultSet: ResultSet = null
      //Nur Kategorie angegeben
      if (!category.equals("_") && name.equals("_")) {
        resultSet = dbc.executeSQL(
          s"SELECT * FROM article INNER JOIN (category INNER JOIN article_category ON article_category.categoryID = category.id) ON article.id=article_category.articleID WHERE c_name='$category'"
        )
      }
      // Nur Name angegeben
      else if (category.equals("_") && !name.equals("_")) {
        resultSet = dbc.executeSQL(
          s"SELECT * FROM article WHERE name LIKE'$name%%'"
        )
      }
      //Beides angegeben
      else if (!category.equals("_") && !name.equals("_")) {
        resultSet = dbc.executeSQL(
          s"SELECT * FROM article INNER JOIN (category INNER JOIN article_category ON article_category.categoryID = category.id) ON article.id=article_category.articleID WHERE c_name='$category' AND name LIKE '$name%%'"
        )
      }
      //Nichts angegeben -> All Article
      else {
        resultSet = dbc.executeSQL("SELECT * FROM article")
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
      Ok(articleList)
    } catch {
      case e: SQLTimeoutException =>
        InternalServerError("SQL-Timeout Exception: " + e.toString())
      case e: SQLException =>
        InternalServerError("SQL Exception: " + e.toString())

      case e: Exception => InternalServerError("Exception: " + e.toString())
    }
  }

  /**
    * Gibt alle Ratings zu einen Artikel zurück
    *
    * @param id Artikel ID
    */
  def getArticleComments(id: Int) = Action { _ =>
    try {
      var resultSet =
        dbc.executeSQL(s"SELECT * FROM rating WHERE articleID = $id")

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
      Ok(comments)
    } catch {
      case e: SQLTimeoutException =>
        InternalServerError("SQL-Timeout Exception: " + e.toString())
      case e: SQLException =>
        InternalServerError("SQL Exception: " + e.toString())

      case e: Exception => InternalServerError("Exception: " + e.toString())
    }
  }

  /**
    * Gibt einen User per ID zurück
    * Wenn es keinen User mit der übergebenen ID gibt, wird ein neuer User angelegt
    * @param id UserID
    */
  def getCustomerByID(id: String) = Action { _ => Ok(getUserByID(id)) }

  /**
    * Gibt alle Bestellungen zurück
    */
  def getAllOrder = Action { _ =>
    try {
      var getOrder = dbc.executeSQL("SELECT * FROM markt_order")
      var orderList = new JsArray()
      while (getOrder.next()) {
        var getArticle = dbc.executeSQL(
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
      Ok(orderList)
    } catch {
      case e: SQLTimeoutException =>
        InternalServerError("SQL-Timeout Exception: " + e.toString())
      case e: SQLException =>
        InternalServerError("SQL Exception: " + e.toString())

      case e: Exception => InternalServerError("Exception: " + e.toString())
    }

  }

  /**
    * Gibt alle BEstellungen eines bestimmten Users zurück
    *
    * @param cid UserID
    */
  def getOrderByCustomerID(cid: String) = Action { _ =>
    try {
      val getOrder =
        dbc.executeSQL(s"SELECT * FROM markt_order WHERE userID='$cid'")
      var orderList = new JsArray()
      while (getOrder.next()) {
        val getArticle = dbc.executeSQL(
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
      Ok(orderList)
    } catch {
      case e: SQLTimeoutException =>
        InternalServerError("SQL-Timeout Exception: " + e.toString())
      case e: SQLException =>
        InternalServerError("SQL Exception: " + e.toString())

      case e: Exception => InternalServerError("Exception: " + e.toString())
    }
  }

  /**
    * Erstellt eine neue Bestellung
    */
  def newOrder = Action(parse.json) { implicit request =>
    try {
      val order = Json.toJson(request.body)
      val userID = order("userID").toString().replace('\"', '\'')
      val article = order("article").as[JsArray]
      val usedPoints = order("usedPoints").toString.toInt

      //summe berechnen
      var summe = 0.0
      for (i <- 0 to article.value.size - 1) {
        var aid = article.apply(i)("id")
        var priceRes =
          dbc.executeSQL(s"SELECT price FROM article WHERE id=$aid")
        priceRes.next()
        var price =
          priceRes.getFloat("price") * article.apply(i)("number").toString.toInt
        summe += price
      }
      summe = summe - (usedPoints / 100)

      //Bank verbindung einfügen
      implicit val sys = ActorSystem("SmartMarkt")
      implicit val mat = ActorMaterializer()
      implicit val ec = sys.dispatcher
      val bank = AccountServiceClient(GrpcClientSettings.fromConfig("account.AccountService"))
      val ibanRequest = bank.getIban(account.UserId(userID))
      var iban = "EMPTY"
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
        if (transferresult.equals("Ok")) {
          //try save order, if order fails -> undo transaction
          val orderID =
            dbc
              .executeUpdate(
                s"INSERT INTO markt_order (userID)  VALUES ($userID)"
              )
              .toInt

          for (i <- 0 to article.value.size - 1) {
            val articleID = article.apply(i)("id")
            val number = article.apply(i)("number")
            dbc.executeUpdate(
              s"INSERT INTO order_article (articleID,orderID,number) VALUES($articleID,$orderID,$number)"
            )
            val currentStock =
              dbc.executeSQL(s"SELECT stock FROM article WHERE id=$articleID")
            currentStock.next()
            var stock = currentStock.getInt("stock")
            stock = stock - number.toString.toInt
            dbc.executeUpdate(
              s"UPDATE article SET stock=$stock WHERE id=$articleID"
            )
          }
          Ok("OK")
        } else Ok("TRANFER FAILED")
      } else Ok("GET IBAN FAILED")
    } catch {
      case e: SQLTimeoutException =>
        InternalServerError("SQL-Timeout Exception: " + e.toString())
      case e: SQLException =>
        InternalServerError("SQL Exception: " + e.toString())

      case e: Exception => InternalServerError("Exception: " + e.toString())
    }

  }

  def newComment = Action(parse.json) { implicit request =>
    try {
      val comment = Json.toJson(request.body)
      val text = comment("text").toString().replace('\"', '\'')
      val rating = comment("rating")
      val userID = comment("userID").toString().replace('\"', '\'')
      val articleID = comment("articleID")
      dbc.executeUpdate(
        s"INSERT INTO rating (text,rating,userID,articleID) VALUES ($text,$rating,$userID,$articleID)"
      )
      Ok("Ok")
    } catch {
      case e: SQLTimeoutException =>
        InternalServerError("SQL-Timeout Exception: " + e.toString())
      case e: SQLException =>
        InternalServerError("SQL Exception: " + e.toString())

      case e: Exception => InternalServerError("Exception: " + e.toString())
    }
  }

  /**
    * Erstellt einen neune Artikel
    */
  def newArticle = Action(parse.json) { implicit request =>
    try {
      val article = Json.toJson(request.body)
      val manufacture = article("manufacture").toString().replace('\"', '\'')
      val name = article("name").toString().replace('\"', '\'')
      val description = article("description").toString().replace('\"', '\'')
      val price = article("price")
      dbc.executeSQL(
        s"INSERT INTO article (manufacture,name,description,price,stock) VALUES ($manufacture,$name,$description,$price,0)"
      )
      Ok("Ok")
    } catch {
      case e: SQLTimeoutException =>
        InternalServerError("SQL-Timeout Exception: " + e.toString())
      case e: SQLException =>
        InternalServerError("SQL Exception: " + e.toString())

      case e: Exception => InternalServerError("Exception: " + e.toString())
    }
  }

  /**
    * Bearbeitet den Bestellstatus
    */
  def updateOrder = Action(parse.json) { implicit request =>
    try {
      val order = Json.toJson(request.body)
      val state = order("state").toString().replace('\"', '\'')
      val id = order("id")
      dbc.executeUpdate(s"UPDATE markt_order SET state=$state WHERE id=$id")
      Ok("Ok")
    } catch {
      case e: SQLTimeoutException =>
        InternalServerError("SQL-Timeout Exception: " + e.toString())
      case e: SQLException =>
        InternalServerError("SQL Exception: " + e.toString())

      case e: Exception => InternalServerError("Exception: " + e.toString())
    }

  }

  /**
    * Berarbeitet einen Artikel
    */
  def alterArticle = Action(parse.json) { implicit request =>
    try {
      val article = Json.toJson(request.body)
      val id = article("id")
      val price = article("price")
      val manufacture = article("manufacture").toString().replace('\"', '\'')
      val name = article("name").toString().replace('\"', '\'')
      val description = article("description").toString().replace('\"', '\'')
      val stock = article("stock")
      dbc.executeUpdate(
        s"UPDATE article SET price=$price, name=$name, manufacture=$manufacture, description=$description, stock=$stock  WHERE id=$id"
      )
      Ok("Ok")
    } catch {
      case e: SQLTimeoutException =>
        InternalServerError("SQL-Timeout Exception: " + e.toString())
      case e: SQLException =>
        InternalServerError("SQL Exception: " + e.toString())

      case e: Exception => InternalServerError("Exception: " + e.toString())
    }
  }

  /**
    * Bearbeitete einen User
    */
  def alterUser = Action(parse.json) { implicit request =>
    try {
      val user = Json.toJson(request.body)
      val id = user("id").toString().replace('\"', '\'')
      val isWorker = user("isWorker")
      val points = user("points")
      dbc.executeUpdate(
        s"UPDATE markt_user SET points=$points, isWorker=$isWorker  WHERE id=$id"
      )
      Ok("Ok")
    } catch {
      case e: SQLTimeoutException =>
        InternalServerError("SQL-Timeout Exception: " + e.toString())
      case e: SQLException =>
        InternalServerError("SQL Exception: " + e.toString())

      case e: Exception => InternalServerError("Exception: " + e.toString())
    }
  }
}
