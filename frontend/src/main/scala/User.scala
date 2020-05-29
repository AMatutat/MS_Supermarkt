class User(
    val id: Int,
    val name: String,
    val treuepunkte: Int,
    val Adreese: String
) {
  def getTreuepunkte(): Int = 0
  def istWorker(): Boolean = false
  def getID(): Int = this.id
}
