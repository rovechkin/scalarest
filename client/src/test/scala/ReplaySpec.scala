package client.ReplaySpec

import akka.actor.ActorSystem
import client.{Config, Replay}
import client.Replay.UrlQuery
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.util.Try

class ReplaySpec extends FlatSpec with Matchers with LazyLogging {

  val system = ActorSystem("DepthSpec")

  "ReplySpec" should "be able to query query ww.msn.com " in {
    val r = Try{Await.result(new UrlQuery("https://www.msn.com",system).fetch(), 10.seconds)}
    assert(r.isSuccess)
  }

  "ReplySpec" should "be NOT able to query query ww.zfdsfdfmsn.com " in {
    val r = Try{Await.result(new UrlQuery("https://ww.zfdsfdfmsn.com",system).fetch(), 10.seconds)}
    assert(r.isFailure)
  }
  
}