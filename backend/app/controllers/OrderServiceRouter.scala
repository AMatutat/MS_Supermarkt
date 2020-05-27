package controllers
import akka.actor.ActorSystem
import akka.stream.Materializer
import grpcOrder._
import grpcOrder.AbstractOrderServiceRouter
import javax.inject.Inject
import javax.inject.Singleton
import scala.concurrent.Future


/** User implementation, with support for dependency injection etc */
@Singleton
class OrderServiceRouter @Inject() (implicit actorSystem: ActorSystem)
    extends AbstractOrderServiceRouter(actorSystem) {


    override def makeOrder (in: OrderInformation): Future[OrderID]=
    Future.successful(OrderID(-1))

    override def trackOrder (in: OrderID): Future[OrderState] = 
    Future.successful(OrderState("Placeholder"))
}