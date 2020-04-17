package example

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class BoxSpec extends ScalaTestWithActorTestKit(s"""
      akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
       """) with AnyWordSpecLike {
  "The box object" should {
    "accept Commands, transform them in events and persist" in {
      val cart = testKit.spawn(Box(scala.util.Random.nextString(4)))
      val probe = testKit.createTestProbe[Box.State]()
      cart ! Box.AddItem("bar", probe.ref)
      probe.expectMessage(Box.State(List(Box.Item("bar"))))
    }
  }
}
