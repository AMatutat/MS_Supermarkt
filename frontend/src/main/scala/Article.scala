import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.JSON

case class Article(
    val id: Int,
    var manufacture: String,
    var description: String,
    var name: String,
    var price: Float,
    var stock: Int,
) {

  def getID(): Int = this.id
  def getManufacture(): String = this.manufacture
  def getDescription(): String = this.description
  def getName(): String = this.name
  def getPrice(): Float = this.price
  def getStock(): Int = this.stock

  
  def setManufacture(man: String): Unit = this.manufacture = man
  def setPrice(newPrice: Float): Unit = this.price = newPrice
  def setName(name: String): Unit = this.name = name
  def setDescription(des: String): Unit = this.description = des
  def restock(anzahl: Int): Unit = this.stock = getStock() + anzahl
  def pushChanges(url: String): Unit = {
    var jsonRequest = ""
    var xhr = new dom.XMLHttpRequest()
    if (this.id > (-1)) {
      jsonRequest =
        s""" {  "id": $id, "price": $price,"manufacture":"$manufacture","name":"$name","description":"$description","stock": $stock} """
      xhr.open("POST", s"$url/alterArticle", false)
    } else {
      jsonRequest =
        s"""{"price": $price,"manufacture":"$manufacture","name":"$name","description":"$description","stock": $stock}"""
      xhr.open("POST", s"$url/newArticle", false)
    }

    xhr.setRequestHeader("Content-Type", "application/json");
    xhr.send(jsonRequest)

  }
  def compare(otherArticle: Article): Boolean = {
    return this.id == otherArticle.getID()
  }

}
