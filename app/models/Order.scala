package models

import java.util.UUID

import models.DAOs.GenericTable
import org.joda.time.DateTime
import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{Format, Json}
import slick.driver.JdbcProfile
import utils.SlickMapping.jodaDateTimeMapping

/**
 * Created by carlos on 03/10/15.
 */
case class Order(id: Option[UUID],
                 ticketBlockID: UUID,
                 customerName: String,
                 customerEmail: String,
                 ticketQuantity: Int,
                 timestamp: Option[DateTime])

object Order {
  implicit val format: Format[Order] = Json.format[Order]

  protected val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  import dbConfig.driver.api._

  class OrderTable(tag: Tag) extends GenericTable[Order](tag, "order"){
    override def id = column[UUID]("id", O.PrimaryKey, O.AutoInc)
    def ticketBlockID = column[UUID]("ticketBlockID")
    def customerName = column[String]("customerName")
    def customerEmail = column[String]("customerEmail")
    def ticketQuantity = column[Int]("ticketQuantity")
    def timestamp = column[DateTime]("timestamp")

    def ticketBlock = foreignKey("o_ticket_tblock", ticketBlockID, TicketBlock.table)(_.id)

    def * = (id.?, ticketBlockID, customerName, customerEmail, ticketQuantity, timestamp.?) <>
      ((Order.apply _).tupled, Order.unapply)
  }
}