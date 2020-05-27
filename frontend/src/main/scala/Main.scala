import org.scalajs.dom
import dom.document
import org.querki.jquery._
import java.util.ArrayList


object Main {
  var user: User = _
  //evtl auch als Object, dann kann man spaß machen wie Contains: etc.
  val shoppingcar = new ArrayList[Article]
  val articleList = new ArrayList[Article]
  def main(args: Array[String]): Unit = {

    createHomePage()

  }

  /**
    * Erstellt die Navigationsleiste um zwischen den einzelnen Obermenüs zu wechseln    *
    * @param logedIn Ist der User eingeloogt?
    * @param isWorker Ist der User ein angestellter?
    */
  def createNavigator(logedIn: Boolean, isWorker: Boolean = false): Unit = {
    val userNavi = document.getElementById("navigtor")
    val userList = document.createElement("ul")

    //Login bzw. logout button
    val logButton = document.createElement("button")
    val li1 = document.createElement("li")
    logButton.id = "log-button"
    li1.appendChild(logButton)
    if (logedIn) {
      logButton.textContent = ("LogOut")

      //My Orders
      val myOrders = document.createElement("button")
      val li2 = document.createElement("li")
      myOrders.id = ("my-orders-button")
      myOrders.textContent = ("Meine Bestellungen")
      li2.appendChild(myOrders)
      userList.appendChild(li2)

      //Einkaufswagen
      val shoppingcar = document.createElement("button")
      val li3 = document.createElement("li")
      shoppingcar.id = ("shoppingcar-button")
      shoppingcar.textContent = ("Einkaufswagen")
      li3.appendChild(shoppingcar)
      userList.appendChild(li3)

      if (isWorker) {
        //Lager
        val warehouse = document.createElement("button")
        val li4 = document.createElement("li")
        warehouse.id = ("warehouse-button")
        warehouse.textContent = ("Lager")
        li4.appendChild(warehouse)
        userList.appendChild(li4)
        //Bestellungen
        val orders = document.createElement("button")
        val li5 = document.createElement("li")
        orders.id = ("orders-button")
        orders.textContent = ("Bestellungen")
        li5.appendChild(orders)
        userList.appendChild(li5)
      }

    } else
      logButton.textContent = ("LogIn")
    userList.appendChild(li1)
    userNavi.appendChild(userList)

    //Navigator-Button listener
    $("#log-button").click { () => { createLogInPage(false) } }
    //$("#log-button").click { () => { test } }
    $("#my-orders-button").click { () => createMyOrdersPage }
    $("#shoppingcar-button").click { () => createShoppingcarPage }
    $("#warehouse-button").click { () => createWarehousePage }
    $("#orders-button").click { () => createOrdersPage }
  }

  /**
    * Erstellt die Homeseite
    */
  def createHomePage(): Unit = {
    createNavigator(true)
    createArticleOverview()
  }

  /**
    * Erstellt die Artikeloverview
    */
  def createArticleOverview(): Unit = {
    var content = document.getElementById("content")
    //Kategorien Filter
    //Suchleiste
    //Artikel Vorschau
    //Bild, Name, Beschreibung, Bewertung, Preis, In den Einkaufswagen Button
  }

  /**
    * Erstellt die Meine Bestllungen Seite
    */
  def createMyOrdersPage(): Unit = {
    var content = document.getElementById("content")
    println("Test")
    //Bestellungen
    //Übersicht
    //Status
  }

  /**
    * Erstellt die Einkaufswagen Seite
    */
  def createShoppingcarPage(): Unit = {
    var content = document.getElementById("content")
    //Artikel Liste
    //Artikel
    //Bild
    //Namen
    //Anzahl
    //Preis
    //+ - entfernen button
    //GesamtPreis
    //Jetzt kaufen Button
  }

  /**
    * Erstellt die LogIn Seite
    */
  def createLogInPage(logedIn: Boolean): Unit = {
    if (logedIn) //logout
      return
    var content = document.getElementById("content")
    //Namen
    //PW
    //OK Button
  }

  /**
    * Erstellt die Lager Seite
    */
  def createWarehousePage(): Unit = {
    var content = document.getElementById("content")
    //Artikeliste
    //ID
    //Name
    //Lagerbestand
    //Preisändern
    //Anzahl neu Ordern
    //Lager füllen
    //Add new Artikel
  }

  /**
    * Erstellt die Übersichtseite alle Bestellungen
    */
  def createOrdersPage(): Unit = {
    var content = document.getElementById("content")
    //Bestellungsliste
    //Bestllung
    //Status, Kunde
    //Suchleiste
  }

  /**
    * Erstellt die Artikel ansicht
    */
  def createArticlePage(): Unit = {
    var content = document.getElementById("content")
    //Bild,Name,Beschreibung,Preis,Anzahl,KaufButton
    //UserBEwertungen, Bewertung schreiben
  }


 //API GET CALL EXAMPLE
 /* def test : Unit ={   
    val xhr = new dom.XMLHttpRequest()   
    xhr.open("GET","http://localhost:9000/allArticle")   
    xhr.onload = { (e: dom.Event) =>
      if (xhr.status == 200) {       
         dom.window.alert("YAY "+ xhr.responseText)
      } else {
        dom.window.alert("ERROR MSG: " + xhr.responseText)
      }
    }   
    xhr.send()

  }*/
 
}
//Befehl zum compilen sbt ~fastOptJS pder sbt fullOptJS
