package actor

import java.util.UUID
import javax.inject.Inject

import akka.actor.{Props, ActorRef, Actor}
import models.DAOs.{OrderDAO, TicketBlockDAO}
import models.{TicketBlock, Order}
import utils.{TicketBlockUnavailable, InsufficientTicketsAvailable, OrderRoutingException}
import akka.actor.Status.{Failure => ActorFailure}
import scala.concurrent.ExecutionContext.Implicits._

/**
 * Created by carlos on 05/10/15.
 */
class TicketIssuerWorker @Inject()(ticketBlockID: UUID, ticketBlockDAO: TicketBlockDAO, orderDAO: OrderDAO) extends Actor {

  var workers = Map[UUID, ActorRef]()

  override def preStart = {
    val ticketBlockResult = ticketBlockDAO.list

    for {
      ticketBlocks <- ticketBlockResult
      block <- ticketBlocks
      id <- block.id
    } createWorker(id)
  }

  def createWorker(ticketblockID: UUID) {
    if (!workers.contains(ticketBlockID)) {
      val worker = context.actorOf(Props(classOf[TicketIssuerWorker], ticketBlockID), name = ticketBlockID.toString)
      workers = workers + (ticketBlockID -> worker)
    }
  }

  def placeOrder(order: Order): Unit = {
    val workerRef = workers.get(order.ticketBlockID)

    workerRef.fold {
      sender ! ActorFailure(TicketBlockUnavailable(order.ticketBlockID))
    } { worker =>
      worker forward order
    }
  }

  def receive = {
    case order: Order => placeOrder(order)
    case TicketBlockCreated(t) => t.id.foreach(createWorker)
  }

}

case class TicketBlockCreated(ticketBlock: TicketBlock)