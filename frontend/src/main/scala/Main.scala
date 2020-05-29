import org.scalajs.dom
import dom.document
import org.querki.jquery._
import java.util.ArrayList

object Main {
  var user = new User(1,true,220)
  //evtl auch als Object, dann kann man spaß machen wie Contains: etc.
  val shoppingcar = new ArrayList[Article]
  val articleList = new ArrayList[Article]
  def main(args: Array[String]): Unit = {
    createHomePage()

  }

  /**
    * Erstellt die Homeseite
    */
  def createHomePage(): Unit = {
    createNavigator(true)
    createArticleOverview()
  }

  /**
    * Erstellt die Navigationsleiste um zwischen den einzelnen Obermenüs zu wechseln    *
    * @param logedIn Ist der User eingeloogt?
    * @param isWorker Ist der User ein angestellter?
    */
  def createNavigator(logedIn: Boolean, isWorker: Boolean = true): Unit = {
    val userNavi = document.getElementById("navigtor")
    val userList = document.createElement("ul")

    //Home button
    val homeButton = document.createElement("button")
    homeButton.textContent = "Home"
    homeButton.id = "home-button"
    val li0 = document.createElement("li")
    li0.appendChild(homeButton)
    userList.appendChild(homeButton)

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
    $("#home-button").click { () => { createArticleOverview() } }
    $("#log-button").click { () => { createLogInPage(false) } }
    //$("#log-button").click { () => { test } }
    $("#my-orders-button").click { () => createMyOrdersPage }
    $("#shoppingcar-button").click { () => createShoppingcarPage }
    $("#warehouse-button").click { () => createWarehousePage }
    $("#orders-button").click { () => createOrdersPage }
  }

  def clearContent(): Unit = {
    var content = document.getElementById("content")
    while (content.firstChild != null) {
      content.removeChild(content.firstChild)
    }
  }

  /**
    * Erstellt die Artikeloverview
    */
  def createArticleOverview(
      filterCat: String = null,
      filterName: String = null
  ): Unit = {
    clearContent()
    val content = document.getElementById("content")

    //Kategorie Navigator
    //API Nachfrage
    val navDiv = document.createElement("div")
    navDiv.id = "catDiv"
    val example = List("Hase", "Tier", "Gemüse", "Obst", "käse")
    val categoryList = document.createElement("ul")
    for (cat <- example) {
      val li = document.createElement("li")
      val c = document.createElement("button")
      c.id = (cat)
      c.textContent = (cat)
      li.appendChild(c)
      categoryList.appendChild(li)
    }
    navDiv.appendChild(categoryList)
    content.appendChild(navDiv)
    for (cat <- example) {
      $("#" + cat).click { () => createArticleOverview(cat) }
    }
    val searchBar = document.createElement("INPUT")
    searchBar.id = "stock-search"
    searchBar.setAttribute("type","Text")
    searchBar.setAttribute("placeholder", "Suche...")
    content.appendChild(searchBar)
    // GET mit Filtern
    //Später ne liste mit :Article
    val a1 = new Article(1, "M1", "D1", "N1", 20f, 12)
    val a2 = new Article(2, "M2", "D2", "N2", 2f, 12)
    val exampleArticle = List(a1, a2)

    val allArticleDiv = document.createElement("div")
    allArticleDiv.id = "allArticleDiv"

    for (article <- exampleArticle) {
      val articleDiv = document.createElement("div")
      articleDiv.id = article.getName
      val articleName = document.createElement("h3")
      articleName.innerHTML = article.getName
      val articleDescription = document.createTextNode(article.getDescription)
      val articlePrice = document.createTextNode("Preis: " + article.getPrice)
      val articleImg = document.createElement("IMG")
      articleImg.setAttribute("src", "../src/main/scala/tmp.png")
      articleImg.setAttribute("width", "50")
      articleImg.setAttribute("height", "50")
      articleDiv.appendChild(articleName)
      articleDiv.appendChild(articleImg)
      articleDiv.appendChild(articleDescription)
      articleDiv.appendChild(document.createElement("BR"))
      articleDiv.appendChild(articlePrice)
      allArticleDiv.appendChild(articleDiv)
    }
    content.appendChild(allArticleDiv)
    for (article <- exampleArticle) {
      $("#" + article.getName()).click { () => createArticlePage(article) }
    }
  }

