package models.DAOs

import java.util.UUID
import javax.inject.{Inject, Singleton}
import actor.TicketBlockCreated
import akka.actor.ActorSelection
import models.{Order, TicketBlock}
import models.TicketBlock.TicketTable
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Akka
import slick.lifted.TableQuery
import slick.driver.PostgresDriver.api._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._
import play.api.Play.current

/**
 * Created by carlos on 03/10/15.
 */
@Singleton
class TicketBlockDAO @Inject()(orderDAO: OrderDAO, protected val dbConfigProvider: DatabaseConfigProvider)
  extends GenericCRUD[TicketTable, TicketBlock] {

  override val table = TableQuery[TicketTable]

  def availability(ticketBlockID: java.util.UUID): Future[Int] = {
    db.run {
      val query =
        sql"""SELECT initial_size - COALESCE(SUM(ticket_quantity), 0)
              FROM ticket_blocks tb
              LEFT JOIN orders o ON o.ticket_block_id = tb.id
              WHERE tb.id=(${ticketBlockID.toString} :: UUID )
              GROUP BY initial_size
           """.as[Int]

      query.headOption
    }.map {_.getOrElse(0)}
  }

  def availability2(ticketBlockID: UUID): Future[Int] = {
    val orders = for{
      o <- Order.table if o.ticketBlockID === ticketBlockID
    } yield o.ticketQuantity

    val quantityLeft = table.filter {
      _.id === ticketBlockID
    }.map {
      tb => tb.initialSize - orders.sum
    }

    Logger.info("Query: " + quantityLeft.result.headOption.statements)
    val queryResult = db.run(quantityLeft.result.headOption)

    queryResult.map { _.flatten.getOrElse(0) }
  }

  def createTicketBlock(newTicketBlock: TicketBlock): Future[TicketBlock] = {
    val insertion = (table returning table.map(_.id)) += newTicketBlock
    db.run(insertion).map { resultID =>
      val createdBlock = newTicketBlock.copy(id = Option(resultID))

      val issuer: ActorSelection =
        Akka.system.actorSelection("/user/ticketIssuer")
      issuer ! TicketBlockCreated(createdBlock)

      createdBlock
    }
  }

  def listForEvent(eventID: UUID): Future[Seq[TicketBlock]] = {
    val blockList = table.filter { tb =>
      tb.eventID === eventID
    }.result
    db.run(blockList)
  }
}
