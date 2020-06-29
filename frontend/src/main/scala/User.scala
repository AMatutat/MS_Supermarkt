import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.JSON

class User(
    val id: String,
    val worker: Boolean,
    var treuepunkte: Int,
    var name: String="NAME",
    var adress: String="ADRESS"
) {
  def getTreuepunkte(): Int = this.treuepunkte
  def isWorker(): Boolean = this.worker
  def setPoints(points: Int): Unit = this.treuepunkte = points
  def getID(): String = this.id
  def getName(): String = this.name
  def getAdress(): String = this.adress
  def setAdress(a: String): Unit = this.adress = a
  def setName(n: String): Unit = this.name = n

  def pushChanges(url: String): Unit = {
    val xhr = new dom.XMLHttpRequest()
    val jsonRequest =
      s""" {"id": "$id", "isWorker": $worker,"points": $treuepunkte } """
    xhr.open("POST", s"$url/alterUser", false)
    println(jsonRequest)
    xhr.setRequestHeader("Content-Type", "application/json");
    xhr.send(jsonRequest)

  }
}