  /**
    * Erstellt die Artikel ansicht
    */
  def createArticlePage(article: Article): Unit = {
    clearContent()

    //Article view
    var content = document.getElementById("content")
    val articleDiv = document.createElement("div")
    val articleName = document.createElement("h1")
    articleName.innerHTML = article.getName
    val articleDescription = document.createTextNode(article.getDescription)
    val articlePrice = document.createTextNode("Preis: " + article.getPrice)
    val articleImg = document.createElement("IMG")
    articleImg.setAttribute("src", "../src/main/scala/tmp.png")
    articleImg.setAttribute("width", "50")
    articleImg.setAttribute("height", "50")
    val number = document.createElement("INPUT")
    number.id = ("number")
    number.setAttribute("type", "number")
    number.setAttribute("value", "1")
    number.setAttribute("min", "1")
    number.setAttribute("max", "" + article.getStock)
    val buyButton = document.createElement("Button")
    buyButton.id = "buy-button"
    buyButton.textContent = "Zum Warenkorb hinzufügen"

    articleDiv.appendChild(articleName)
    articleDiv.appendChild(articleImg)
    articleDiv.appendChild(articleDescription)
    articleDiv.appendChild(document.createElement("BR"))
    articleDiv.appendChild(articlePrice)
    articleDiv.appendChild(number)
    articleDiv.appendChild(buyButton)
    content.appendChild(articleDiv)

    //FIXME
    $("#buy-button").click { () =>
      // add to warenkorb
      println(
        "add to warenkorb: " + document
          .getElementById("number")
          .getAttribute("value")
      )
      //show warenkorb
    }

    //Write Review
    //if loged in
    val writeReviewDiv = document.createElement("div")
    val textField = document.createElement("INPUT")
    textField.setAttribute("type", "text")
    textField.id = "text-field"
    val yourRating = document.createElement("INPUT")
    yourRating.setAttribute("type", "number")
    yourRating.setAttribute("min", "1")
    yourRating.setAttribute("max", "5")
    yourRating.setAttribute("value", "3")
    yourRating.id = "your-rating"
    val sendButton = document.createElement("Button")
    sendButton.id = "send-button"
    sendButton.textContent = "Senden"

    writeReviewDiv.appendChild(textField)
    writeReviewDiv.appendChild(yourRating)
    writeReviewDiv.appendChild(sendButton)
    content.appendChild(writeReviewDiv)

    //FIXME
    $("#send-button").click { () =>
      //new Review -> article.addReview
      println(
        "send review: " + document
          .getElementById("text-field")
          .innerHTML + "for " + article.getID + " is: " + document
          .getElementById("your-rating")
          .getAttribute("value")
      )
    }

    //Show Reviews
    val allReviewDiv = document.createElement("div")
    val review1 = new Review("Gut", 3, null, article)
    val review2 = new Review("Gut", 3, null, article)
    val reviews = List(review1, review2)

    for (review <- reviews) {
      val reviewDiv = document.createElement("div")
      val text = document.createTextNode(review.getText)
      val author = document.createTextNode("" + review.getUser)
      val rating = document.createTextNode("" + review.getRating)
      //val date = document.createTextNode(review.getDate)
      //reviewDiv.appendChild(date)
      reviewDiv.appendChild(author)
      reviewDiv.appendChild(text)
      reviewDiv.appendChild(rating)
      allReviewDiv.appendChild(reviewDiv)
      allReviewDiv.appendChild(document.createElement("BR"))
    }
    content.appendChild(allReviewDiv)

  }

  /**
    * Erstellt die Meine Bestllungen Seite
    */
  def createMyOrdersPage(): Unit = {
    clearContent()
    var content = document.getElementById("content")

    val a1 = new Article(1, "M1", "D1", "N1", 20f, 12)
    val a2 = new Article(2, "M2", "D2", "N2", 2f, 12)
    val o1 = new Order(1,user,"On the Way")
    val o2 = new Order(1,user,"In Bearbeitung")
    var articles= List(a1,a2)
    val orders = List(o1,o2)
    for (order<-orders){
      val orderDiv = document.createElement("div")
      //Order Datum noch anzeigen
      //GET Articles to order

      for (article<-articles){
        val articleDiv = document.createElement("div")
        val articleName= document.createTextNode(article.getName)
        val articleNumber= document.createTextNode("Anzahl: 2") //db abfrage
        
        articleDiv.appendChild(articleName)
        articleDiv.appendChild(articleNumber)
        orderDiv.appendChild(articleDiv)
      }
      val orderState= document.createTextNode(order.getState)
      orderDiv.appendChild(orderState)
      content.appendChild(orderDiv)
    }
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
    clearContent()
    val content = document.getElementById("content")
    val allArticleDiv = document.createElement("div")

    val a1 = new Article(1, "M1", "D1", "N1", 20f, 12)
    val a2 = new Article(2, "M2", "D2", "N2", 2f, 12)
    val exampleArticle = List(a1, a2)
    val searchBar = document.createElement("INPUT")
    searchBar.id = "stock-search"
    searchBar.setAttribute("type","Text")
    searchBar.setAttribute("placeholder", "Suche...")
    content.appendChild(searchBar)
    for (article <- exampleArticle) {
      val articleDiv = document.createElement("div")
      val articleName = document.createElement("h3")
      articleName.innerHTML = article.getName
      val articleDescription = document.createTextNode(article.getDescription)
      val articlePrice = document.createTextNode("Preis: " + article.getPrice)
      val articleStock =
        document.createTextNode("Bestand: " + article.getStock())
      val articleImg = document.createElement("IMG")
      val alterButton = document.createElement("Button")
      alterButton.id = "alter-button" + article.getID()
      alterButton.textContent = ("Bearbeiten")
      val restockButton = document.createElement("Button")
      restockButton.id = "restock-button" + article.getID()
      restockButton.textContent = ("Lager füllen")

      articleImg.setAttribute("src", "../src/main/scala/tmp.png")
      articleImg.setAttribute("width", "50")
      articleImg.setAttribute("height", "50")
      articleDiv.appendChild(articleName)
      articleDiv.appendChild(articleImg)
      articleDiv.appendChild(articleDescription)
      articleDiv.appendChild(document.createElement("BR"))
      articleDiv.appendChild(articlePrice)
      articleDiv.appendChild(articleStock)
      articleDiv.appendChild(alterButton)
      articleDiv.appendChild(restockButton)
      allArticleDiv.appendChild(articleDiv)

      $("#alter-button" + article.getID()).click { () => println("Bearbeite") }
      $("#restock-button" + article.getID()).click { () => println("Restock") }
    }
    content.appendChild(allArticleDiv)
    val newArticleButton= document.createElement("Button")
    newArticleButton.id="new-article-button"
    newArticleButton.textContent=("Neuen Artikel hinzufügen")
    content.appendChild(newArticleButton)
     $("#new-article-button").click { () => println("Neuer Article") }
  }

  //Artikeliste
  //ID
  //Name
  //Lagerbestand
  //Preisändern
  //Anzahl neu Ordern
  //Lager füllen
  //Add new Artikel

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
