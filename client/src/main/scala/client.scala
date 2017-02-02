package client

import client.replay._
import com.typesafe.scalalogging.LazyLogging


case class Config(
                   mode: Option[Config=>Unit] = None,
                   file: Option[String]= None,
                   pair: Option[String]= None
                 )
object Client extends LazyLogging{
  def main(args: Array[String]):Unit = {
    val parser = new scopt.OptionParser[Config]("client") {
      head("client", "0.1")
      help("help") text ("prints this usage text")
      // replay urls from file returning HTML
      cmd("replay") action { (_, c) =>
        c.copy(mode = Some({c=> replay.Replay.go(c.file.get)
        })) } text("Reply URLs from file") children(
        opt[String]("file") valueName(s"<file>") text(
          "File with list of urls. One per line."
          )
          action { (x, c) => c.copy(file = Some(x)) }
      )
      // query Poloniex to get depth for BTC_ETH, returns json
      cmd("poloniex") action { (_, c) =>
        c.copy(mode = Some({c=> poloniex.Poloniex.depth(c.pair.get)
        })) } text("Query poloniex for depth") children(
        opt[String]("pair") valueName(s"<pair>") text(
          s"exchange pair: ${poloniex.Poloniex.pairs.mkString}"
          )
          action { (x, c) => c.copy(pair = Some(x)) }
        )
    }

    parser.parse(args, Config()) match {
      case Some(config) =>
        if (config.mode.isEmpty) {
          logger.error("Invalid mode")
          parser.usage
          sys.exit(1)
        } else {
          try {
            config.mode.get (config)
          } catch {
            case e: Throwable =>
              logger.error("Exception thrown in  " + config.mode.get.toString + " exception: " + e.toString)
              sys.exit(1)
          }
          sys.exit(0)
        }

      case None =>
        // arguments are bad, error message will have been displayed
        sys.exit(1)
    }
  }
}