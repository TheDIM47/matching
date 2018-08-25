package matching

import org.scalatest._
import scalaz.zio._

class SampleAppSpec extends FlatSpec with Matchers with RTS {
  import matching.SampleApp._

  "App" should "read clients from resource file" in {
    unsafeRun(
      for {
        cli <- readClients("src/main/resources/clients.txt")
      } yield {
        cli.length shouldBe 9
      }
    )
  }

  it should "read orders from resource file" in {
    unsafeRun(
      for {
        ord <- readOrders("src/main/resources/orders.txt")
      } yield {
        ord.length shouldBe 8070
      }
    )
  }

  it should "fail with UnhandledError on file not found" in {
    assertThrows[scalaz.zio.Errors.UnhandledError] {
      unsafeRun(
        for {
          cli <- readClients("clientZZ.txt")
        } yield {
          cli.length shouldBe 3
        }
      )
    }
  }

  it should "fail with NumberFormatException on invalid clients file" in {
    assertThrows[NumberFormatException] {
      unsafeRun(
        for {
          cli <- readClients("src/test/resources/orders.txt")
        } yield {
          cli.length shouldBe 3
        }
      )
    }
  }

  it should "write and read clients to temp file" in {
      unsafeRun(
          for {
              cli <- readClients("./src/main/resources/clients.txt").map(_.toSeq)
              _   <- writeClients(cli, "tmp.txt")
              dup <- readClients("tmp.txt").map(_.toSeq)
              _   <- IO.point(new java.io.File("tmp.txt").delete())
          } yield {
              dup should contain allElementsOf cli
          }
      )
  }

}
