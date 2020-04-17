package example

import akka.persistence.typed.scaladsl.EventSourcedBehavior
import akka.persistence.typed.scaladsl.Effect
import akka.persistence.typed.PersistenceId
import akka.actor.typed.Behavior

object Box {

  //STATE
  case class Item(description: String)
  final case class State(items: List[Item]) {
    def addItem(item: Item): State = {
      this.copy(items = item +: items)
    }
  }

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
      (state, command) => Effect.none,
      (state, event) => state
    )
  }

  def commandHanlder(state: State, command: Command): Effect[Event, State] = {
    command match {
      case AddItem(description) => Effect.persist(ItemAdded(description))
    }
  }
  def eventHandler(state: State, event: Event): State = {
    event match {
      case ItemAdded(description) => {
        val status = state.addItem(Item(description))
        println(status)
        status
      }
    }
  }

}
