package matching

import scalaz.zio.{App, IO}

object SampleApp extends App {
  import Client._
  import Order._

  def readClients(fileName: String): IO[Exception, Iterator[Client]] =
    IO.syncException(
      scala.io.Source
        .fromFile(fileName)
        .getLines
        .filterNot(_.isEmpty)
        .map(_.toClient)
    )

  def writeClients(clients: Iterable[Client],
                   fileName: String): IO[java.io.IOException, Unit] = {
    import java.io._
    IO.now(new BufferedWriter(new FileWriter(fileName)))
      .bracket(f => IO.now({ f.flush; f.close })) { wrt =>
        IO.now(
          clients
            .toSeq
            .sortWith(_.name < _.name)
            .map(_.toStr)
            .foreach(s => {
              wrt.write(s)
              wrt.newLine
            })
        )
      }
  }

  def readOrders(fileName: String): IO[Exception, Iterator[Order]] =
    IO.syncException(
      scala.io.Source
        .fromFile(fileName)
        .getLines
        .filterNot(_.isEmpty)
        .map(_.toOrder)
    )

  def run(args: List[String]): IO[Nothing, ExitStatus] =
    process.attempt.map(_.fold(_ => 1, _ => 0)).map(ExitStatus.ExitNow(_))

  def process: IO[Exception, Unit] = {
    val clientZ = readClients("src/main/resources/clients.txt").map(_.toSeq)
    val orderZ = readOrders("src/main/resources/orders.txt")
    for {
      clients <- clientZ
      orders  <- orderZ
      clientMap = clients.map(c => c.name -> c).toMap
      start = WorldOps.World(Map.empty, clientMap)
      world = orders.foldLeft(start){ case (w, o) => WorldOps.processWorld(o, w) }
      _       <- writeClients(world.clients.values, "result.txt")
    } yield ()
  }
}
