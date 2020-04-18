package example

import akka.persistence.typed.scaladsl.EventSourcedBehavior
import akka.persistence.typed.scaladsl.Effect
import akka.persistence.typed.PersistenceId
import akka.actor.typed.Behavior
import akka.actor.typed.ActorRef

object Box {

  //STATE
  case class Item(description: String)
  final case class State(items: List[Item])
  
  object State {
    val empty = State(List.empty)
  }

  //COMMANDS
  sealed trait Command
  case class AddItem(description: String) extends Command

  //EVENTS
  sealed trait Event
  case class ItemAdded(description: String) extends Event

  def apply(boxId: String): Behavior[Command] = {
    EventSourcedBehavior[Command, Event, State](
      PersistenceId("Box", boxId),
      State.empty,
      (state, command) => commandHandler(state, command),
      (state, event) => eventHandler(state, event)
    )
  }

  def commandHandler(state: State, command: Command) = ??? // what's the result type?

  def eventHandler(state: State, event: Event) = ??? // what's the result type?

}
