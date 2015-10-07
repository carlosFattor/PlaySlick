package actor

import java.util.UUID

import akka.actor.Actor
import models.DAOs.{OrderDAO, TicketBlockDAO}
import models.Order
import akka.actor.Status.{Failure => ActorFailure}
import utils.{TicketBlockUnavailable, InsufficientTicketsAvailable, OrderRoutingException}
import scala.concurrent.ExecutionContext.Implicits._

/**
 * Created by carlos on 06/10/15.
 */
class TicketIssuerWorker(ticketBlockID: UUID, ticketBlockDAO: TicketBlockDAO, orderDAO: OrderDAO) extends Actor {

  override def preStart = {
    val availabilityFuture = ticketBlockDAO.availability(ticketBlockID)

    availabilityFuture.onSuccess {
      case result => self ! AddTickets(result)
    }
  }

  def validateRouting(requestedID: UUID) = {
    if (ticketBlockID != requestedID) {

      val msg = s"IssuerWorker #$ticketBlockID recieved " +
        s"an order for Ticket Block $requestedID"

      sender ! ActorFailure(new OrderRoutingException(msg))
      false
    } else {
      true
    }
  }

  def initializing: Actor.Receive = {
    case AddTickets(availability) => {
      context.become(normalOperation(availability))
    }
    case order: Order => {
      if (validateRouting(order.ticketBlockID)) {
        val failureResponse = TicketBlockUnavailable(
          order.ticketBlockID)

        sender ! ActorFailure(failureResponse)
      }
    }

  }

  def normalOperation(availability: Int): Actor.Receive = {
    case AddTickets(newQuantity) => {
      context.become(normalOperation(availability + newQuantity))
    }
    case order: Order => placeOrder(order, availability)
  }

  def soldOut: Actor.Receive = {
    case AddTickets(availability) => {
      context.become(normalOperation(availability))
    }
    case order: Order => {
      if (validateRouting(order.ticketBlockID)) {
        val failureResponse = InsufficientTicketsAvailable(
          order.ticketBlockID, 0)

        sender ! ActorFailure(failureResponse)
      }
    }
  }

  def placeOrder(order: Order, availability: Int): Unit = {
    val origin = sender()

    if (validateRouting(order.ticketBlockID)) {
      val msg = s"IssuerWorker #$ticketBlockID recieved " +
        s"an order for Ticket Block ${order.ticketBlockID}"
      if (validateRouting(order.ticketBlockID)) {
        if (availability >= order.ticketQuantity) {
          val newAvailability = availability - order.ticketQuantity
          context.become(normalOperation(newAvailability))

          val createdOrder = orderDAO.create(order)

          createdOrder.map(origin ! _)
        } else {
          val failureResponse = InsufficientTicketsAvailable(
            order.ticketBlockID,
            availability)

          origin ! ActorFailure(failureResponse)
        }
      }
    }
  }

  def receive = initializing
}

case class AddTickets(quantity: Int)