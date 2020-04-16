package example

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class BoxSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  "The box object" should { 
    "accept Commands, transform them in events and persist" in {
    val cart = testKit.spawn(Box(scala.util.Random.nextString(4)))
      
    cart ! Box.AddItem("bar") 
    cart ! Box.AddItem("foo") 
  }
}
}
