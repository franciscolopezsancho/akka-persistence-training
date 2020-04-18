package example

import akka.persistence.typed.scaladsl.EventSourcedBehavior
import akka.persistence.typed.scaladsl.Effect
import akka.persistence.typed.PersistenceId
import akka.actor.typed.Behavior
import akka.actor.typed.ActorRef

object Box {

  case class State(value: String)
  object State {
    val empty = null
  }
  trait Command
  trait Event

  def apply(boxId: String): Behavior[Command] = {
    EventSourcedBehavior[Command, Event, State](
      PersistenceId("Box", boxId),
      State.empty,
      (state, command) => Effect.none,
      (state, event) => state
    )
  }


}
