package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._
import java.sql.ResultSet
import user._

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
  //val dbURL = "jdbc:postgresql://database:5432/smartmarkt"

  //Database Controller
  val dbc = new DBController(dbuser, dbpw, dbURL)

  var res = "START"

  /**
    * Löscht bestehende Datenbank und initialisiert eine neue DB.
    */
  def createDB = Action { _ =>
    Ok(dbc.createDB("jdbc:postgresql://localhost:5432/", url))
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

  /**
    * Gibt den Inhalt der Tabelle Category zurück
    */
  def getAllCategorys = Action { _ =>
    var resultSet = dbc.executeSQL("SELECT c_name FROM category")
    var categorys = new JsArray()
    while (resultSet.next()) {
      var category = Json.obj("name" -> resultSet.getString("c_name"))
      categorys = categorys.append(category)
    }
    Ok(Json.toJson(categorys))
  }

  /**
    * Gibt den Inhalt der Tablle Article zurück
    */
  def getAllArticle = Action { _ =>
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
  }

  /**
    * Gibt alle Artikle zurück, die den Filteroptionen entsprechen
    *
    * @param category Filter Kategorie (_ -> null)
    * @param name Filter Zeichenkette (_ -> null)
    */
  def getArticle(category: String, name: String) = Action { _ =>
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
  }

  /**
    * Gibt alle Ratings zu einen Artikel zurück
    *
    * @param id Artikel ID
    */
  def getArticleComments(id: Int) = Action { _ =>
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
  }

  /**
    * Gibt einen User per ID zurück
    * Wenn es keinen User mit der übergebenen ID gibt, wird ein neuer User angelegt
    * @param id UserID
    */
  def getCustomerByID(id: String) = Action { _ =>
    var resultSet =
      dbc.executeSQL(s"SELECT * FROM markt_user WHERE id = '$id'")
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
      dbc.executeUpdate(
        "INSERT INTO markt_user (id,points,isWorker) VALUES ($id,500,false)"
      )
      user = Json.obj(
        "points" -> 500,
        "isWorker" -> false,
        "id" -> id
      )
    }
    Ok(new JsArray().append(user))
  }

  /**
    * Gibt alle Bestellungen zurück
    */
  def getAllOrder = Action { _ =>
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

  }

  /**
    * Gibt alle BEstellungen eines bestimmten Users zurück
    *
    * @param cid UserID
    */
  def getOrderByCustomerID(cid: String) = Action { _ =>
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
  }

  /**
    * Erstellt eine neue Bestellung
    */
  def newOrder = Action(parse.json) { implicit request =>
    val order = Json.toJson(request.body)
    val userID = order("userID")
    val summe = order("summe") //später für bank
    val article = order("article").as[JsArray]

    val orderID =
      dbc.executeUpdate(s"INSERT INTO markt_order (userID)  VALUES ($userID)")

    for (i <- 0 to article.value.size - 1) {
      val articleID = article.apply(i)("id")
      val number = article.apply(i)("number")
      dbc.executeUpdate(
        "INSERT INTO order_article (articleID,orderID,number) VALUES($articleID,$orderID,$number)"
      )
      val currentStock =
        dbc.executeSQL("SELECT stock FROM article WHERE id=$articleID")
      currentStock.next()
      var stock = currentStock.getInt("stock")
      stock = stock - number.toString.toInt
      dbc.executeUpdate(s"UPDATE article SET stock=$stock WHERE id=$articleID")
    }
    Ok("OK")
  }

  def newComment = Action(parse.json) { implicit request =>
    val comment = Json.toJson(request.body)
    val text = comment("text").toString().replace('\"', '\'')
    val rating = comment("rating")
    val userID = comment("userID")
    val articleID = comment("articleID")
    dbc.executeSQL(
      s"INSERT INTO rating (text,rating,userID,articleID) VALUES ($text,$rating,$userID,$articleID)"
    )
    Ok("Ok")
  }

  /**
    * Erstellt einen neune Artikel
    */
  def newArticle = Action(parse.json) { implicit request =>
    val article = Json.toJson(request.body)
    val manufacture = article("manufacture").toString().replace('\"', '\'')
    val name = article("name").toString().replace('\"', '\'')
    val description = article("description").toString().replace('\"', '\'')
    val price = article("price")
    val cat = article("cat")
    dbc.executeSQL(
      s"INSERT INTO article (manufacture,name,description,price,stock) VALUES ($manufacture,$name,$description,$price,0)"
    )
    Ok("Ok")
  }

  /**
    * Bearbeitet den Bestellstatus
    */
  def updateOrder = Action(parse.json) { implicit request =>
    val order = Json.toJson(request.body)
    val state = order("state").toString().replace('\"', '\'')
    val id = order("id")
    dbc.executeUpdate(s"UPDATE markt_order SET state=$state WHERE id=$id")
    Ok("Ok")

  }

  /**
    * Berarbeitet einen Artikel
    */
  def alterArticle = Action(parse.json) { implicit request =>
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
  }

  /**
    * Bearbeitete einen User
    */
  def alterUser = Action(parse.json) { implicit request =>
    val user = Json.toJson(request.body)
    val id = user("id")
    val isWorker = user("isWorker")
    val points = user("points")
    dbc.executeUpdate(
      s"UPDATE markt_user SET points=$points, isWorker=$isWorker  WHERE id=$id"
    )
    Ok("Ok")
  }
}
