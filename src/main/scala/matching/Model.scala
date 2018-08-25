package matching

case class Client(name: String, amount: Int, securities: Map[Char, Int]) {
  def sell(s: Char, p: Int, q: Int): Client = this.copy(
    amount = this.amount + (p * q),
    securities = this.securities +
      (s -> (this.securities(s) - q))
  )

  def buy(s: Char, p: Int, q: Int): Client = this.copy(
    amount = this.amount - (p * q),
    securities = this.securities +
      (s -> (this.securities(s) + q))
  )
}

object Client {
  implicit class ClientOps(cli: Client) {
    private def toStr(m: Map[Char, Int]) =
      m.toSeq.sortBy(_._1).map(v => s"${v._2}").mkString("\t", "\t", "")

    def toStr: String = {
      s"${cli.name}\t${cli.amount}${toStr(cli.securities)}"
    }
  }

  implicit class ClientStringOps(str: String) {
    def toClient: Client = {
      val arr = str.split("\t")
      val map = arr
        .drop(2)
        .map(_.toInt)
        .zip(Stream.from('A').map(_.toChar))
        .map(_.swap)
        .toMap
      Client(arr(0), arr(1).toInt, map)
    }
  }
}

sealed trait Order {
  def client: String
  def secName: Char
  def price: Int
  def quantity: Int
}
case class BuyOrder(client: String, secName: Char, price: Int, quantity: Int)
    extends Order
case class SellOrder(client: String, secName: Char, price: Int, quantity: Int)
    extends Order

object Order {
  import scalaz.Equal._
  import scalaz.std.string._
  import scalaz.syntax.equal._

  implicit class OrderOps(str: String) {
    def toOrder: Order = {
      str.split("\t") match {
        case Array(client, otype, secName, price, quantity)
            if otype.toLowerCase === "b" =>
          BuyOrder(client, secName.head, price.toInt, quantity.toInt)
        case Array(client, otype, secName, price, quantity)
            if otype.toLowerCase === "s" =>
          SellOrder(client, secName.head, price.toInt, quantity.toInt)
        case _ =>
          throw new IllegalArgumentException(s"Invalid string format [$str]")
      }
    }
  }
}

object WorldOps {
  type OrderKey = (Int, Int)
  type OrderMap = Map[OrderKey, collection.mutable.ListBuffer[Order]]

  case class World(glasses: Map[Char, OrderMap], clients: Map[String, Client])

  import scalaz.Equal._
  import scalaz.std.anyVal._
  import scalaz.syntax.equal._

  def findPair(order: Order, orders: Iterable[Order]): Option[Order] =
    order match {
      case BuyOrder(_, _, p, q) =>
        orders.find(item =>
          item match {
            case SellOrder(_, _, p1, q1) if p1 === p && q1 === q => 
                true
            case _ => 
                false
        })
      case SellOrder(_, _, p, q) =>
        orders.find(item =>
          item match {
            case BuyOrder(_, _, p1, q1) if p1 === p && q1 === q => 
                true
            case _ => 
                false
        })
    }

  @scala.annotation.tailrec
  final def processWorld(order: Order, world: World): World = {
    world.glasses.get(order.secName) match {
      case None =>
        processWorld(
          order,
          world.copy(glasses = world.glasses + (order.secName -> Map.empty)))
      case Some(glass) =>
        val (g, cls) = {
          val key = (order.price, order.quantity)
          glass.get(key) match {
            case Some(holds) =>
              findPair(order, holds) match {
                case Some(pair) =>
                  // найдена пара
                  val (b, s) = pair match {
                    // владелец pair продает
                    case SellOrder(name, amt, p, q) =>
                      val buyer = world.clients(order.client)
                      val seller = world.clients(pair.client)
                      (buyer, seller)
                    // владелец pair покупает
                    case BuyOrder(name, amount, p, q) =>
                      val seller = world.clients(order.client)
                      val buyer = world.clients(pair.client)
                      (buyer, seller)
                  }
                  val buyer = b.buy(pair.secName, pair.price, pair.quantity)
                  val seller = s.sell(pair.secName, pair.price, pair.quantity)
                  // обновляем стакан
                  (glass + (key -> (holds - pair)), List(buyer, seller))
                case _ =>
                  (glass + (key -> (holds += order)), Nil)
              }
            case None =>
              (glass + (key -> collection.mutable.ListBuffer(order)), Nil)
          }
        }
        world.copy(
          glasses = world.glasses + (order.secName -> g),
          clients = world.clients ++ cls.map(v => (v.name -> v)).toMap
        )
    }
  }

}
