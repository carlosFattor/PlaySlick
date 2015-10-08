package models

import java.util.UUID

import models.DAOs.GenericTable
import org.joda.time.DateTime
import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{Format, Json}
import utils.SlickMapping.jodaDateTimeMapping

import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
 * Created by carlos on 03/10/15.
 */
case class Event(id: Option[UUID], name: String, start: DateTime, end: DateTime,
                 address: String, city: String, state: String, country: String) {
}

object Event {
  implicit val format: Format[Event] = Json.format[Event]

  protected val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  import dbConfig.driver.api._

  class EventTable(tag: Tag) extends GenericTable[Event](tag, "events") {
    override def id = column[UUID]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def eventStart = column[DateTime]("event_start")
    def eventEnd = column[DateTime]("event_end")
    def address = column[String]("address")
    def city = column[String]("city")
    def state = column[String]("state")
    def country = column[String]("country")

    def * = (id.?, name, eventStart, eventEnd, address, city, state, country)<>((Event.apply _).tupled, Event.unapply)
  }

  val table = TableQuery[EventTable]
}
