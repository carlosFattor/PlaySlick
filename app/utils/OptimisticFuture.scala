package utils

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._


/**
 * Created by carlos on 08/10/15.
 */
object OptimisticFuture {

  def sequence[A](source: Seq[Future[A]]): Future[Seq[A]] = {

    val optioned = source.map { f =>
      f.map(Option.apply).recover {
        case _ => None: Option[A]
      }
    }

    Future.sequence(optioned).map(_.flatten)
  }
}
