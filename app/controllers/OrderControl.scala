package controllers

import java.util.UUID
import javax.inject.Inject
import actor.TicketIssuer
import akka.actor.Props
import akka.util.Timeout
import models.Order
import play.api.Logger
import play.api.libs.concurrent.Akka
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits._
import play.api.libs.json.Json
import play.api.mvc._
import models.DAOs.{TicketBlockDAO, OrderDAO}
import utils.{TicketBlockUnavailable, InsufficientTicketsAvailable, ErrorResponse, SuccessResponse}
import akka.pattern.ask
import scala.concurrent.Future
import play.api.Play.current

/**
 * Created by carlos on 05/10/15.
 */
class OrderControl @Inject()(orderDAO: OrderDAO, ticketBlockDAO: TicketBlockDAO) extends Controller {

  //val issuer = TicketIssuer.getSelection

  def list = Action.async { request =>
    val orders = orderDAO.list
    orders.map({ o =>
      Ok(Json.toJson(SuccessResponse(o)))
    })
  }

  def getByID(orderID: UUID) = Action.async { request =>
    val orderFuture = orderDAO.getByID(orderID)

    orderFuture.map({ order =>
      order.fold {
        NotFound(Json.toJson(ErrorResponse(NOT_FOUND, "No order found")))
      } { o =>
        Ok(Json.toJson(SuccessResponse(o)))
      }
    })
  }

  def create = Action.async(parse.json) { implicit request =>
    val incomingBody = request.body.validate[Order]

    incomingBody.fold(error => {
      val errorMSG = s"Invalid JSON: $error"
      val response = ErrorResponse(ErrorResponse.INVALID_JSON, errorMSG)
      Future.successful(BadRequest(Json.toJson(response)))
    }, { order =>

      val timeoutKey = "PlaySlick.timeouts.issuer"
      val configuredTimeout = current.configuration.getInt(timeoutKey)
      val resolvedTimeout = configuredTimeout.getOrElse(5)
      implicit val timeout = Timeout(resolvedTimeout.seconds)

      val issuer = TicketIssuer.getSelection
      val orderFuture = (issuer ? order).mapTo[Order]

      orderFuture.map { createdOrder =>
        Ok(Json.toJson(SuccessResponse(createdOrder)))
      }.recover({
        case ita: InsufficientTicketsAvailable => {
          val responseMessage =
            "There are not enough tickets remaining to complete this order." +
              s" Quantity Remaining: $ita.ticketsAvailable"

          val response = ErrorResponse(
            ErrorResponse.NOT_ENOUGH_TICKETS,
            responseMessage)

          BadRequest(Json.toJson(response))
        }
        case tba: TicketBlockUnavailable => {
          val responseMessage =
            s"Ticket Block $order.ticketBlockID is not available."
          val response = ErrorResponse(
            ErrorResponse.TICKET_BLOCK_UNAVAILABLE,
            responseMessage)

          BadRequest(Json.toJson(response))
        }
        case unexpected => {
          Logger.error(
            s"Unexpected error while placing an order: $unexpected.toString")
          val response = ErrorResponse(
            INTERNAL_SERVER_ERROR,
            "An unexpected error occurred")

          InternalServerError(Json.toJson(response))
        }
      })
    })
  }
}
