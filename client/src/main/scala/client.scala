package client

import com.typesafe.scalalogging.LazyLogging


case class Config(
                   mode: Option[Config=>Unit] = None
                 )

object Client extends LazyLogging{
  def main(args: Array[String]):Unit = {
    val parser = new scopt.OptionParser[Config]("client") {
      head("client", "0.1")
      help("help") text ("prints this usage text")

    }

    parser.parse(args, Config()) match {
      case Some(config) =>
        if (config.mode.isEmpty) {
          logger.error("Invalid mode")
          parser.usage
          sys.exit(1)
        } else {
          sys.exit(0)
        }

      case None =>
        // arguments are bad, error message will have been displayed
        sys.exit(1)
    }
  }
}