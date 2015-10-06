package models.DAOs

import javax.inject.Inject

import models.Order
import models.Order.OrderTable
import play.api.db.slick.DatabaseConfigProvider
import slick.lifted.TableQuery

/**
 * Created by carlos on 03/10/15.
 */
class OrderDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends GenericCRUD[OrderTable, Order]{

  override val table = TableQuery[OrderTable]

}
