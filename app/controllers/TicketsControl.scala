package controllers

import java.util.UUID
import javax.inject.Inject

import models.DAOs.TicketBlockDAO
import models.TicketBlock
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import utils.{AvailabilityResponse, ErrorResponse, SuccessResponse}
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

/**
 * Created by carlos on 03/10/15.
 */
class TicketsControl @Inject()(ticketDAO: TicketBlockDAO) extends Controller {

  def ticketsAvailable = Action { request =>
    val availableTickets = 1000
    val response = AvailabilityResponse("ok", Option(availableTickets))
    Ok(Json.toJson(response))
  }

  def list = Action.async { request =>
    val ticketBlockFuture: Future[Seq[TicketBlock]] = ticketDAO.list

    ticketBlockFuture.map { ticketBlock =>
      Ok(Json.toJson(SuccessResponse(ticketBlock)))
    }
  }

  def getByID(ticketBlockID: UUID) = Action.async { request =>
    val ticketBlockFuture: Future[Option[TicketBlock]] = ticketDAO.getByID(ticketBlockID)

    ticketBlockFuture.map { ticketBlock =>
      ticketBlock.fold {
        NotFound(Json.toJson(ErrorResponse(NOT_FOUND, "No ticket block found")))
      } { tb =>
        Ok(Json.toJson(SuccessResponse(tb)))
      }
    }
  }

  def create = Action.async(parse.json) { request =>
    val incomingBody = request.body.validate[TicketBlock]

    incomingBody.fold(error => {
      val errorMessage = s"Invalid JSON: $error"
      val response = ErrorResponse(ErrorResponse.INVALID_JSON, errorMessage)
      Future.successful(BadRequest(Json.toJson(response)))
    }, { ticketBlock =>
      // save ticket block and get a copy back
      val createdBlockFuture: Future[TicketBlock] = ticketDAO.createTicketBlock(ticketBlock)

      createdBlockFuture.map { createdBlock =>

        Created(Json.toJson(createdBlock))
      }
    })
  }
}
