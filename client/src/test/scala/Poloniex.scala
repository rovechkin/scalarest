package client.PoloniexSpec

import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.{FlatSpec, Matchers}
import poloniex.Poloniex.DepthQuery

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.util.Try

class PoloniexSpec extends FlatSpec with Matchers with LazyLogging {

  val system = ActorSystem("DepthSpec")

  "PoloniexSpec" should "be able to query USDT_BTC " in {
    val r = Try{Await.result(new DepthQuery("USDT_BTC",system).fetch(), 10.seconds)}
    assert(r.isSuccess)
  }

  "PoloniexSpec" should "NOT be able to query USDT_BTC111 " in {
    val r = Try{Await.result(new DepthQuery("USDT_BTC111",system).fetch(), 10.seconds)}
    assert(r.isFailure)
  }

}