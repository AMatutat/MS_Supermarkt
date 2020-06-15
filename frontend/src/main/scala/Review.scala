import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.JSON

class Review(
    val text: String,
    val rating: Int,
    val author: User,
    val toArticle: Int
) {

  def getText: String = this.text
  def getRating: Int = this.rating
  def getUser: User = this.author
  def getArticle: Int = this.toArticle
  def pushReview(url: String): Unit = {
    val xhr = new dom.XMLHttpRequest()
    val uid = author.getID()
    val jsonRequest =
      s""" {  "text": "$text", "rating": $rating, "userID": $uid ,"articleID": $toArticle } """
    xhr.open("POST", s"$url/newComment", false)

    xhr.setRequestHeader("Content-Type", "application/json");
    xhr.onreadystatechange = { (e: dom.Event) =>
      val respons = js.JSON.parse(xhr.responseText)
      respons match {
        case json: js.Dynamic =>
          println(json)
          println(respons)
      }
    }
    xhr.send(jsonRequest)

  }

}
