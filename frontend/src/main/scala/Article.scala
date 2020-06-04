class Article(
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
  def restock(anzahl: Int): Unit = this.stock=getStock() + anzahl
  def pushChanges():Unit = {
    println(this.name)
    println(this.description)
    println(this.price)
    println(this.manufacture)

  }
  def compare(otherArticle: Article): Boolean = {
      return this.id==otherArticle.getID()
  }
}
