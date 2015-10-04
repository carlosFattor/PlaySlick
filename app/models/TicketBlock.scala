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
 * Created by root on 29/09/15.
 */
case class TicketBlock(
                        id: Option[UUID],
                        eventID: UUID,
                        name: String,
                        productCode: String,
                        price: BigDecimal,
                        initialSize: Int,
                        saleStart: DateTime,
                        saleEnd: DateTime)

object TicketBlock {
  implicit val format: Format[TicketBlock] = Json.format[TicketBlock]

  protected val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  import dbConfig.driver.api._

  class TicketTable(tag: Tag) extends GenericTable[TicketBlock](tag, "ticket_blocks") {
    override def id = column[UUID]("id", O.PrimaryKey, O.AutoInc)
    def eventID = column[UUID]("event_id")
    def name = column[String]("name")
    def productCode = column[String]("product_code")
    def price = column[BigDecimal]("price")
    def initialSize = column[Int]("initial_size")
    def saleStart = column[DateTime]("sale_start")
    def saleEnd = column[DateTime]("sale_end")

    def * = (id.?, eventID, name, productCode, price, initialSize, saleStart, saleEnd)<>((TicketBlock.apply _).tupled, TicketBlock.unapply)
  }

  val table = TableQuery[TicketTable]
}