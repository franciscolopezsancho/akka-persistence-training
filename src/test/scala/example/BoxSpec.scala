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
      val cart = testKit.spawn(Box("abcd"))
      cart ! AddItem("foo")
      cart ! AddItem("bar")
    }
  }

}
