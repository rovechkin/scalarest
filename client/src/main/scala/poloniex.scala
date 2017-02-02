package poloniex
// query depth from poloniex public API

import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import spray.client.pipelining._
import spray.http.{HttpRequest, HttpResponse}
import spray.json.{DefaultJsonProtocol, JsonFormat}

import scala.concurrent.{Await, Future, Promise}
import scala.util.{Failure, Success, Try}
import concurrent.duration._
import spray.httpx.SprayJsonSupport._

case class Ask(q:Double,v:Double)
case class Bid(q:Double,v:Double)
case class Depth(asks:Seq[Ask],bids:Seq[Bid])

case class Depth1(asks:List[(String,Double)],bids:List[(String,Double)])
private object DepthJsonProtocol extends DefaultJsonProtocol {
  implicit val depthFmt = jsonFormat2(Depth1)
}

 object DepthConverter{
   private def ac(a:(String,Double)): Ask = Ask(a._1.toDouble,a._2)
   private def bc(b:(String,Double)): Bid = Bid(b._1.toDouble,b._2)

   implicit def convert(d1:Depth1):Depth = {
    Depth(d1.asks.map{ac},d1.bids.map{bc})
  }
}

object Poloniex extends LazyLogging{
  val pairs = Seq("BTC_ETH","BTC_LTC","USDT_BTC");
  def depth(pair:String):Unit ={
    if(pairs.find(_ == pair).isEmpty) throw new Exception(s"Pair: $pair is invalid")

    val as = ActorSystem("PoloniexAs")
    Try{Await.result(new DepthQuery(pair,as).fetch(),10.seconds)} match {
      case Success(s)=>
        println(s)
      case Failure(f) =>
        logger.error(f.toString)
    }
  }

  class DepthQuery(pair:String, as:ActorSystem) extends LazyLogging {
    import DepthJsonProtocol._
    implicit val system = as
    import system.dispatcher // execution context for futures
    val url = s"https://poloniex.com/public?command=returnOrderBook&currencyPair=${pair}&depth=10"

    type Entity = Depth
    def fetch() : Future[Entity] = {
      logger.debug(s"fetch: $pair")
      val p = Promise[Entity]
      def responseFuture:Future[Entity] = {
        val pipeline =
          logRequest(showRequest _) ~>
          sendReceive ~>
          logResponse(showResponse _) ~>
            unmarshal[Depth1] ~>
            DepthConverter.convert
        pipeline {
          logger.debug(s"GET: $url")
          Get(url)
        }
      }
      def execRequest = {
        responseFuture onComplete {
          case Success(s) =>
            p.success(s)
          case Failure(f) =>
            p.failure(f)
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
}