package models.DAOs

import javax.inject.{Inject, Singleton}

import models.TicketBlock
import models.TicketBlock.TicketTable
import play.api.db.slick.DatabaseConfigProvider
import slick.lifted.TableQuery

/**
 * Created by carlos on 03/10/15.
 */
@Singleton
class TicketBlockDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends GenericCRUD[TicketTable, TicketBlock] {

  override val table = TableQuery[TicketTable]
}
