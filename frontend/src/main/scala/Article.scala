import org.scalajs.dom
//import upickle.default.{ReadWriter => RW, macroRW}



case class Article (
    val id: Int,
    var manufacture: String,
    var description: String,
    var name: String,
    var price: Float,
    var stock: Int



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
    
    val xhr = new dom.XMLHttpRequest()
    xhr.open("POST", "TEST" + "/scategorys")

   
    xhr.onload = { (e: dom.Event) => if (xhr.status == 200) {} }
    xhr.send()
    
  }
  def compare(otherArticle: Article): Boolean = {
    return this.id == otherArticle.getID()
  }


}
