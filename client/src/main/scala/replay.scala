package client.replay

import client.{Config, FutureUtil}
import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import spray.client.pipelining._
import spray.http.{HttpRequest, HttpResponse}

import scala.concurrent.{Await, Future, Promise}
import scala.util.{Failure, Success}
import scala.io.Source
import scala.concurrent.duration._


object Replay extends LazyLogging{
  val as = ActorSystem("ReplayAs")
  implicit val system = as
  import system.dispatcher // execution context for futures

  def go(file:String) = {
    val sf = Source.fromFile(file).getLines.filter(!_.startsWith("#")).map{
      new UrlQuery(_,as).fetch()
    }.toList

    val r = Await.result(FutureUtil.filterFutures(sf),30.seconds)
    for(e<-r) {
      e match{
        case Success(s) =>
          println(s"****${s._1}***** \n ${s._2}")
          println(s"****End of ${s._1}*****")
        case Failure(f) =>
          f match {
            case UrlError(u, e) =>
              logger.error(s"$u : $e")
            case _ =>
              logger.error(s"$f")
          }
      }
    }
  }

}

case class UrlError(url:String, e:Throwable) extends Throwable

class UrlQuery(url:String, as:ActorSystem) extends LazyLogging {
  implicit val system = as
  import system.dispatcher // execution context for futures

  type Entity= HttpResponse
  def fetch():Future[(String,Entity)] ={
    logger.debug(s"fetch: $url")
    val p = Promise[(String,Entity)]
    def responseFuture():Future[Entity] = {
      val pipeline =
        logRequest(showRequest _) ~>
          sendReceive ~>
          logResponse(showResponse _)
      pipeline {
        logger.debug("GET: " + url)
        Get(url)
      }
    }

    def execRequest = {
      responseFuture onComplete{
        case Success(s)=>
          p.success((url,s))
        case Failure(f) =>
          p.failure(UrlError(url,f))
      }
    }
    Future{execRequest}
    p.future
  }


  def showRequest(request: HttpRequest): Unit = {
    logger.debug(request.toString)
  }

  def showResponse(reply: HttpResponse): Unit = {
    logger.debug(reply.toString)
  }

}