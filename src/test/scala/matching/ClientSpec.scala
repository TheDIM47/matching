package matching

import org.scalatest._

class ClientSpec extends FlatSpec with Matchers {
  "Client" should "parse client data from string" in {
    import matching.Client._
    val cli = "C1	1000	130	240	760	320".toClient
    cli.name shouldBe "C1"
    cli.amount shouldBe 1000
    cli.securities shouldBe Map('A' -> 130, 'B' -> 240, 'C' -> 760, 'D' -> 320)
  }

  it should "read minimal data from string" in {
    import matching.Client._
    val cli = "C1A1	1000".toClient
    cli.name shouldBe "C1A1"
    cli.amount shouldBe 1000
    cli.securities shouldBe Map.empty[Char, Int]
  }

  it should "serialize/deserealize to/from string" in {
    import matching.Client._
    val orig = Client("A1", 100, Map('A' -> 1, 'B' -> 2, 'C' -> 3, 'D' -> 4))
    val str = orig.toStr
    val clone = str.toClient
    clone shouldBe orig
  }

  it should "serialize/deserealize minimal client" in {
    import matching.Client._
    val orig = Client("A123", 1230, Map.empty[Char, Int])
    val str = orig.toStr
    val clone = str.toClient
    clone shouldBe orig
  }

  it should "fail on invalid securities" in {
    import matching.Client._
    assertThrows[NumberFormatException] {
      val cli = "C1	1000    abcd".toClient
    }
  }

  it should "fail on invalid amount" in {
    import matching.Client._
    assertThrows[NumberFormatException] {
      val cli = "C1	qqq1	130	240	760	320".toClient
    }
  }

  it should "fail on invalid delimiter" in {
    import matching.Client._
    assertThrows[ArrayIndexOutOfBoundsException] {
      val cli = "C1 1 130 240 760 320".toClient
    }
  }

  it should "fail on empty string" in {
    import matching.Client._
    assertThrows[ArrayIndexOutOfBoundsException] {
      val cli = "".toClient
    }
  }

  it should "read from file" in {
    import matching.Client._
    val clients = scala.io.Source
      .fromFile("src/test/resources/clients.txt")
      .getLines
      .filterNot(_.isEmpty)
      .map(_.toClient)
      .toSeq

    clients should contain theSameElementsAs Seq(
      Client("C1", 1000, Map('A' -> 130, 'B' -> 240, 'C' -> 760, 'D' -> 320)),
      Client("C3", 2760, Map('A' -> 0, 'B' -> 0, 'C' -> 0, 'D' -> 0)),
      Client("C8", 7000, Map('A' -> 90, 'B' -> 190, 'C' -> 0, 'D' -> 0))
    )
  }
}
