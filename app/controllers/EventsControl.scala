package controllers

import java.util.UUID
import javax.inject.Inject

import models.DAOs.{TicketBlockDAO, EventDAO}
import models.{TicketBlock, Event}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import utils.{ErrorResponse, SuccessResponse}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._

/**
 * Created by carlos on 03/10/15.
 */
class EventsControl @Inject()(eventDAO: EventDAO, ticketBlockDAO: TicketBlockDAO) extends Controller {

  def list = Action.async { request =>
    val eventsFuture: Future[Seq[Event]] = eventDAO.list

    val response = eventsFuture.map{ events =>
      Ok(Json.toJson(SuccessResponse(events)))
    }

    response
  }

  def getByID(eventID: UUID) = Action.async { request =>
    val eventFuture: Future[Option[Event]] = eventDAO.getByID(eventID)
    eventFuture.map { event =>
      event.fold {
        NotFound(Json.toJson(ErrorResponse(NOT_FOUND, "No event found")))
      } { e =>
        Ok(Json.toJson(SuccessResponse(e)))
      }
    }
  }

  def create = Action.async(parse.json) { request =>
    // parse from json post body
    val incomingEvent = request.body.validate[Event]

    incomingEvent.fold(error => {
      val errorMsg = s"Invalid JSON: $error"
      val response = ErrorResponse(ErrorResponse.INVALID_JSON, errorMsg)
      Future.successful(BadRequest(Json.toJson(response)))

    }, { event =>
      val createdEvent: Future[UUID] = eventDAO.create(event)

      createdEvent.map{ uuid =>
        val eventCopy = event.copy(id = Option(uuid))
        Created(Json.toJson(SuccessResponse(eventCopy)))
      }
    })
  }

  def ticketBlocksForEvent(eventID: UUID) = Action.async {
    val eventFuture = eventDAO.getByID(eventID)

    eventFuture.flatMap { event =>
      event.fold {
        Future.successful(
          NotFound(Json.toJson(ErrorResponse(NOT_FOUND, "No event found"))))
      } { e =>
        val ticketBlocks: Future[Seq[TicketBlock]] =
          ticketBlockDAO.listForEvent(e.id.getOrElse(java.util.UUID.fromString("0")))
        ticketBlocks.map { tb =>
          Ok(Json.toJson(SuccessResponse(tb)))
        }
      }
    }
  }
}
