class Orders(
    val id: Int,
    val customer: User,
    status: String,
    article: Array[Article]
) {
  def getCustomer(): User = null
  def getStatus(): String = "None"
  def getArticle(): Array[Article] = return article
  def setStatus(state: String): Unit = return //api call ALTER

}
