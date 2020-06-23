package controllers

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement

/**
  * Handelt Verbindung zur Datenbank
  *
  * @param dbuser
  * @param dbpw
  * @param dbURL
  */
class DBController(val dbuser: String, val dbpw: String, val dbURL: String) {

  /**
    * Initialisiert eine neue Datenbank
    *
    * @param rootUrl übergeordnete URL der Datenbank
    * @param url name der Datenbank
    * @return erfolg bzw error msg
    */
  def createDB(rootUrl: String, url: String): String = {
    try {
      val rootConnection = DriverManager.getConnection(rootUrl, dbuser, dbpw)
      var rootStatement = rootConnection.createStatement()
      println("connection" + rootConnection)

      var sql =
        s"SELECT 'CREATE DATABASE $url' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$url');"
      val affectedRows = rootStatement.executeUpdate(sql)
      // 0 affected Rows -> DB already exists
      if (affectedRows != 0) {
        sql = s"CREATE DATABASE $url;"
        rootStatement.executeUpdate(sql)
      } else
        rootConnection.close()
      this.dropDB();

    } catch {
      case e: Exception => return e.toString()
    }
    try {
      this.setupDB();
      this.fillDB()
      return "DB CREATED"
    } catch {
      case e: Exception => return e.toString()
    }
  }

  /**
    * Löscht alle Tabellen aus der DB
    */
  def dropDB(): Unit = {
    var connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
    var statement = connection.createStatement()

    try {
      var sql = "DROP TABLE IF EXISTS article_category;"
      statement.execute(sql)
      sql = "DROP TABLE IF EXISTS order_article;"
      statement.execute(sql)
      sql = "DROP TABLE IF EXISTS rating;"
      statement.execute(sql)
      sql = "DROP TABLE IF EXISTS markt_order;"
      sql = "DROP TABLE IF EXISTS category;"
      statement.execute(sql)
      sql = "DROP TABLE IF EXISTS article;"
      statement.execute(sql)
      sql = "DROP TABLE IF EXISTS markt_user;"
      statement.execute(sql)
    } catch {
      case e: Exception => println(e)
    }
    connection.close()
  }

  /**
    * Erstellt alle Tabellen der DB
    */
  def setupDB(): Unit = {
    var connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
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
    } catch {
      case e: Exception => println(e)
    }
    connection.close()
  }

  /**
    * Einfügen von Beispieldaten
    */
  def fillDB(): Unit = {
    var connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
    var statement = connection.createStatement()
    try {

      var sql =
        "INSERT INTO markt_user (id,points,isWorker) VALUES ('1',500,TRUE);"
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

    } catch {
      case e: Exception => println(e)
    }
    connection.close()
  }

  /**
    * Ausführen eines SQL SELECT Befehls
    *
    * @param sql SELECT Statement
    * @return Ergebniss
    */
  def executeSQL(sql: String): ResultSet = {
    val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
    var statement = connection.createStatement()
    var resultSet = statement.executeQuery(sql)
    connection.close()
    resultSet
  }

  /**
    * Ausführen eines SQL Update/Insert Befehls
    *
    * @param sql SQL Statement
    * @return Key
    */
  def executeUpdate(sql: String): Long = {
    val connection = DriverManager.getConnection(dbURL, dbuser, dbpw)
    var statement =
      connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
    statement.execute()
    connection.close()
    val generatedKey = statement.getGeneratedKeys()
    generatedKey.next()
    generatedKey.getLong(1)
  }

}
