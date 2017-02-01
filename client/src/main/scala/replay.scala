package client

import com.typesafe.scalalogging.LazyLogging
import scala.io.Source

object Replay extends LazyLogging{
  def go(c:Config) = {
    val lines = Source.fromFile(c.file.get).getLines

  }

}