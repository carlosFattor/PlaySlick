package controllers

import play.api.mvc._
import play.api.routing.JavaScriptReverseRouter

class Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def jsRoutes = Action { implicit request =>
    Ok(
      JavaScriptReverseRouter("jsRoutes")(
        routes.javascript.TicketsControl.ticketsAvailable
      )).as("text/javascript")
  }
}
