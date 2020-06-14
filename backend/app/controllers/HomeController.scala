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
  val dbURL = f"jdbc:postgresql://localhost:5432/$url"
  createDB


  def createDB: Unit = {
    val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
    var statement = connection.createStatement()
    try {
      var sql =
        "CREATE TABLE category(id SERIAL PRIMARY KEY NOT NULL,c_name TEXT NOT NULL);"
      statement.execute(sql)
      sql =
        "CREATE TABLE article(id SERIAL PRIMARY KEY NOT NULL,manufacture TEXT NOT NULL,name TEXT NOT NULL,description TEXT NOT NULL,price FLOAT NOT NULL,picture BYTEA,stock INTEGER NOT NULL);"
      statement.execute(sql)
      sql =
        "CREATE TABLE article_category(articleID INTEGER REFERENCES article(id),categoryID INTEGER REFERENCES category(id));"
      statement.execute(sql)
      sql =
        "CREATE TABLE markt_user(id TEXT PRIMARY KEY NOT NULL,points INTEGER,isWorker BOOLEAN NOT NULL);"
      statement.execute(sql)
      sql =
        "CREATE TABLE markt_order(id SERIAL PRIMARY KEY NOT NULL,userID TEXT REFERENCES markt_user(id),state TEXT NOT NULL DEFAULT 'Unbearbeitet',date DATE NOT NULL DEFAULT CURRENT_TIMESTAMP);"
      statement.execute(sql)
      sql =
        "CREATE TABLE order_article(articleID INTEGER REFERENCES article(id),orderID INTEGER REFERENCES markt_order(id),number INTEGER NOT NULL);"
      statement.execute(sql)
      sql =
        "CREATE TABLE rating(id SERIAL PRIMARY KEY NOT NULL,text TEXT NOT NULL,rating INTEGER NOT NULL,userID TEXT REFERENCES markt_user(id),articleID INTEGER REFERENCES article(id));"
      statement.execute(sql)
      sql =
        "INSERT INTO category(c_name)VALUES('Gemuese'),('Obst'),('Fleisch'),('Backwaren'),('Milchprodukte'),('Tiernahrung'),('Haushaltsmittel'),('Vegetarisch'),('Sonstiges');"
      statement.execute(sql)
      sql =
        "INSERT INTO article(manufacture,name,description,price,stock)VALUES('Schrott&Teuer', 'Ziegenkaese 200g', 'Lecker schmecker Ziegenkaese', 1.5, 5),('Schrott&Teuer', 'Fertig Pizza Salami', 'Lecker schmecker Pizza', 2.5, 5),('Schrott&Teuer', 'Erdbeer Marmelade 100g', 'Lecker schmecker Marmelade', 0.5, 5),('Schrott&Teuer', 'Cola 2L', 'Lecker schmecker Cola', 1.0, 5);"
      statement.execute(sql)
      sql =
        "INSERT INTO article_category(articleID,categoryID)VALUES(1, 5),(2, 3),(2, 9),(2, 4),(3, 2),(3, 8),(3, 9),(4, 8),(4, 9);"
      statement.execute(sql)

    } catch {
      case e: Exception => println("DB ALREADY EXIST")
    }
  }

