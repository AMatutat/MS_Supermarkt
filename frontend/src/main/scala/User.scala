class User(
    val id: String,
    val isWorker: Boolean,
    var treuepunkte: Int,
    var name: String ="USERNAME",
    var adress: String = "USERADRESS"
) {
  def getTreuepunkte(): Int = this.treuepunkte
  def istWorker(): Boolean = this.istWorker
  def setPoints(points: Int):Unit= this.treuepunkte=points
  def getID(): String = this.id
  def getName():String = this.name
  def getAdress():String= this.adress
  def setAdress(a:String): Unit = this.adress=a
  def setName(n:String):Unit = this.name=n
}
