package client

import akka.actor.ActorSystem

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object FutureUtil{
  // http://stackoverflow.com/questions/20874186/scala-listfuture-to-futurelist-disregarding-failed-futures
  def filterFutures[T](listOfFutures:Seq[Future[T]])(implicit system:ActorSystem): Future[Seq[Try[T]]] = {
    import system.dispatcher // execution context for futures
    def futureToFutureTry[T](f: Future[T]): Future[Try[T]] =
      f.map(Success(_)).recover{case x => Failure(x)}
    val listOfFutureTrys = listOfFutures.map(futureToFutureTry(_))
    val futureListOfTrys = Future.sequence(listOfFutureTrys)
    futureListOfTrys
  }
  // select only successes
  def filterFutureSuccesses[T](listOfFutures:Seq[Future[T]])(implicit system:ActorSystem): Future[Seq[Try[T]]] = {
    import system.dispatcher // execution context for futures
    filterFutures(listOfFutures).map(_.filter(_.isSuccess))
  }
  // select only failures
  def filterFutureFailures[T](listOfFutures:Seq[Future[T]])(implicit system:ActorSystem): Future[Seq[Try[T]]] = {
    import system.dispatcher // execution context for futures
    filterFutures(listOfFutures).map(_.filter(_.isFailure))
  }
}