import scala.concurrent.ExecutionContext.Implicits.global
  def login(token: String) = Action { _ =>   
    var uid="STARTVALUE"
    val f = Future {
      implicit val sys = ActorSystem("SmartMarkt")
      implicit val mat = ActorMaterializer()
      implicit val ec = sys.dispatcher
     
      val client = UserServiceClient(GrpcClientSettings.fromConfig("user.UserService"))
      val reply = client.verifyUser(UserToken(token))
      reply.onComplete {
        case Success(msg: UserId) => uid=msg.uid.toString()
        case Failure(exception) => InternalServerError(exception.toString())
        case _ => InternalServerError("Unknown ERROR on verifyUser") 
      }
    }   
    Await.ready(f, Duration.Inf)
    Ok(uid.toString())
    if (uid.equals("")) InternalServerError("Timeout")

    val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
    var statement = connection.createStatement()
    var resultSet =
      statement.executeQuery(f"SELECT * FROM markt_user WHERE id= $uid")
    var user = Json.obj()

    if (resultSet.next()) {
      user = Json.obj(
        "name" -> "Beispiel Nutzer",
        "adresse" -> "Beispielweg 22",
        "points" -> resultSet.getInt("points"),
        "isWorker" -> resultSet.getString("isWorker"),
        "id" -> resultSet.getInt("id")
      )
    } else {
      //neue User kriegen 500 Startpunkte
      var statement2 = connection.prepareStatement(
        f"INSERT INTO markt_user (id,points,isWorker) VALUES ($uid,500,false)"
      )
      statement2.executeUpdate()
      user = Json.obj(
        "name" -> "Beispiel Nutzer",
        "adresse" -> "Beispielweg 22",
        "points" -> 500,
        "isWorker" -> false,
        "id" -> uid
      )
    }
    Ok(user)

 
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
    Ok(Json.toJson(articleList))
  }

  //Maybe delete
  def getArticleByID(id: Int) = Action { _ =>
    val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
    var statement = connection.createStatement()
    var resultSet =
      statement.executeQuery(s"SELECT * FROM Article WHERE id = $id")
    var article = Json.obj()
    if (resultSet.next()) {
      article = Json.obj(
        "id" -> resultSet.getInt("id"),
        "manufacture" -> resultSet.getString("manufacture"),
        "name" -> resultSet.getString("name"),
        "description" -> resultSet.getString("description"),
        "price" -> resultSet.getFloat("price"),
        "stock" -> resultSet.getInt("stock")
      )

    }
    Ok(article)
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
        f"SELECT * FROM article WHERE name LIKE'$name%%'"
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
    Ok(Json.toJson(articleList))
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
    Ok(Json.toJson(comments))
  }

  def getCustomerByID(id: Int) = Action { _ =>
    val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
    var statement = connection.createStatement()
    var resultSet =
      statement.executeQuery(s"SELECT * FROM markt_user WHERE id = $id")
    var user = Json.obj()
    if (resultSet.next()) {
      user = Json.obj(
        "id" -> resultSet.getString("id"),
        "points" -> resultSet.getInt("points"),
        "isWorker" -> resultSet.getString("isWorker"),
        "name" -> "Beispiel Nutzer",
        "adresse" -> "Beispielweg 22"
      )

    }
    Ok(user)
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
    Ok(Json.toJson(orderList))

  }
  //Maybe delete
  def getOrderByID(id: Int) = Action { _ =>
    val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
    val statement = connection.createStatement()
    val getOrder =
      statement.executeQuery(f"SELECT * FROM markt_order WHERE id=$id")
    var orderList = new JsArray()
    if (getOrder.next()) {
      val statement2 = connection.createStatement()
      val getArticle = statement2.executeQuery(
        f"SELECT * FROM  article INNER JOIN order_article ON article.id = order_article.articleID WHERE order_article.orderID=$id"
      )
      var articleInOrder = new JsArray()
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
    Ok(Json.toJson(orderList))
  }

  def getOrderByCustomerID(cid: Int) = Action { _ =>
    val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
    val statement = connection.createStatement()
    val getOrder =
      statement.executeQuery(s"SELECT * FROM markt_order WHERE userID=$cid")
    var orderList = new JsArray()
    if (getOrder.next()) {
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
    Ok(Json.toJson(orderList))
  }

  def newOrder = Action(parse.json) { implicit request =>
    try {
      val order = Json.toJson(request.body)
      val userID = order("userID")
      val article = order("article").as[JsArray]
      val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
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
    }

  }

  def newComment = Action(parse.json) { implicit request =>
    try {
      val comment = Json.toJson(request.body)
      val text = comment("text").toString().replace('\"', '\'')
      val rating = comment("rating")
      val userID = comment("userID")
      val articleID = comment("articleID")

      val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)

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
    }

  }

  def newArticle = Action(parse.json) { implicit request =>
    try {
      val article = Json.toJson(request.body)
      val manufacture = article("manufacture").toString().replace('\"', '\'')
      val name = article("name").toString().replace('\"', '\'')
      val description = article("description").toString().replace('\"', '\'')
      val price = article("price")

      val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
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
    }

  }

  def updateOrder = Action(parse.json) { implicit request =>
    try {
      val order = Json.toJson(request.body)
      val state = order("state").toString().replace('\"', '\'')
      val id = order("id")

      val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
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
    }

  }

  def alterArticle = Action(parse.json) { implicit request =>
    try {
      val article = Json.toJson(request.body)
      val id = article("id")
      val price = article("price")
      val manufacture = article("manufacture").toString().replace('\"', '\'')
      val name = article("name").toString().replace('\"', '\'')
      val description = article("description").toString().replace('\"', '\'')
      val stock = article("stock")

      val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
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
    }
  }

  def alterUser = Action(parse.json) { implicit request =>
    try {
      val user = Json.toJson(request.body)
      val id = user("id")
      val isWorker = user("isWorker")
      val points = user("points")

      val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
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
    }
  }

}
