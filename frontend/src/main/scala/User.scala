import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.JSON

class User(
    val id: String,
    val isWorker: Boolean,
    var treuepunkte: Int,
    var name: String = "USERNAME",
    var adress: String = "USERADRESS"
) {
  def getTreuepunkte(): Int = this.treuepunkte
  def istWorker(): Boolean = this.istWorker
  def setPoints(points: Int): Unit = this.treuepunkte = points
  def getID(): String = this.id
  def getName(): String = this.name
  def getAdress(): String = this.adress
  def setAdress(a: String): Unit = this.adress = a
  def setName(n: String): Unit = this.name = n

  def pushChanges(url: String): Unit = {
    val xhr = new dom.XMLHttpRequest()
    val jsonRequest =s""" {  "id": "$id", "isWorker": $isWorker,"points": $treuepunkte } """
    xhr.open("POST", s"$url/alterUser", false)

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
