import org.scalajs.dom
import dom.document
import dom.window
import org.querki.jquery._
import java.util.ArrayList
import scala.collection.mutable.HashMap
import scala.scalajs.js
import scala.scalajs.js.JSON
import play.api.libs.json._
import scala.collection.mutable.ListBuffer

object Main {
// val port = "8080"
// val backend = "http://localhost:" + port
  val backend = "http://supermarkt.dvess.network/api"

  /**
    * Aktueller User
    */
  var user = new User("1", true, 220)

  /**
    * Einkaufswagen vom aktuellen User
    */
  //val shoppingcar = new ArrayList[Article]
  //Key= Article value= anzahl
  var shoppingcar = HashMap[Article, Int]()
  def main(args: Array[String]): Unit = {
    createHomePage()

  }

  /**
    * Löscht alle Inhalte aus den Content Bereich
    */
  def clearContent(): Unit = {
    var content = document.getElementById("content")
    while (content.firstChild != null) {
      content.removeChild(content.firstChild)
    }
  }

  /**
    * Erstellt einen Button
    *
    * @param label Text im Button
    * @param id id des Button
    * @return Button Element
    */
  def createButton(label: String, id: String): org.scalajs.dom.raw.Node = {
    val button = document.createElement("Button")
    button.textContent = label
    button.id = id
    return button
  }

