class User(
    val id: String,
    val isWorker: Boolean,
    var treuepunkte: Int
) {
  def getTreuepunkte(): Int = this.treuepunkte
  def istWorker(): Boolean = this.istWorker
  def setPoints(points: Int):Unit= this.treuepunkte=points
  def getID(): String = this.id
}
