import org.scalajs.dom
import dom.document
import org.querki.jquery._
import java.util.ArrayList

object Main {
  var user = new User(-1, true, 220)
  //var user:User = _
  //evtl auch als Object, dann kann man spaß machen wie Contains: etc.
  val shoppingcar = new ArrayList[Article]

  def main(args: Array[String]): Unit = {
    createHomePage()

  }

  def clearContent(): Unit = {
    var content = document.getElementById("content")
    while (content.firstChild != null) {
      content.removeChild(content.firstChild)
    }
  }

  def createButton(label: String, id: String): org.scalajs.dom.raw.Node = {
    val button = document.createElement("Button")
    button.textContent = label
    button.id = id
    return button
  }

  def createArticleDiv(
      article: Article,
      id: String
  ): org.scalajs.dom.raw.Node = {
    val articleDiv = document.createElement("div")
    articleDiv.id = id
    articleDiv.setAttribute("class", "article-div")
    val articleName = document.createElement("h3")
    articleName.innerHTML = article.getName
    val articleInfoDiv = document.createElement("Div")
    articleInfoDiv.id = "info-div"
    val articleDescription = document.createTextNode(article.getDescription)
    val articlePrice =
      document.createTextNode("Preis: " + article.getPrice + "€")
    val articleImg = document.createElement("IMG")
    articleImg.id = "article-img"
    articleImg.setAttribute("src", "../src/main/scala/tmp.png")
    articleImg.setAttribute("width", "80")
    articleImg.setAttribute("height", "80")
    articleDiv.appendChild(articleName)
    articleDiv.appendChild(articleImg)
    articleInfoDiv.appendChild(articleDescription)
    articleInfoDiv.appendChild(document.createElement("BR"))
    articleInfoDiv.appendChild(articlePrice)
    articleDiv.appendChild(articleInfoDiv)
    return articleDiv
  }

  def createHomePage(): Unit = {
    createNavigator()
    createArticleOverview()
  }

  def createNavigator(): Unit = {
    val userNavi = document.getElementById("navigtor")
    val userList = document.createElement("ul")

    //Home button
    val homeButton = createButton("Home", "home-button")
    val li0 = document.createElement("li")
    li0.appendChild(homeButton)

    userList.appendChild(homeButton)

    //Login bzw. logout button

    val logButton = createButton("LogIn", "log-button")
    val li1 = document.createElement("li")
    li1.appendChild(logButton)
    if (user != null) {
      logButton.textContent = ("LogOut")

      //My Orders
      val myOrders = createButton("Meine Bestellungen", "my-orders-button")
      val li2 = document.createElement("li")
      li2.appendChild(myOrders)
      userList.appendChild(li2)

      //Einkaufswagen
      val shoppingcar = createButton("Einkaufswagen", "shoppingcar-button")
      val li3 = document.createElement("li")
      li3.appendChild(shoppingcar)
      userList.appendChild(li3)

      if (user.isWorker) {

        //Lager
        val warehouse = createButton("Lager", "warehouse-button")
        val li4 = document.createElement("li")
        li4.appendChild(warehouse)
        userList.appendChild(li4)

        //Bestellungen
        val orders = createButton("Bestellung", "orders-button")
        val li5 = document.createElement("li")
        li5.appendChild(orders)
        userList.appendChild(li5)
      }

    } else
      logButton.textContent = ("LogIn")
    userList.appendChild(li1)
    userNavi.appendChild(userList)

    //Navigator-Button listener
    $("#home-button").click { () => { createArticleOverview() } }
    $("#log-button").click { () =>
      {
        if (user == null)
          createLogInPage
        else {
          user = null
          var navbar = document.getElementById("navigtor")
          while (navbar.firstChild != null) {
            navbar.removeChild(navbar.firstChild)

          }
          createHomePage()
        }
      }
    }
    $("#my-orders-button").click { () => createMyOrdersPage }
    $("#shoppingcar-button").click { () => createShoppingcarPage }
    $("#warehouse-button").click { () => createWarehousePage() }
    $("#orders-button").click { () => createOrdersPage }
  }

