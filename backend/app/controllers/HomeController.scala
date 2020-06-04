package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._
/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  val tmpArticle= Json.obj("id"->1, "manufacutre"->"LeckerSchmecker","name"->"Bratwurst","decription"->"100% reinste Bratwurst perfekt auf den Grill oder in der Pfane." ,"price"-> 1.5f, "picture"->"tbd", "stock"->55)
  /*val tmpArticle2= Json.obj("id"->2, "manufacutre"->"Saftspaß","name"->"O-Saft","decription"->"Unsere Säfte werden aus natürlichen Früchten und ohne die Zugabe von Zucker hergestellt." ,"price"-> 2.8f, "picture"->"t", "stock"->30)
  val tmpCat1=Json.obj("id"->1,"name"->"grillgut") 
  val tmpCat2=Json.obj("id"->2,"name"->"Säfte") 
  val articleCat1=Json.obj("catid"->1,"articleid"->1)
   val articleCat2=Json.obj("catid"->2,"articleid"->2)

  val tmpReview1= Json.obj("id"->1, "text"->"Tolles produkt. Immer wieder gerne","rating"->5,"date"-"Datum","forArticle"->1, "author"->1)
  val tmpReview2= Json.obj("id"->2, "text"->"Tolles produkt. Immer wieder gerne. Versand war etwas langsam.","rating"->4,"date"-"Datum","forArticle"->1, "author"->2)
  val tmpReview3= Json.obj("id"->3, "text"->"Tolles produkt. Immer wieder gerne","rating"->5,"date"-"Datum","forArticle"->2, "author"->1)
  val tmpReview4= Json.obj("id"->4, "text"->"Tolles produkt. Immer wieder gerne. Versand war etwas langsam.","rating"->4,"date"-"Datum","forArticle"->2, "author"->2)

  val tmpUser1= Json.obj("id"->1,"points"->200,"isWorker"->true)
  val tmpUser2= Json.obj("id"->1,"points"->200,"isWorker"->false)
  val tmpUserData1 = Json.obj("userid"->1,"name"->"Hans Meier","adresse"->"Beispielweg 66")
  val tmpUserData2 = Json.obj("userid"->2,"name"->"Carla Meier","adresse"->"Beispielweg 66")


  val tmpOrder1 = Json.obj("id"->1,"state"->"Unterwegs","date"->"Heute","user"->1)
  val tmpOrder2 = Json.obj("id"->2,"state"->"In Bearbeitung","date"->"Heute","user"->1)
  val tmpOrder3 = Json.obj("id"->3,"state"->"Unbearbeitet","date"->"Heute","user"->1)
  val tmpOrder4 = Json.obj("id"->4,"state"->"Unbearbeitet","date"->"Heute","user"->2)
  val tmpOrder5 = Json.obj("id"->5,"state"->"Ausgestellt","date"->"Heute","user"-2)
  val orderArticle1= Json.obj("orderid"->1,"articleid"->1,"number"->5) 
  val orderArticle2= Json.obj("orderid"->2,"articleid"->2,"number"->2) 
  val orderArticle3= Json.obj("orderid"->3,"articleid"->2,"number"->1) 
  val orderArticle4= Json.obj("orderid"->4,"articleid"->2,"number"->3) 
  val orderArticle5= Json.obj("orderid"->5,"articleid"->1,"number"->5) 
*/
   
  
  def login(name: String, pw: String)= Action{ _ => 
    Ok(tmpArticle)
  } 

 def getAllCategorys= Action{ _ => 
    val a=List("grillgut","säfte")  
    Ok(Json.toJson(a))
  }

  def getAllArticle= Action{ _ => 

    Ok(tmpArticle)
  }
  
  def getArticleByID(id: Int) = Action{ _ => 
    Ok(tmpArticle)
  }

  def getArticle(category: String, name: String) = Action {_ =>
    Ok(tmpArticle)
  }

  def getArticleComments(id: Int)= Action{ _ => 
   Ok(tmpArticle)
  }
     
  def getCustomerByID(id: Int) = Action{ _ => 
     Ok(tmpArticle)
  }

  def getAllOrder= Action{ _ => 
       Ok(tmpArticle)
  }

  def getOrderByID(id: Int) = Action{ _ => 
     Ok(tmpArticle)
  }

  def getOrderByCustomerID(cid: Int) = Action{ _ =>   
    Ok(tmpArticle)
  }

  def newOrder = Action(parse.json) { implicit request =>
     Ok(Json.toJson(request.body))
  }

    def newComment = Action(parse.json) { implicit request => 
    Ok(Json.toJson(request.body))
  }

    def newArticle = Action(parse.json) { implicit request =>
    Ok(Json.toJson(request.body))
  }


     def updateOrder = Action(parse.json) { implicit request =>  
    Ok(Json.toJson(request.body))
  }

     def fillStock = Action(parse.json) { implicit request =>  
    Ok(Json.toJson(request.body))
  }

     def updatePrice = Action(parse.json) { implicit request =>    
    Ok(Json.toJson(request.body))
  }
}
