package models.DAOs

import javax.inject.{Inject, Singleton}

import models.{TicketBlock, Event}
import models.Event.EventTable
import play.api.db.slick.DatabaseConfigProvider
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by carlos on 03/10/15.
 */
@Singleton
class EventDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends GenericCRUD[EventTable, Event] {

  override val table = TableQuery[EventTable]

}