  def createArticleOverview(filterCat: String = null): Unit = {
    clearContent()
    val content = document.getElementById("content")

    //Kategorie Navigator
    val navDiv = document.createElement("div")
    navDiv.id = "catDiv"
    val example = List("Hase", "Tier", "Gemüse", "Obst", "käse")
    val categoryList = document.createElement("ul")
    for (cat <- example) {
      val li = document.createElement("li")
      val c = createButton(cat, "filter-" + cat)
      li.appendChild(c)
      categoryList.appendChild(li)
    }
    navDiv.appendChild(categoryList)
    content.appendChild(navDiv)
    for (cat <- example) {
      $("#filter-" + cat).click { () => createArticleOverview(cat) }
    }
    val searchBar = document.createElement("INPUT")
    searchBar.id = "article-search"
    searchBar.setAttribute("type", "Text")
    searchBar.setAttribute("placeholder", "Suche...")
    content.appendChild(searchBar)

    // GET mit Filtern
    //Später ne liste mit :Article
    val a1 = new Article(1, "M1", "D1", "N1", 20f, 12)
    val a2 = new Article(2, "M2", "D2", "N2", 2f, 12)
    val exampleArticle = List(a1, a2)

    val allArticles = document.createElement("div")
    allArticles.id = "all-article-div"
    for (article <- exampleArticle) {
      allArticles.appendChild(createArticleDiv(article, article.getName()))
    }
    content.appendChild(allArticles)
    for (article <- exampleArticle) {
      $("#" + article.getName()).click { () => createArticlePage(article) }
    }
    //Only work on enter
    $("#article-search").change { () =>
      {
        while (allArticles.firstChild != null) {
          allArticles.removeChild(allArticles.firstChild)
        }
        val exampleArticle2 = List(a1)
        for (article <- exampleArticle2) {
          allArticles.appendChild(createArticleDiv(article, article.getName()))
          $("#" + article.getName()).click { () => createArticlePage(article) }
        }

      }
    }

  }

