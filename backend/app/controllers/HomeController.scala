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
class HomeController @Inject() (configuration: play.api.Configuration, val controllerComponents: ControllerComponents)
    extends BaseController {

  val dbuser = configuration.underlying.getString("myPOSTGRES_USER")
  val dbpw =configuration.underlying.getString("myPOSTGRES_PASSWORD")
  val url= configuration.underlying.getString("myPOSTGRES_DB")
 
  val dbURL = f"jdbc:postgresql://localhost:5432/$url"

  def login(name: String, pw: String) = Action { _ =>
    println(dbuser)
    val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
    var statement = connection.createStatement()
    var resultSet = statement.executeQuery("SELECT * FROM USER WHERE id= 1")
    var user = Json.obj()
    if (resultSet.next()) {
      user = Json.obj(
        "name" -> "Beispiel Nutzer",
        "adresse" -> "Beispielweg 22",
        "points" -> resultSet.getInt("points"),
        "isWorker" -> resultSet.getString("isWorker"),
        "id" -> resultSet.getInt("id")
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
        "manufacutre" -> resultSet.getString("manufacture"),
        "name" -> resultSet.getString("name"),
        "decription" -> resultSet.getString("description"),
        "price" -> resultSet.getFloat("price"),
        "stock" -> resultSet.getInt("stock")
      )
      articleList = articleList.append(article)
    }
    Ok(Json.toJson(articleList))
  }

  def getArticleByID(id: Int) = Action { _ =>
    val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
    var statement = connection.createStatement()
    var resultSet =
      statement.executeQuery(s"SELECT * FROM Article WHERE id = $id")
    var article = Json.obj()
    if (resultSet.next()) {
      article = Json.obj(
        "id" -> resultSet.getInt("id"),
        "manufacutre" -> resultSet.getString("manufacture"),
        "name" -> resultSet.getString("name"),
        "decription" -> resultSet.getString("description"),
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
        f"SELECT * FROM Article WHERE name LIKE'$name%%'"
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
      resultSet = statement.executeQuery("SELECT * FROM Article")
    }

    var articleList = new JsArray()
    while (resultSet.next()) {
      var article = Json.obj(
        "id" -> resultSet.getInt("id"),
        "manufacutre" -> resultSet.getString("manufacture"),
        "name" -> resultSet.getString("name"),
        "decription" -> resultSet.getString("description"),
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
        "userID" -> resultSet.getInt("userID"),
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
        "id" -> resultSet.getInt("id"),
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
          "manufacutre" -> getArticle.getString("manufacture"),
          "name" -> getArticle.getString("name"),
          "decription" -> getArticle.getString("description"),
          "price" -> getArticle.getFloat("price"),
          "stock" -> getArticle.getInt("stock"),
          "number" -> getArticle.getInt("number")
        )
        articleInOrder = articleInOrder.append(article)

      }
      var order = Json.obj(
        "id" -> getOrder.getInt("id"),
        "userID" -> getOrder.getInt("userID"),
        "state" -> getOrder.getString("state"),
        "date" -> getOrder.getString("date"),
        "article" -> articleInOrder
      )
      orderList = orderList.append(order)
    }
    Ok(Json.toJson(orderList))

  }

  def getOrderByID(id: Int) = Action { _ =>
    val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
    val statement = connection.createStatement()
    val getOrder =
      statement.executeQuery(f"SELECT * FROM markt_order WHERE id=$id")
    var order = Json.obj()
    if (getOrder.next()) {
      val statement2 = connection.createStatement()
      val getArticle = statement2.executeQuery(
        f"SELECT * FROM  article INNER JOIN order_article ON article.id = order_article.articleID WHERE order_article.orderID=$id"
      )
      var articleInOrder = new JsArray()
      while (getArticle.next()) {
        val article = Json.obj(
          "id" -> getArticle.getInt("id"),
          "manufacutre" -> getArticle.getString("manufacture"),
          "name" -> getArticle.getString("name"),
          "decription" -> getArticle.getString("description"),
          "price" -> getArticle.getFloat("price"),
          "stock" -> getArticle.getInt("stock"),
          "number" -> getArticle.getInt("number")
        )
        articleInOrder = articleInOrder.append(article)

      }
      order = Json.obj(
        "id" -> getOrder.getInt("id"),
        "userID" -> getOrder.getInt("userID"),
        "state" -> getOrder.getString("state"),
        "date" -> getOrder.getString("date"),
        "article" -> articleInOrder
      )
    }
    Ok(order)
  }

  def getOrderByCustomerID(cid: Int) = Action { _ =>
    val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
    val statement = connection.createStatement()
    val getOrder =
      statement.executeQuery(s"SELECT * FROM markt_order WHERE userID=$cid")
    var order = Json.obj()
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
          "manufacutre" -> getArticle.getString("manufacture"),
          "name" -> getArticle.getString("name"),
          "decription" -> getArticle.getString("description"),
          "price" -> getArticle.getFloat("price"),
          "stock" -> getArticle.getInt("stock"),
          "number" -> getArticle.getInt("number")
        )
        articleList = articleList.append(article)

      }
      order = Json.obj(
        "id" -> getOrder.getInt("id"),
        "userID" -> getOrder.getInt("userID"),
        "state" -> getOrder.getString("state"),
        "date" -> getOrder.getString("date"),
        "article" -> articleList
      )

    }
    Ok(order)
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
