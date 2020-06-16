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
  val dbURL = s"jdbc:postgresql://localhost:5432/$url"
  
  val token= "eyJhbGciOiJSUzI1NiIsImtpZCI6IjRlMjdmNWIwNjllYWQ4ZjliZWYxZDE0Y2M2Mjc5YmRmYWYzNGM1MWIiLCJ0eXAiOiJKV1QifQ.eyJuYW1lIjoibW11c3RlciIsImlzcyI6Imh0dHBzOi8vc2VjdXJldG9rZW4uZ29vZ2xlLmNvbS9zbWFydC1jaXR5LXNzMjAyMCIsImF1ZCI6InNtYXJ0LWNpdHktc3MyMDIwIiwiYXV0aF90aW1lIjoxNTkyMzMzNzMwLCJ1c2VyX2lkIjoiNlRiemNQYXZyU05kcTFXMXFBS3F5ZmhodnhCMiIsInN1YiI6IjZUYnpjUGF2clNOZHExVzFxQUtxeWZoaHZ4QjIiLCJpYXQiOjE1OTIzMzM3MzEsImV4cCI6MTU5MjMzNzMzMSwiZW1haWwiOiJleGFtcGxldXNlckB0ZXN0LmRlIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImZpcmViYXNlIjp7ImlkZW50aXRpZXMiOnsiZW1haWwiOlsiZXhhbXBsZXVzZXJAdGVzdC5kZSJdfSwic2lnbl9pbl9wcm92aWRlciI6InBhc3N3b3JkIn19.GhbYnIhnuRR9mB_5leNRjSZElxHV0gGRrybc-CuTS3XV7UcFj4On2L9jTTY9X376kw32MKq11dvYl34e_vhKk-Syb2V0R9k_KeC6dsu7EWKnxSyR5X0HDkRGImHFgEcoBzyrT_FeokaHRnzuo9JOgQlVkvYhC8I0LYawYiSxI8sU4IAdSqkN4YpdaRGYtp7Cf35o5jwOWOdq3F3aYP1_MCBP3AZk9YlL12_J8T54qCD86phVfhsTaoOulQ6Itu9D5tjgiPwMPocV3L9Ia977aaUbBJiTPahM_YHcmaJ4pF1nlG1hQFDSj5sWb2cIHFLoFIboqJAJns-9VzlU_2bGtg"
  var res="START"

  def createDB = Action { _ =>
    var sql = ""

    try {
      val rootConnection = DriverManager
        .getConnection("jdbc:postgresql://database:5432", dbuser, dbpw)
      var rootStatement = rootConnection.createStatement()
      sql =
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
      val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
      var statement = connection.createStatement()

      sql = "DROP TABLE IF EXISTS category;"
      statement.execute(sql)
      sql = "DROP TABLE IF EXISTS article;"
      statement.execute(sql)
      sql = "DROP TABLE IF EXISTS article_category;"
      statement.execute(sql)
      sql = "DROP TABLE IF EXISTS markt_user;"
      statement.execute(sql)
      sql = "DROP TABLE IF EXISTS order_article;"
      statement.execute(sql)
      sql = "DROP TABLE IF EXISTS rating;"
      statement.execute(sql)
      sql = "DROP TABLE IF EXISTS markt_order;"
      statement.execute(sql)

      sql =
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

      sql = "INSERT INTO markt_user (id,points,isWorker) VALUES ('1',500,TRUE);"
      statement.execute(sql)
      sql =
        "INSERT INTO category(c_name)VALUES('Gemuese'),('Obst'),('Fleisch'),('Backwaren'),('Milchprodukte'),('Tiernahrung'),('Haushaltsmittel'),('Vegetarisch'),('Sonstiges');"
      statement.execute(sql)
      sql =
        "INSERT INTO article(manufacture,name,description,price,stock)VALUES ('Schrott&Teuer', 'Ziegenkaese 500g', 'Lecker schmecker Ziegenkaese', 3, 5), ('Schrott&Teuer', 'Ziegenkaese 200g', 'Lecker schmecker Ziegenkaese', 1.5, 5),('Schrott&Teuer', 'Fertig Pizza Salami', 'Lecker schmecker Pizza', 2.5, 5),('Schrott&Teuer', 'Erdbeer Marmelade 100g', 'Lecker schmecker Marmelade', 0.5, 5),('Schrott&Teuer', 'Cola 2L', 'Lecker schmecker Cola', 1.0, 5);"
      statement.execute(sql)
      sql =
        "INSERT INTO article_category(articleID,categoryID)VALUES(1, 5),(2, 3),(2, 9),(2, 4),(3, 2),(3, 8),(3, 9),(4, 8),(4, 9);"
      statement.execute(sql)
      sql =
        "INSERT INTO rating (text,rating,userID,articleID) VALUES ('Tolles Produkt!',4,'1',1);"
      statement.execute(sql)

      sql =
        "INSERT INTO markt_order (userID,state) VALUES ('1', 'Auf den Weg');"
      statement.execute(sql)
      sql =
        "INSERT INTO order_article(articleID,orderID,number)VALUES(1, 1, 5),(2, 1, 5);"
      statement.execute(sql)

      connection.close()
      Ok("DB CREATED")

    } catch {
      case e: Exception => Ok(e.toString())
    }

  }

  import scala.concurrent.ExecutionContext.Implicits.global

  def login(token: String) =
    Action //.async
    { _ =>
      verifyUser(token)
      // val uid = await(verifyUser(token))
      // val user=await(getUser(uid))
      //  Ok(user)
      Ok(res)
    }

  def verifyUser(token: String):String = {
      implicit val sys = ActorSystem("SmartMarkt")
      implicit val mat = ActorMaterializer()
      implicit val ec = sys.dispatcher
      val client =
        UserServiceClient(GrpcClientSettings.fromConfig("user.UserService"))
      val reply = client.verifyUser(UserToken(token))
      reply.onComplete {
        case Success(msg: UserId) =>res=msg.uid.toString()
        case Failure(exception) => res=exception.toString()
        case _ => "res=Unknown ERROR on verifyUser"
      }

      return "test"
    }

    /*
    def getUser(uid: String): JsArray = {
    val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
    var statement = connection.createStatement()
    var resultSet =
      statement.executeQuery(s"SELECT * FROM markt_user WHERE id= $uid")
    var user = Json.obj()

    if (resultSet.next()) {
      user = Json.obj(
        "points" -> resultSet.getInt("points"),
        "isWorker" -> resultSet.getString("isWorker"),
        "id" -> resultSet.getInt("id")
      )
    } else {
      //neue User kriegen 500 Startpunkte
      var statement2 = connection.prepareStatement(
        s"INSERT INTO markt_user (id,points,isWorker) VALUES ($uid,500,false)"
      )
      statement2.executeUpdate()
      user = Json.obj(
        "points" -> 500,
        "isWorker" -> false,
        "id" -> uid
      )
    }
    new JsArray().append(user)
    }

   */

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
  //Maybe delete
  def getOrderByID(id: Int) = Action { _ =>
    val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
    val statement = connection.createStatement()
    val getOrder =
      statement.executeQuery(s"SELECT * FROM markt_order WHERE id=$id")
    var orderList = new JsArray()
    if (getOrder.next()) {
      val statement2 = connection.createStatement()
      val getArticle = statement2.executeQuery(
        s"SELECT * FROM  article INNER JOIN order_article ON article.id = order_article.articleID WHERE order_article.orderID=$id"
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
