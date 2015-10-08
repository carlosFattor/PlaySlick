package actor

import java.util.UUID
import javax.inject.Inject
import akka.actor.Status.{Failure => ActorFailure}
import akka.actor.{Actor, ActorRef, Props}
import models.DAOs.{OrderDAO, TicketBlockDAO}
import models.{Order, TicketBlock}
import play.libs.Akka
import utils.TicketBlockUnavailable
import scala.concurrent.ExecutionContext.Implicits._


/**
 * Created by carlos on 06/10/15.
 */
class TicketIssuer @Inject()(ticketBlockDAO: TicketBlockDAO, orderDAO: OrderDAO) extends Actor{

  var workers = Map[UUID, ActorRef]()

  override def preStart = {
    val ticketBlocksResult = ticketBlockDAO.list

    for {
      ticketBlocks <- ticketBlocksResult
      block <- ticketBlocks
      id <- block.id
    } createWorker(id)
  }

  def createWorker(ticketBlockID: UUID): Unit ={
    if(!workers.contains(ticketBlockID)){
      val worker = context.actorOf(Props(classOf[TicketIssuerWorker], ticketBlockID), name = ticketBlockID.toString)
      workers = workers + (ticketBlockID -> worker)
    }
  }

  def placeOrder(order: Order)= {
    val workerRef = workers.get(order.ticketBlockID)

    workerRef.fold{
      sender ! ActorFailure(TicketBlockUnavailable(order.ticketBlockID))
    }{ worker =>
      worker forward order
    }
  }

  def checkAvailability(message: AvailabilityCheck) = {
    val workerRef = workers.get(message.eventID)

    workerRef.fold {
      sender ! ActorFailure(TicketBlockUnavailable(message.eventID))
    } { worker =>
      worker forward message
    }
  }

  def receive = {
    case order: Order => placeOrder(order)
    case a: AvailabilityCheck => checkAvailability(a)
    case TicketBlockCreated(t) => t.id.foreach(createWorker)
  }

}

case class TicketBlockCreated(ticketBlock: TicketBlock)
case class AvailabilityCheck(eventID: UUID)

object TicketIssuer {
  def props = Props[TicketIssuer]

  private val reference = Akka.system.actorOf(TicketIssuer.props, name = "ticketIssuer")

  def getSelection = Akka.system.actorSelection("/user/ticketIssuer")
}