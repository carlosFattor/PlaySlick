package actor

import akka.actor.Actor
import com.google.inject.Inject
import models.DAOs.{OrderDAO, TicketBlockDAO}
import models.Order
import utils.InsufficientTicketsAvailable
import scala.concurrent.ExecutionContext.Implicits._
import akka.actor.Status.{ Failure => ActorFailure }

/**
 * Created by carlos on 05/10/15.
 */
class TicketIssuer @Inject()(ticketBlockDAO: TicketBlockDAO, orderDAO: OrderDAO) extends Actor{

  def placeOrder(order: Order): Unit ={
    val origin = sender

    val availabilityResult = ticketBlockDAO.availability(order.ticketBlockID)
    availabilityResult.map{ availability =>
      if(availability >= order.ticketQuantity){
        val createdOrderIDResult = orderDAO.create(order)
        createdOrderIDResult.map{ uuid =>
          val createdOrder = order.copy(id = Option(uuid))
          origin ! createdOrder
        }
      } else {
        val failureResponse = InsufficientTicketsAvailable(order.ticketBlockID, availability)
        origin ! ActorFailure(failureResponse)
      }
    }
  }

  def receive = {
    case order: Order => placeOrder(order)
  }
}
