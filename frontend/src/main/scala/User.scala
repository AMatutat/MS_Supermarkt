class User(
    val id: Int,
    val isWorker: Boolean,
    var treuepunkte: Int
) {
  def getTreuepunkte(): Int = this.treuepunkte
  def istWorker(): Boolean = this.istWorker
  def getID(): Int = this.id
}
