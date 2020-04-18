package example

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike
import example.Box._
import com.typesafe.config.ConfigFactory

class BoxSpec extends ScalaTestWithActorTestKit(s"""
      akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
       """) with AnyWordSpecLike {

  "The box object" should {
    "accept Commands, transform them in events and persist" in {
      val maxCapacity = 10
      val cart = testKit.spawn(Box("abcd"))
      val probe = testKit.createTestProbe[State]()
      cart ! AddItem("bar", probe.ref)
      probe.expectMessage(State(List(Item("bar"))))
    }
  }

}
