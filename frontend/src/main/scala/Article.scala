class Article(val id: Int, val name: String, var price: Float) {
  def getName(): String = this.name
  def getID(): Int = this.id
  def getPrice(): Float = this.price
  def setPrice(newPrice: Float): Unit = {
    this.price = newPrice
    //update DB
  }
  def getLagerBestand(): Int = {
    //api call
    0
  }
  def addToLager(anzahl: Int): Unit = {
    getLagerBestand() + anzahl
  }
  def orderNachschub(anzahl: Int): Unit = {
    //api call
  }
  def getBewertungen(): Array[Bewertung] = {
    new Array[Bewertung](1)
  }

  def addBewertung(bewertung: Bewertung): Unit = {
    //ApiCall
  }

}
