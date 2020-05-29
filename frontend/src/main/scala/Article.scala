class Article(
    val id: Int,
    val manufacture: String,
    val description: String,
    val name: String,
    var price: Float,
    val stock: Int
) {

  def getName(): String = this.name
  def getID(): Int = this.id
  def getPrice(): Float = this.price
  def getDescription(): String = this.description
  def setPrice(newPrice: Float): Unit = {
    this.price = newPrice
    //update DB
  }
  def getStock(): Int = {
    //api call
    this.stock
  }
  def addToLager(anzahl: Int): Unit = {
    getStock() + anzahl
  }
  def orderNachschub(anzahl: Int): Unit = {
    //api call
  }
  def getBewertungen(): Array[Review] = {
    new Array[Review](1)
  }

  def addBewertung(bewertung: Review): Unit = {
    //ApiCall
  }

}