  /**
    * Erstellt den Standart ArtikelDiv mit Name, Beschreibung, Preis, Bild
    *
    * @param article Artikel der dargestellt werden soll
    * @param id id des Divs
    * @return Div Element
    */
  def createArticleDiv(
      article: Article,
      id: String
  ): org.scalajs.dom.raw.Node = {
    val articleDiv = document.createElement("div")
    val articleName = document.createElement("h3")
    val articleInfoDiv = document.createElement("Div")
    val articleDescription = document.createTextNode(article.getDescription)
    val articlePrice =
      document.createTextNode("Preis: " + article.getPrice + "€")
    val articleImg = document.createElement("IMG")

    articleDiv.id = id
    articleDiv.setAttribute("class", "article-div")
    articleName.innerHTML = article.getName
    articleInfoDiv.id = "info-div"
    articleImg.id = "article-img"
    articleImg.setAttribute(
      "src",
      "https://de.seaicons.com/wp-content/uploads/2015/06/Fruits-Vegetables-icon.png"
    )
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

  /**
    * Erestellt die Startseite
    */
  def createHomePage(): Unit = {
    createNavigator()
    createArticleOverview()
  }

  def createHREF(
      label: String,
      cssclass: String = null,
      link: String,
      id: String
  ): org.scalajs.dom.raw.Node = {
    val ref = document.createElement("a")
    if (cssclass != null)
      ref.setAttribute("class", cssclass)
    ref.textContent = label
    ref.setAttribute("href", link)
    ref.id = id
    return ref;

  }

  /**
    * Erstellt Navigator im oberen Bereich der Seite
    */
  def createNavigator(): Unit = {
    val userList = document.getElementById("navbar")
    //val homeButton = createButton("Home", "home-button")
    val homeButton = createHREF("Home", "nav-link", "#", "home-button")
    val logButton = createHREF("Login", "nav-link", "#", "log-button")
    val myOrders =
      createHREF("Meine Bestellungen", "nav-link", "#", "my-orders-button")
    val shoppingcar =
      createHREF("Einkaufswagen", "nav-link", "#", "shoppingcar-button")
    val warehouse = createHREF("Lager", "nav-link", "#", "warehouse-button")
    val orders =
      createHREF("Alle Bestellungen", "nav-link", "#", "orders-button")
    val portal = createHREF(
      "Portal",
      "nav-link",
      "http://portal.dvess.network/",
      "portal-button"
    )
    val li0 = document.createElement("li")
    li0.setAttribute("class", "nav-item")
    val li1 = document.createElement("li")
    li1.setAttribute("class", "nav-item")
    val li2 = document.createElement("li")
    li2.setAttribute("class", "nav-item")
    val li3 = document.createElement("li")
    li3.setAttribute("class", "nav-item")
    val li4 = document.createElement("li")
    li4.setAttribute("class", "nav-item")
    val li5 = document.createElement("li")
    li5.setAttribute("class", "nav-item")
    val li6 = document.createElement("li")
    li6.setAttribute("class", "nav-item")

    li0.appendChild(homeButton)
    userList.appendChild(li0)

    li1.appendChild(logButton)
    if (user != null) {
      logButton.textContent = ("LogOut")

      li2.appendChild(myOrders)
      userList.appendChild(li2)

      li3.appendChild(shoppingcar)
      userList.appendChild(li3)

      if (user.isWorker) {

        li4.appendChild(warehouse)
        userList.appendChild(li4)

        li5.appendChild(orders)
        userList.appendChild(li5)
      }

    } else
      logButton.textContent = ("LogIn")
    userList.appendChild(li1)
    li6.appendChild(portal)
    userList.appendChild(li6)

    //Navigator-Button listener
    $("#home-button").click { () => { createArticleOverview() } }
    $("#log-button").click { () =>
      {
        if (user == null)
          createLogInPage
        else {
          user = null
          var navbar = document.getElementById("navbar")
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
    $("#portal-button").click { () =>
      window.open("http://portal.dvess.network/", "_self")
    }

  }
  
  /**
    * Erstellt Kategorie Naviagor für die ArticleOverviewPage
    */
  def createCatNavigator(): Unit = {
    val content = document.getElementById("content")
    val navDiv = document.createElement("div")
    content.appendChild(navDiv)

    val xhr = new dom.XMLHttpRequest()
    xhr.open("GET", backend + "/categorys")
    xhr.onload = { (e: dom.Event) =>
      if (xhr.status == 200) {

        var catList = new ListBuffer[String]()
        val respons = js.JSON.parse(xhr.responseText)
        respons match {
          case jsonlist: js.Array[js.Dynamic] =>
            for (cat <- jsonlist) {
              catList += cat.name.toString
            }
        }
        val categorys = catList.toList
        val categoryList = document.createElement("ul")
        navDiv.id = "catDiv"

        //DB Abfrage
        //val example = List("Hase", "Tier", "Gemüse", "Obst", "käse")
        for (cat <- categorys) {
          val li = document.createElement("li")
          val c = createButton(cat, "filter-" + cat)
          li.appendChild(c)
          categoryList.appendChild(li)
        }
        navDiv.appendChild(categoryList)

        for (cat <- categorys) {
          $("#filter-" + cat).click { () => createArticleOverview(cat) }
        }

      } else println("ERROR createCatNavigator")
    }
    xhr.send()

  }

  /**
    * Erstellt die ArticleOverviewPage die alle Artikel anzeigt, welche die Filter kriterien erfüllen
    *
    * @param filterCat Kategorie der angezeigten Artikel
    */
  def createArticleOverview(filterCat: String = null): Unit = {
    clearContent()
    createCatNavigator()
    val xhr = new dom.XMLHttpRequest()
    if (filterCat == null)
      xhr.open("GET", backend + "/allArticle")
    else
      xhr.open("GET", backend + "/article/" + filterCat + "/_")

    xhr.onload = { (e: dom.Event) =>
      if (xhr.status == 200) {

        var articleList = new ListBuffer[Article]()
        val respons = js.JSON.parse(xhr.responseText)
        respons match {
          case jsonlist: js.Array[js.Dynamic] =>
            for (article <- jsonlist) {
              articleList += new Article(
                article.id.toString.toInt,
                article.manufacture.toString,
                article.description.toString,
                article.name.toString,
                article.price.toString.toFloat,
                article.stock.toString.toInt
              )

            }
        }
        val articles = articleList.toList
        val content = document.getElementById("content")
        val searchBar = document.createElement("INPUT")

        searchBar.id = "article-search"
        searchBar.setAttribute("type", "Text")
        searchBar.setAttribute("placeholder", "Suche...")

        content.appendChild(searchBar)

        val allArticles = document.createElement("div")
        allArticles.id = "all-article-div"
        for (article <- articles) {
          allArticles.appendChild(
            createArticleDiv(article, article.getID().toString)
          )
        }
        content.appendChild(allArticles)
        for (article <- articles) {
          $("#" + article.getID()).click { () => createArticlePage(article) }
        }

        $("#article-search").keyup { () =>
          {
            val filter = $("#article-search").value
            if (filter.equals("")) createArticleOverview(filterCat)
            else {
              while (allArticles.firstChild != null) {
                allArticles.removeChild(allArticles.firstChild)
              }

              if (filterCat == null)
                xhr.open(
                  "GET",
                  backend + "/article/" + "_/" + filter
                )
              else
                xhr.open(
                  "GET",
                  backend + "/article/" + filterCat + "/" + filter
                )

              xhr.onload = { (e: dom.Event) =>
                var articleListFilter = new ListBuffer[Article]()
                val responsFilter = js.JSON.parse(xhr.responseText)
                responsFilter match {
                  case jsonlist: js.Array[js.Dynamic] =>
                    for (article <- jsonlist) {
                      articleListFilter += new Article(
                        article.id.toString.toInt,
                        article.manufacture.toString,
                        article.description.toString,
                        article.name.toString,
                        article.price.toString.toFloat,
                        article.stock.toString.toInt
                      )

                    }
                }
                val articlesFilter = articleListFilter.toList

                for (article <- articlesFilter) {
                  allArticles.appendChild(
                    createArticleDiv(article, article.getName())
                  )
                  $("#" + article.getName()).click { () =>
                    createArticlePage(article)
                  }
                }

              }
              xhr.send()
            }
          }
        }

      }

    }
    xhr.send()
  }

  /**
    * Erstellt die Detailierte ArtikelSeite
    *
    * @param article Artikel der dargestellt werden soll
    */
  def createArticlePage(article: Article): Unit = {
    clearContent()

    //Article view
    var content = document.getElementById("content")
    val articleDiv = createArticleDiv(article, "Test")
    val number = document.createElement("INPUT")
    val buyButton = createButton("Add to Warenkorb", "buy-button")

    number.id = ("number")
    number.setAttribute("type", "number")
    number.setAttribute("value", "1")
    number.setAttribute("min", "1")
    number.setAttribute("max", "" + article.getStock)

    articleDiv.appendChild(number)
    articleDiv.appendChild(buyButton)
    content.appendChild(articleDiv)

    $("#buy-button").click { () =>
      var changes = false
      //für jeden artikel im wagen=k
      for ((k, v) <- shoppingcar) {
        if (k.compare(article)) {
          val num = v + $("#number").value().toString().toInt
          shoppingcar.remove(k)
          shoppingcar.put(article, num)
          changes = true
        }
      }
      if (!changes) {
        shoppingcar.put(article, $("#number").value().toString().toInt)
      }
    }
    //Write Review
    //if loged in
    if (user != null) {

      val writeReviewDiv = document.createElement("div")
      val textField = document.createElement("INPUT")
      val sendButton = createButton("Senden", "send-button")
      val yourRating = document.createElement("INPUT")
      textField.setAttribute("type", "text")
      textField.id = "text-field"
      yourRating.setAttribute("type", "number")
      yourRating.setAttribute("min", "1")
      yourRating.setAttribute("max", "5")
      yourRating.setAttribute("value", "3")
      yourRating.id = "your-rating"
      writeReviewDiv.appendChild(textField)
      writeReviewDiv.appendChild(yourRating)
      writeReviewDiv.appendChild(sendButton)
      content.appendChild(writeReviewDiv)

      $("#send-button").click { () =>
        val reviewText = $("#text-field").value().toString()
        val reviewRating = $("#your-rating").value()
        val r = new Review(
          reviewText,
          reviewRating.toString().toInt,
          user,
          article.getID()
        )
        r.push
        createArticlePage(article)
      }
    } //Show Reviews

    val xhr = new dom.XMLHttpRequest()
    xhr.open("GET", backend + "/articleComments/" + article.getID)

    xhr.onload = { (e: dom.Event) =>
      val allReviewDiv = document.createElement("div")

      var reviewList = new ListBuffer[Review]()
      val respons = js.JSON.parse(xhr.responseText)
      respons match {
        case jsonlist: js.Array[js.Dynamic] =>
          for (review <- jsonlist) {
            reviewList += new Review(
              review.text.toString,
              review.rating.toString.toInt,
              new User(
                review.userID.toString,
                false,
                0
              ), //isWoker und treuepunkte sind für Kommentar schreiber egal
              review.articleID.toString.toInt
            )
          }

      }

      val reviews = reviewList.toList

      for (rev <- reviews) {

        val xhrUserRequest = new dom.XMLHttpRequest()
        xhrUserRequest.open(
          "GET",
          backend + "/customerByID/" + rev.getUser.getID
        )
        xhrUserRequest.onload = { (e: dom.Event) =>
          val userRespons = js.JSON.parse(xhrUserRequest.responseText)
          userRespons match {
            case json: js.Array[js.Dynamic] =>
              for (user <- json) {
                rev.getUser.setName(user.name.toString)
                val reviewDiv = document.createElement("div")
                val text = document.createTextNode(rev.getText)
                val author =
                  document.createTextNode(
                    "  " + rev.getUser.getName() + ":     "
                  )
                val rating =
                  document.createTextNode("     " + rev.getRating + "/5")
                //val date = document.createTextNode(rev.getDate)
                // reviewDiv.appendChild(date)
                reviewDiv.appendChild(author)
                reviewDiv.appendChild(text)
                reviewDiv.appendChild(rating)
                allReviewDiv.appendChild(reviewDiv)
                allReviewDiv.appendChild(document.createElement("BR"))
              }
          }

        }
        xhrUserRequest.send()
      }
      content.appendChild(allReviewDiv)
    }
    xhr.send()
  }

  /**
    * Erstellt die Übersicht aller Bestellungen des aktuellen Users
    */
  def createMyOrdersPage(): Unit = {
    clearContent()
    var content = document.getElementById("content")

    val xhr = new dom.XMLHttpRequest()
    xhr.open("GET", backend + "/orderByCustomerID/" + user.getID)

    xhr.onload = { (e: dom.Event) =>
      var orderList = new ListBuffer[Order]()
      val respons = js.JSON.parse(xhr.responseText)

      respons match {
        case jsonlist: js.Array[js.Dynamic] =>
          for (order <- jsonlist) {
            val articleListe = new ListBuffer[Article]
            order.article match {
              case jsonlist2: js.Array[js.Dynamic] =>
                for (article <- jsonlist2) {
                  articleListe += new Article(
                    article.id.toString.toInt,
                    article.manufacture.toString,
                    article.description.toString,
                    article.name.toString,
                    article.price.toString.toFloat,
                    article.number.toString.toInt //stock als bestellanzahl verwenden
                  )
                }
            }
            orderList +=
              new Order(
                order.id.toString.toInt,
                this.user,
                order.state.toString,
                order.date.toString,
                articleListe.toList
              )
          }

      }

      val orders = orderList.toList

      for (order <- orders) {
        val orderDiv = document.createElement("div")
        val orderState = document.createTextNode(order.getState)

        //GET Articles to order
        for (article <- order.getArticle) {
          val articleName = document.createTextNode(article.getName)
          orderDiv.appendChild(articleName)
          orderDiv.appendChild(
            document.createTextNode("Anzahl: " + article.getStock)
          )
        }
        orderDiv.appendChild(
          document.createTextNode("Bestellt am: " + order.getDate())
        )
        orderDiv.appendChild(orderState)
        content.appendChild(orderDiv)
      }
    }
    xhr.send()
  }

  /**
    * Erstellt EinkaufswagenSeite vom aktuellen User
    */
  def createShoppingcarPage(): Unit = {
    clearContent()
    println(shoppingcar)
    var content = document.getElementById("content")

    var summe = 0f;
    for ((article, number) <- shoppingcar) {
      val articleDiv = document.createElement("div")
      val img = document.createElement("IMG")
      img.setAttribute(
        "src",
        "https://de.seaicons.com/wp-content/uploads/2015/06/Fruits-Vegetables-icon.png"
      )
      img.setAttribute("width", "50")
      img.setAttribute("height", "50")
      articleDiv.appendChild(img)
      articleDiv.appendChild(
        document.createTextNode(article.getName() + "       ")
      )
      articleDiv.appendChild(document.createTextNode(number + "x       "))
      articleDiv.appendChild(
        document.createTextNode(
          "Preis für alle: " + article.getPrice() * number + "€          "
        )
      )
      summe += article.getPrice() * number
      val deleteButton =
        createButton("Löschen", "delete-button-" + article.getID())
      articleDiv.appendChild(deleteButton)
      content.appendChild(articleDiv)

      $("#delete-button-" + article.getID).click(() => {
        shoppingcar.remove(article)
        createShoppingcarPage()
      })
    }

    if (!shoppingcar.isEmpty) {
      val usePoints = document.createElement("INPUT")
      val summeDiv = document.createElement("div")
      val buyButton = createButton("Kaufen", "buy-button")
      var usePointsChecked = false
      usePoints.setAttribute("type", "Checkbox")
      usePoints.id = "use-points-box"
      summeDiv.id = "summe-div"
      summeDiv.appendChild(
        document.createTextNode("Gesamtpreis: " + summe + "€")
      )
      content.appendChild(summeDiv)
      content.appendChild(document.createTextNode("Treuepunkte einlösen?"))
      content.appendChild(usePoints)
      content.appendChild(buyButton)

      $("#use-points-box").change(() => {
        var rabatt = 0
        //jeder punkt ist 1Cent wert
        if (!usePointsChecked)
          rabatt = user.getTreuepunkte() / 100

        summeDiv.removeChild(summeDiv.firstChild)
        summeDiv.appendChild(
          document.createTextNode("Gesamtpreis: " + (summe - rabatt) + "€")
        )
        usePointsChecked = (!usePointsChecked)

      })

      $("#buy-button").click(() => {
        if (usePointsChecked)
          user.setPoints(-user.getTreuepunkte())
        else
          //Für jeden Euro gibt es einen Punkt
          user.setPoints(user.getTreuepunkte() + summe.toInt)

        //API CALL

      })
    }
  }

  /**
    * Erstellt Login Page
    */
  def createLogInPage: Unit = {
    clearContent()
    val content = document.getElementById("content")
    val button = createButton("Ok", "log-in-button")
    val nameField = document.createElement("INPUT")
    val passwordField = document.createElement("INPUT")
    val nameLabel = document.createElement("label")
    val nameT = document.createTextNode("Name:")
    val pwLabel = document.createElement("label")
    val pwT = document.createTextNode("Passwort:")

    nameField.setAttribute("type", "text")
    nameField.id = "name-field"
    passwordField.setAttribute("type", "password")
    passwordField.id = "password-field"

    nameLabel.appendChild(nameT)
    pwLabel.appendChild(pwT)
    content.appendChild(nameLabel)
    content.appendChild(nameField)
    content.appendChild(pwLabel)
    content.appendChild(passwordField)
    content.appendChild(button)

    $("#login-button").click { () =>
      val email = $("#name-field").value.toString
      val pw = $("#password-field").value.toString
      println(email + "  " + pw)
    }

  }

  /**
    * Erstellt die Lagerseite
    *
    * @param filter
    */
  def createWarehousePage(): Unit = {
    clearContent()

    val content = document.getElementById("content")
    val allArticles = document.createElement("div")
    val searchBar = document.createElement("INPUT")
    val newArticleButton = createButton("Neuer Artikel", "new-article-button")

    searchBar.id = "stock-search"
    searchBar.setAttribute("type", "Text")
    searchBar.setAttribute("placeholder", "Suche...")
    content.appendChild(searchBar)

    val xhr = new dom.XMLHttpRequest()
    xhr.open("GET", backend + "/allArticle")

    xhr.onload = { (e: dom.Event) =>
      var articleList = new ListBuffer[Article]()
      val respons = js.JSON.parse(xhr.responseText)
      respons match {
        case jsonlist: js.Array[js.Dynamic] =>
          for (article <- jsonlist) {
            articleList += new Article(
              article.id.toString.toInt,
              article.manufacture.toString,
              article.description.toString,
              article.name.toString,
              article.price.toString.toFloat,
              article.stock.toString.toInt
            )

          }
      }
      val articles = articleList.toList

      for (article <- articles) {
        allArticles.appendChild(createStorageDiv(article))
        content.appendChild(allArticles)

        $("#alter-button-" + article.getID()).click { () =>
          createAlterArticlePage(article)
        }
        $("#restock-button-" + article.getID()).click { () =>
          createRestockPage(article)
        }
      }

      content.appendChild(newArticleButton)

      $("#new-article-button").click { () => createAlterArticlePage() }
      $("#stock-search").keyup { () =>
        {
          val filter = $("#stock-search").value
          if (filter.equals("")) createWarehousePage
          else {

            val xhr = new dom.XMLHttpRequest()
            xhr.open("GET", backend + "/article/_/" + filter)

            xhr.onload = { (e: dom.Event) =>
              while (allArticles.firstChild != null) {
                allArticles.removeChild(allArticles.firstChild)
              }
              var articleList = new ListBuffer[Article]()
              val respons = js.JSON.parse(xhr.responseText)
              respons match {
                case jsonlist: js.Array[js.Dynamic] =>
                  for (article <- jsonlist) {
                    articleList += new Article(
                      article.id.toString.toInt,
                      article.manufacture.toString,
                      article.description.toString,
                      article.name.toString,
                      article.price.toString.toFloat,
                      article.stock.toString.toInt
                    )

                  }
              }
              val articles = articleList.toList

              for (article <- articles) {
                allArticles.appendChild(createStorageDiv(article))

                $("#alter-button-" + article.getID()).click { () =>
                  createAlterArticlePage(article)
                }
                $("#restock-button-" + article.getID()).click { () =>
                  createRestockPage(article)
                }
              }
            }

            xhr.send()
          }
        }
      }
    }
    xhr.send()
  }

  /**
    * Erstellt ArticleDiv für die Lagerseite
    *
    * @param article Artikel der dargestellt werden soll
    * @return Div Element
    */
  def createStorageDiv(article: Article): org.scalajs.dom.raw.Node = {
    val articleDiv = createArticleDiv(article, "test")
    val articleStock = document.createTextNode("Bestand: " + article.getStock())
    val alterButton =
      createButton("Bearbeiten", "alter-button-" + article.getID())
    val restockButton =
      createButton("Auffüllen", "restock-button-" + article.getID())
    articleDiv.appendChild(articleStock)
    articleDiv.appendChild(alterButton)
    articleDiv.appendChild(restockButton)
    return articleDiv
  }

  /**
    * Erstellt die Übersichtsseite aller Bestellungen
    */
  def createOrdersPage(): Unit = {
    clearContent()

    val content = document.getElementById("content")
    val xhr = new dom.XMLHttpRequest()
    xhr.open("GET", backend + "/allOrder")

    xhr.onload = { (e: dom.Event) =>
      var orderList = new ListBuffer[Order]()
      val respons = js.JSON.parse(xhr.responseText)

      respons match {
        case jsonlist: js.Array[js.Dynamic] =>
          for (order <- jsonlist) {
            val articleListe = new ListBuffer[Article]
            order.article match {
              case jsonlist2: js.Array[js.Dynamic] =>
                for (article <- jsonlist2) {
                  articleListe += new Article(
                    article.id.toString.toInt,
                    article.manufacture.toString,
                    article.description.toString,
                    article.name.toString,
                    article.price.toString.toFloat,
                    article.number.toString.toInt //stock als bestellanzahl verwenden
                  )
                }
            }
            orderList +=
              new Order(
                order.id.toString.toInt,
                new User(order.userID.toString, false, 0),
                order.state.toString,
                order.date.toString,
                articleListe.toList
              )
          }

      }

      val orders = orderList.toList

      for (order <- orders) {
        val xhrUserRequest = new dom.XMLHttpRequest()
        xhrUserRequest.open(
          "GET",
          backend + "/customerByID/" + order.getUser.getID
        )
        xhrUserRequest.onload = { (e: dom.Event) =>
          val userRespons = js.JSON.parse(xhrUserRequest.responseText)
          userRespons match {
            case json: js.Array[js.Dynamic] =>
              for (user <- json) {
                order.getUser.setName(user.name.toString)
                order.getUser.setAdress(user.adresse.toString)

                val orderDiv = document.createElement("div")
                val moreButton =
                  createButton("Details", "more-button-" + order.getID())
                orderDiv.appendChild(
                  document.createTextNode("BestellNr: " + order.getID())
                )
                orderDiv.appendChild(document.createElement("BR"))
                orderDiv.appendChild(
                  document.createTextNode("KundenNr: " + order.getUser.getID())
                )
                orderDiv.appendChild(document.createElement("BR"))
                orderDiv.appendChild(
                  document.createTextNode("Kunde: " + order.getUser.getName)
                )
                orderDiv.appendChild(document.createElement("BR"))
                orderDiv.appendChild(
                  document.createTextNode("Adresse: " + order.getUser.getAdress)
                )
                orderDiv.appendChild(document.createElement("BR"))
                orderDiv.appendChild(
                  document.createTextNode("Status: " + order.getState)
                )
                orderDiv.appendChild(moreButton)
                content.appendChild(orderDiv)

                $("#more-button-" + order.getID()).click { () =>
                  createOrderDetailsPage(order)
                }
              }
          }

        }
        xhrUserRequest.send()
      }

    }
    xhr.send()
  }

  /**
    * Erstellt die detailierte Ansicht einer Bestellung
    *
    * @param order Bestellung die dargestellt werden soll
    */
  def createOrderDetailsPage(order: Order): Unit = {
    clearContent()

    val content = document.getElementById("content")
    val headerDiv = document.createElement("Div")
    val comboBox = document.createElement("SELECT")
    val option1 = document.createElement("option")
    val option2 = document.createElement("option")
    val option3 = document.createElement("option")
    val option4 = document.createElement("option")

    comboBox.id = "select-box"
    option1.textContent = "Unbearbeitet"
    option2.textContent = "In Bearbeitung"
    option3.textContent = "Unterwegs"
    option4.textContent = "Ausgestellt"
    comboBox.appendChild(option1)
    comboBox.appendChild(option2)
    comboBox.appendChild(option3)
    comboBox.appendChild(option4)

    headerDiv.appendChild(
      document.createTextNode("BestellNr: " + order.getID())
    )
    headerDiv.appendChild(document.createElement("BR"))
    headerDiv.appendChild(
      document.createTextNode("KundenNr: " + order.getUser().getID())
    )
    headerDiv.appendChild(document.createElement("BR"))
    headerDiv.appendChild(document.createTextNode("Kunde: "+order.getUser.getName))
    headerDiv.appendChild(document.createElement("BR"))
    headerDiv.appendChild(document.createTextNode("Adresse: "++order.getUser.getAdress))
    headerDiv.appendChild(document.createElement("BR"))
    content.appendChild(headerDiv)

    var articles = order.getArticle

    for (article <- articles) {
      val articleBox = document.createElement("INPUT")
      articleBox.setAttribute("type", "checkbox")
      content.appendChild(
        document.createTextNode(
          "Artikel: " + article.getName + "   ID: " + article.getID + "   Anzahl:    " + article.getStock
        )
      )
      content.appendChild(articleBox)
      content.appendChild(document.createElement("BR"))
    }

    headerDiv.appendChild(comboBox)

    $("#select-box").change(() => {
      order.setStatus($("#select-box").value().toString())
    })

  }

  /**
    * Erstellt die "Artikel bearbeiten/ Neuen Artikel hinzufügen" Seite
    *
    * @param article Artikel welcher bearbeitet werden soll. Empty wenn ein neuer Artikel angelegt werden soll
    */
  def createAlterArticlePage(
      article: Article =
        new Article(-1, "Hersteller", "Beschreibung", "Name", 0, 0)
  ): Unit = {
    clearContent()

    val content = document.getElementById("content")
    val nameField = document.createElement("INPUT")
    val desField = document.createElement("INPUT")
    val manField = document.createElement("INPUT")
    val priceField = document.createElement("INPUT")
    val okButton = createButton("Speichern", "alter-article-button")
    val exitButton = createButton("Abbrechen", "exit-alter-button")
    val catSelection = document.createElement("SELECT")

    nameField.id = "name-field"
    nameField.setAttribute("type", "text")
    nameField.setAttribute("value", article.getName())
    desField.id = "des-field"
    desField.setAttribute("type", "text")
    desField.setAttribute("value", article.getDescription())
    manField.id = "man-field"
    manField.setAttribute("type", "text")
    manField.setAttribute("value", article.getManufacture())
    priceField.id = "price-field"
    priceField.setAttribute("type", "number")
    priceField.setAttribute("value", "" + article.getPrice())

    content.appendChild(nameField)
    content.appendChild(desField)
    content.appendChild(manField)
    content.appendChild(priceField)
    content.appendChild(catSelection)
    content.appendChild(okButton)
    content.appendChild(exitButton)

    $("#alter-article-button").click { () =>
      article.setDescription($("#des-field").value().toString())
      article.setManufacture($("#man-field").value().toString())
      article.setName($("#name-field").value().toString())
      article.setPrice($("#price-field").value().toString().toFloat)
      article.pushChanges(backend)
    }
    $("#exit-alter-button").click { () => createWarehousePage() }

  }

  /**
    * Erstellt die Auffüllseite für einen Artikel
    *
    * @param article Artikel der aufgefüllt werden soll
    */
  def createRestockPage(article: Article): Unit = {
    clearContent()
    val content = document.getElementById("content")
    val number = document.createElement("Input")
    val okButton = createButton("Ok", "restock-button")
    val exitButton = createButton("Abbrechen", "exit-restock-button")

    number.id = "restock-input"
    number.setAttribute("type", "number")

    content.appendChild(
      document.createTextNode(article.getName() + "    ID:" + article.getID())
    )
    content.appendChild(
      document.createTextNode("    Auf Lager: " + article.getStock())
    )
    content.appendChild(number)
    content.appendChild(document.createTextNode("Hinzufügen"))
    content.appendChild(okButton)
    content.appendChild(exitButton)

    $("#exit-restock-button").click { () => createWarehousePage() }

    $("#restock-button").click { () =>
      article.restock($("#restock-input").value().toString().toInt)
      createWarehousePage()
    }
  }

}

//Befehl zum compilen sbt ~fastOptJS oder sbt fullOptJS
