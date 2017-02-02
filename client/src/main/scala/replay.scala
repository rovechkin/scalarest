package client

import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import spray.client.pipelining._
import spray.http.{HttpRequest, HttpResponse}

import scala.concurrent.{Await, Future, Promise}
import scala.util.{Failure, Success, Try}
import scala.io.Source
import scala.concurrent.duration._


object Replay extends LazyLogging{
  val as = ActorSystem("ReplayAs")
  implicit val system = as
  import system.dispatcher // execution context for futures

  def go(c:Config) = {
    val sf = Source.fromFile(c.file.get).getLines.filter(!_.startsWith("#")).map{
      new UrlQuery(_,as).fetch()
    }.toList

    val r = Await.result(FutureUtil.filterFutures(sf),30.seconds)
    for(e<-r) {
      e match{
        case Success(s) =>
          logger.info(s.toString)
        case Failure(f) =>
          logger.error(f.toString)
      }
    }
  }

}

class UrlQuery(url:String, as:ActorSystem) extends LazyLogging {
  implicit val system = as
  import system.dispatcher // execution context for futures

  type Entity= HttpResponse
  def fetch():Future[Entity] ={
    logger.info(s"fetch: $url")
    val p = Promise[Entity]
    def responseFuture():Future[HttpResponse] = {
      val pipeline =
        logRequest(showRequest _) ~>
          sendReceive ~>
          logResponse(showResponse _)
      pipeline {
        logger.debug("GET: " + url)
        Get(url)
      }
    }

    def execReques = {
      responseFuture onComplete{
        case Success(s)=>
          p.success(s)
        case Failure(f) =>
          p.failure(f)
      }
    }
    Future{execReques}
    p.future
  }


  def showRequest(request: HttpRequest): Unit = {
    logger.debug(request.toString)
  }

  def showResponse(reply: HttpResponse): Unit = {
    logger.debug(reply.toString)
  }

}