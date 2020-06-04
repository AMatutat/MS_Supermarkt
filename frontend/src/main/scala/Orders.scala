
class Order(
    val id: Int,
    val customer: User,
    var state: String
) {
  def getCustomer(): User = this.customer
  def getState(): String = this.state
  def getID(): Int = this.id
  def getDate():String = "tbd"
  def setStatus(state: String): Unit = this.state=state //api call ALTER

}
