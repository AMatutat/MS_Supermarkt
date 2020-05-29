class Order(
    val id: Int,
    val customer: User,
    var state: String
) {
  def getCustomer(): User = this.customer
  def getState(): String = this.state
  def getID(): Int = this.id
  def setStatus(state: String): Unit = return //api call ALTER

}
