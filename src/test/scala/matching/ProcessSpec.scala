package matching

import org.scalatest._
import scalaz.zio._

class ProcessSpec extends FlatSpec with Matchers with RTS {
  import matching.WorldOps._

  "Processor" should "process orders and return new world" in {
    val clientMap = Seq(
      Client("A", 100, Map('A' -> 10)),
      Client("B", 100, Map('A' -> 10))
    ).map(c => c.name -> c).toMap

    val orders: Stream[Order] =
      BuyOrder("A", 'A', price = 1, quantity = 10) #::
        SellOrder("B", 'A', price = 1, quantity = 10) #:: 
        BuyOrder("A", 'B', price = 1, quantity = 10) #::
        SellOrder("C", 'C', price = 1, quantity = 10) #:: 
        Stream.empty

    val start = World(Map.empty, clientMap)
    val world = orders.foldLeft(start){ case (w, o) => processWorld(o, w) }
    world.clients.values should contain allOf (
      Client("A", 90, Map('A' -> 20)),
      Client("B", 110, Map('A' -> 0))
    )
  }
}
