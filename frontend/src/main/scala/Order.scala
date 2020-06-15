import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.JSON

class Order(
    val id: Int= -1,
    val customer: User,
    var state: String,
    val date: String,
    val article: List[Article],
) {
  def getUser(): User = this.customer
  def getState(): String = this.state
  def getID(): Int = this.id
  def getDate(): String = this.date
  def getArticle: List[Article] = this.article
  def setStatus(state: String, url: String): Unit = {
    val xhr = new dom.XMLHttpRequest()
    val jsonRequest =
      s""" {  "id": $id, "state": "$state" } """
    xhr.open("POST", s"$url/updateOrder", false)

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
