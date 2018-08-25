package matching

import org.scalatest._

class OrderSpec extends FlatSpec with Matchers {
  "Order" should "parse buy order data from string" in {
    import matching.Order._
    val order = "C3	b	D	3	4".toOrder
    order shouldBe BuyOrder("C3", 'D', 3, 4)
  }

  it should "parse sell order data from string" in {
    import matching.Order._
    val order = "C1	s	B	6	1".toOrder
    order shouldBe SellOrder("C1", 'B', 6, 1)
  }

  it should "fail on invalid order type" in {
    import matching.Order._
    assertThrows[IllegalArgumentException] {
      val order = "C3	a	D	3	4".toOrder
    }
  }

  it should "fail on invalid price" in {
    import matching.Order._
    assertThrows[IllegalArgumentException] {
      val order = "C3	b	D	x	4".toOrder
    }
  }

  it should "fail on invalid quantity" in {
    import matching.Order._
    assertThrows[IllegalArgumentException] {
      val order = "C3	b	D	1	y".toOrder
    }
  }

  it should "fail on incomplete string" in {
    import matching.Order._
    assertThrows[IllegalArgumentException] {
      val order = "C3	b	D	1".toOrder
    }
  }

  it should "read from file" in {
    import matching.Order._
    val orders = scala.io.Source
      .fromFile("src/test/resources/orders.txt")
      .getLines
      .filterNot(_.isEmpty)
      .map(_.toOrder)
      .toSeq

    orders.length shouldBe 5
    orders should contain allOf (
      SellOrder("C2", 'C', 13, 2),
      BuyOrder("C9", 'B', 6, 4)
    )
  }
}
