package models.DAOs

import java.util.UUID

import play.api.Play
import play.api.db.slick.{HasDatabaseConfig, HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile
import slick.lifted.Tag
import slick.driver.PostgresDriver.api._
import scala.concurrent.Future

/**
 * Created by Carlos on 30/09/15.
 */

abstract class GenericTable[T](tag: Tag, name: String) extends Table[T](tag, name) {
  def id = column[UUID]("id", O.PrimaryKey, O.AutoInc)
}

trait GenericCRUD[C <: GenericTable[T], T] extends HasDatabaseConfig[JdbcProfile] {

  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  import driver.api._

  protected val table: TableQuery[C]

  private val queryById = Compiled((id: Rep[UUID]) => table.filter(_.id === id))

  def list: Future[Seq[C#TableElementType]] = {
    val list = table.result
    db.run(list)
  }

  def create(c: C#TableElementType): Future[UUID] = {
    val insertion = (table returning table.map(_.id)) += c
    db.run(insertion)
  }

  def update(id: UUID, c: C#TableElementType): Future[Int] = db.run(queryById(id).update(c))

  def delete(id: UUID): Future[Int] = db.run(queryById(id).delete)

  def getByID(id: UUID): Future[Option[C#TableElementType]] = {
    db.run(queryById(id).result.headOption)
  }

  def count: Future[Int] = {
    db.run(table.length.result)
  }
}
