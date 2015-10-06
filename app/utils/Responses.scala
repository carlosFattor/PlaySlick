package utils

import java.util.UUID

import play.api.libs.json._

/**
 * Created by carlos on 03/10/15.
 */

case class AvailabilityResponse(result: String, ticketQuantity: Option[Long])

object AvailabilityResponse {
  implicit val responseFormat = Json.format[AvailabilityResponse]
}

case class ErrorResult(status: Int, message: String)

object ErrorResult {
  implicit val format: Format[ErrorResult] = Json.format[ErrorResult]
}

case class EndpointResponse(result: String,
                            response: JsValue,
                            error: Option[ErrorResult]) {
}

object EndpointResponse {
  implicit val format: Format[EndpointResponse] = Json.format[EndpointResponse]
}

object ErrorResponse {
  val INVALID_JSON = 1000
  val NOT_ENOUGH_TICKETS = 1001
  val TICKET_BLOCK_UNAVAILABLE = 1002

  def apply(status: Int, message: String) = {
    EndpointResponse("NOK", JsNull, Option(ErrorResult(status, message)))
  }
}

object SuccessResponse {
  def apply[A](successResponse: A)(implicit w: Writes[A]) = {
    EndpointResponse("OK", Json.toJson(successResponse), None)
  }
}

case class InsufficientTicketsAvailable(ticketBlockID: UUID,
                                        ticketsAvailable: Int) extends Throwable

class OrderRoutingException(message: String) extends Exception(message)

case class TicketBlockUnavailable(ticketBlockID: UUID) extends Throwable