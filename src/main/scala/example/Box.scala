package example

import akka.persistence.typed.scaladsl.EventSourcedBehavior
import akka.persistence.typed.scaladsl.Effect
import akka.persistence.typed.PersistenceId
import akka.actor.typed.Behavior
import akka.actor.typed.ActorRef

object Box {

  //STATE
  case class Item(description: String)
  final case class State(items: List[Item]) {
    def addItem(item: Item): State = {
      println(s"######### adding $item to [$items]")
      this.copy(items = item +: items)
    }
  }

  object State {
    def empty = State(List.empty)
  }

  //COMMANDS
  sealed trait Command
  case class AddItem(
      description: String
  ) extends Command

  //EVENTS
  sealed trait Event {
    def boxId: String
  }
  case class ItemAdded(boxId: String, description: String) extends Event

  def apply(boxId: String): Behavior[Command] = {
    EventSourcedBehavior[Command, Event, State](
      PersistenceId("Box", boxId),
      State.empty,
      (state, command) => commandHandler(boxId, state, command),
      (state, event) => eventHandler(state, event)
    )
  }

  def commandHandler(
      boxId: String,
      state: State,
      command: Command
  ): Effect[Event, State] = {
    command match {
      case AddItem(description) => {
        Effect.persist(ItemAdded(boxId, description))
      }
    }
  }
  def eventHandler(state: State, event: Event): State = {
    event match {
      case ItemAdded(boxId, description) => {
        state.addItem(Item(description))
      }
    }
  }

}
