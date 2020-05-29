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

  val tmpArticle= Json.obj("id"->1, "manufacutre"->"M1","name"->"A1","decription"->"D1" ,"price"-> 20f, "picture"->"t", "stock"->12)
  val tmpArticle2= Json.obj("id"->2, "manufacutre"->"M2","name"->"A2","decription"->"D2" ,"price"-> 20f, "picture"->"t", "stock"->12)


   def login(name: String, pw: String)= Action{ _ => 
    Ok(tmpArticle)
  } 

 def getAllCategorys= Action{ _ => 
   println(tmpArticle)
   var r= Json.toJson(List(tmpArticle,tmpArticle2))
   println(r)
   Ok(r)
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