  def createArticlePage(article: Article): Unit = {
    clearContent()

    //Article view
    var content = document.getElementById("content")

    val articleDiv = createArticleDiv(article, "Test")
    val number = document.createElement("INPUT")
    number.id = ("number")
    number.setAttribute("type", "number")
    number.setAttribute("value", "1")
    number.setAttribute("min", "1")
    number.setAttribute("max", "" + article.getStock)
    val buyButton = createButton("Add to Warenkorb", "buy-button")

    articleDiv.appendChild(number)
    articleDiv.appendChild(buyButton)
    content.appendChild(articleDiv)

    //FIXME liest nur startwerte
    $("#buy-button").click { () =>
      // add to warenkorb
      println(
        "add to warenkorb: " + document
          .getElementById("number")
          .getAttribute("value")
      )

    }

    //Write Review
    //if loged in
    if (user != null) {
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
      val sendButton = createButton("Senden", "send-button")
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

  def createMyOrdersPage(): Unit = {
    clearContent()
    var content = document.getElementById("content")

    val a1 = new Article(1, "M1", "D1", "N1", 20f, 12)
    val a2 = new Article(2, "M2", "D2", "N2", 2f, 12)
    val o1 = new Order(1, user, "On the Way")
    val o2 = new Order(2, user, "In Bearbeitung")
    var articles = List(a1, a2)
    val orders = List(o1, o2)

    for (order <- orders) {
      val orderDiv = document.createElement("div")
      //Order Datum noch anzeigen
      //GET Articles to order

      for (article <- articles) {
        val articleName = document.createTextNode(article.getName)
        val articleNumber = document.createTextNode("Anzahl: 2") //db abfrage
        orderDiv.appendChild(articleName)
        orderDiv.appendChild(articleNumber)
      }
      val orderState = document.createTextNode(order.getState)
      orderDiv.appendChild(orderState)
      content.appendChild(orderDiv)
    }
  }

  def createShoppingcarPage(): Unit = {
    clearContent()
    var content = document.getElementById("content")

    val a1 = new Article(1, "M1", "D1", "N1", 20f, 12)
    val a2 = new Article(2, "M2", "D2", "N2", 2f, 12)
    if (shoppingcar.isEmpty()) {
      shoppingcar.add(a1)
      shoppingcar.add(a2)
    }

    var summe = 0f;
    val it = shoppingcar.iterator
    while (it.hasNext()) {
      val article = it.next()
      val articleDiv = document.createElement("div")
      val img = document.createElement("IMG")
      img.setAttribute("src", "../src/main/scala/tmp.png")
      img.setAttribute("width", "50")
      img.setAttribute("height", "50")
      articleDiv.appendChild(img)
      articleDiv.appendChild(document.createTextNode(article.getName()))
      articleDiv.appendChild(
        document.createTextNode("" + article.getPrice() + "€")
      )
      summe += article.getPrice()
      val deleteButton =
        createButton("Löschen", "delete-button-" + article.getID())
      articleDiv.appendChild(deleteButton)
      content.appendChild(articleDiv)

      $("#delete-button-" + article.getID).click(() => {

        println("Delete " + article.getID())
        shoppingcar.remove(article)
        createShoppingcarPage()
      })
    }

    val usePoints = document.createElement("INPUT")
    usePoints.setAttribute("type", "Checkbox")
    usePoints.id = "use-points-box"
    content.appendChild(document.createTextNode("Treuepunkte einlösen?"))
    content.appendChild(usePoints)
    content.appendChild(document.createTextNode("Gesamtpreis: " + summe + "€"))
    val buyButton = createButton("Kaufen", "buy-button")
    content.appendChild(buyButton)
    $("#buy-button").click(() => {
      println("Gekauft")
    })

  }

  def createLogInPage: Unit = {
    clearContent()
    val content = document.getElementById("content")
    val button = createButton("Ok", "log-in-button")
    val nameField = document.createElement("INPUT")
    nameField.setAttribute("type", "text")
    nameField.id = "name-field"
    val passwordField = document.createElement("INPUT")
    passwordField.setAttribute("type", "password")
    passwordField.id = "password-field"

    val nameLabel = document.createElement("label")
    val nameT = document.createTextNode("Name:")
    val pwLabel = document.createElement("label")
    val pwT = document.createTextNode("Passwort:")
    nameLabel.appendChild(nameT)
    pwLabel.appendChild(pwT)
    content.appendChild(nameLabel)
    content.appendChild(nameField)
    content.appendChild(pwLabel)
    content.appendChild(passwordField)
    content.appendChild(button)

  }

  def createWarehousePage(filter: String = null): Unit = {
    clearContent()
    val content = document.getElementById("content")

    val a1 = new Article(1, "M1", "D1", "N1", 20f, 12)
    val a2 = new Article(2, "M2", "D2", "N2", 2f, 12)
    val exampleArticle = List(a1, a2)
    val searchBar = document.createElement("INPUT")
    searchBar.id = "stock-search"
    searchBar.setAttribute("type", "Text")
    searchBar.setAttribute("placeholder", "Suche...")
    content.appendChild(searchBar)

    val allArticles = document.createElement("div")

    for (article <- exampleArticle) {
      allArticles.appendChild(createStorageDiv(article))
      content.appendChild(allArticles)

      $("#alter-button-" + article.getID()).click { () =>
        createAlterArticlePage(article)
      }
      $("#restock-button-" + article.getID()).click { () =>
        createRestockPage(article)
      }
    }

    //content.appendChild(allArticles)
    val newArticleButton = createButton("Neuer Artikel", "new-article-button")
    content.appendChild(newArticleButton)
    $("#new-article-button").click { () => createAlterArticlePage() }

    $("#stock-search").change { () =>
      {
        while (allArticles.firstChild != null) {
          allArticles.removeChild(allArticles.firstChild)
        }
        val exampleArticle2 = List(a2)
        for (article <- exampleArticle2) {
          allArticles.appendChild(createStorageDiv(article))
          $("#alter-button-" + article.getID()).click { () =>
            createAlterArticlePage(article)
          }
          $("#restock-button-" + article.getID()).click { () =>
            createRestockPage(article)
          }
        }

      }
    }
  }

  def createStorageDiv(article: Article): org.scalajs.dom.raw.Node = {
    val articleDiv = createArticleDiv(article, "test")
    val articleStock =
      document.createTextNode("Bestand: " + article.getStock())
    val alterButton =
      createButton("Bearbeiten", "alter-button-" + article.getID())
    val restockButton =
      createButton("Auffüllen", "restock-button-" + article.getID())
    articleDiv.appendChild(articleStock)
    articleDiv.appendChild(alterButton)
    articleDiv.appendChild(restockButton)

    return articleDiv

  }

  def createOrdersPage(): Unit = {
    clearContent()
    val content = document.getElementById("content")

    val a1 = new Article(1, "M1", "D1", "N1", 20f, 12)
    val a2 = new Article(2, "M2", "D2", "N2", 2f, 12)
    val o1 = new Order(1, user, "Unterwegs")
    val o2 = new Order(2, user, "In Bearbeitung")
    var articles = List(a1, a2)
    val orders = List(o1, o2)

    for (order <- orders) {
      val orderDiv = document.createElement("div")
      orderDiv.appendChild(
        document.createTextNode("BestellNr: " + order.getID())
      )
      orderDiv.appendChild(document.createElement("BR"))
      orderDiv.appendChild(
        document.createTextNode("KundenNr: " + order.getCustomer.getID())
      )
      orderDiv.appendChild(document.createElement("BR"))
      orderDiv.appendChild(
        document.createTextNode("Kunde: " + order.getCustomer.getID())
      )
      orderDiv.appendChild(document.createElement("BR"))
      orderDiv.appendChild(
        document.createTextNode("Adresse: " + order.getCustomer.getID())
      )
      orderDiv.appendChild(document.createElement("BR"))
      orderDiv.appendChild(
        document.createTextNode("Status: " + order.state)
      )
      val moreButton = createButton("Details", "more-button-" + order.getID())
      orderDiv.appendChild(moreButton)
      content.appendChild(orderDiv)
      $("#more-button-" + order.getID()).click { () =>
        createOrderDetailsPage(order)
      }

    }

  }

  def createOrderDetailsPage(order: Order): Unit = {
    clearContent()
    val content = document.getElementById("content")

    val headerDiv = document.createElement("Div")
    headerDiv.appendChild(
      document.createTextNode("BestellNr: " + order.getID())
    )
    headerDiv.appendChild(document.createElement("BR"))
    headerDiv.appendChild(
      document.createTextNode("KundenNr: " + order.getCustomer().getID())
    )
    headerDiv.appendChild(document.createElement("BR"))
    headerDiv.appendChild(document.createTextNode("Kunde: Test Meier"))
    headerDiv.appendChild(document.createElement("BR"))
    headerDiv.appendChild(document.createTextNode("Adresse: Testweg 12"))
    headerDiv.appendChild(document.createElement("BR"))

    val comboBox = document.createElement("SELECT")
    comboBox.id = "select-box"
    val option1 = document.createElement("option")
    option1.textContent = "Unbearbeitet"
    val option2 = document.createElement("option")
    option2.textContent = "In Bearbeitung"
    val option3 = document.createElement("option")
    option3.textContent = "Unterwegs"
    val option4 = document.createElement("option")
    option4.textContent = "Ausgestellt"
    comboBox.appendChild(option1)
    comboBox.appendChild(option2)
    comboBox.appendChild(option3)
    comboBox.appendChild(option4)
    //fixme
    //comboBox.selectedIndex=2
    headerDiv.appendChild(comboBox)
    content.appendChild(headerDiv)

    val a1 = new Article(1, "M1", "D1", "N1", 20f, 12)
    val a2 = new Article(2, "M2", "D2", "N2", 2f, 12)
    var articles = List(a1, a2)
    for (article <- articles) {
      val articleBox = document.createElement("INPUT")
      articleBox.setAttribute("type", "checkbox")

      content.appendChild(
        document.createTextNode(
          "Artikel: " + article.getName() + " ID: " + article
            .getID() + " Anzahl: 2"
        )
      )
      content.appendChild(articleBox)
      content.appendChild(document.createElement("BR"))
    }
    val saveButton = createButton("Speichern", "save-button")
    content.appendChild(saveButton)

  }

  def createAlterArticlePage(
      article: Article =
        new Article(-1, "Hersteller", "Beschreibung", "Name", 0, 0)
  ): Unit = {
    clearContent()
    val content = document.getElementById("content")
    val nameField = document.createElement("INPUT")
    nameField.id = "name-field"
    nameField.setAttribute("type", "text")
    nameField.setAttribute("value", article.getName())
    content.appendChild(nameField)
    val desField = document.createElement("INPUT")
    desField.id = "des-field"
    desField.setAttribute("type", "text")
    desField.setAttribute("value", article.getDescription())
    val manField = document.createElement("INPUT")
    manField.id = "man-field"
    manField.setAttribute("type", "text")
    manField.setAttribute("value", article.getManufacture())
    val priceField = document.createElement("INPUT")
    priceField.id = "price-field"
    priceField.setAttribute("type", "number")
    priceField.setAttribute("value", "" + article.getPrice())
    val OkButton = createButton("Speichern", "alter-article-button")
    val ExitButton = createButton("Abbrechen", "exit-alter-button")
    content.appendChild(nameField)
    content.appendChild(desField)
    content.appendChild(manField)
    content.appendChild(priceField)
    content.appendChild(OkButton)
    content.appendChild(ExitButton)

    //FIXME nur startwerte werden gelesen
    //EVTL change listener an felder mit .value = wasdrinnesteht

    $("#alter-article-button").click { () =>
      article.setName(
        document.getElementById("name-field").getAttribute("value")
      )
      article.setManufacture(
        document.getElementById("man-field").getAttribute("value")
      )
      article.setPrice(
        document.getElementById("price-field").getAttribute("value").toFloat
      )
      article.setDescription(
        document.getElementById("des-field").getAttribute("value")
      )
      article.pushChanges()
    }
    $("#exit-alter-button").click { () => createWarehousePage() }

  }

  def createRestockPage(article: Article): Unit = {
    clearContent()
    val content = document.getElementById("content")
    content.appendChild(
      document.createTextNode(article.getName() + " " + article.getID())
    )
    content.appendChild(
      document.createTextNode("Auf Lager: " + article.getStock())
    )
    val number = document.createElement("Input")
    number.id = "restock-input"
    number.setAttribute("type", "number")
    content.appendChild(number)
    content.appendChild(document.createTextNode("Hinzufügen"))
    val OkButton = createButton("Ok", "restock-button")
    val exitButton = createButton("Abbrechen", "exit-restock-button")
    content.appendChild(OkButton)
    content.appendChild(exitButton)

    $("#exit-restock-button").click { () => createWarehousePage() }
    //FIXME
    $("#restock-button").click { () =>
      article.restock(
        document.getElementById("restock-input").getAttribute("value").toInt
      )
      article.pushChanges()
      createWarehousePage()
    }
  }

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
//Befehl zum compilen sbt ~fastOptJS pder sbt fullOptJS
