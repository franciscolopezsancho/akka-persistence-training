package example

import akka.persistence.typed.scaladsl.EventSourcedBehavior
import akka.persistence.typed.scaladsl.Effect
import akka.persistence.typed.PersistenceId
import akka.actor.typed.Behavior
import akka.actor.typed.ActorRef
  
//BINDING SERIALIZER WITH THIS TRAIT
trait CborSerializable

object Box {

  //STATE
  case class Item(description: String, size: Int)
  final case class State(items: List[Item], maxCapacity: Int) {
    def addItem(item: Item): State = {
      this.copy(items = item +: items)
    }
    def roomLeft = maxCapacity - items.map(_.size).sum
  }

  object State {
    def empty(maxCapacity: Int) = State(List.empty, maxCapacity)
  }


  //COMMANDS
  sealed trait Command
  case class AddItem(
      description: String,
      size: Int,
      replyTo: ActorRef[Confirmation]
  ) extends Command

  //EVENTS
  sealed trait Event extends CborSerializable {
    def boxId: String
  }
  case class ItemAdded(boxId: String, description: String, size: Int) extends Event

  //REPLIES
  sealed trait Confirmation
  case class Accepted(roomLeft: Int) extends Confirmation
  case class Rejected(item: Item, roomLeft: Int) extends Confirmation

  def apply(boxId: String, maxCapacity: Int): Behavior[Command] = {
    EventSourcedBehavior[Command, Event, State](
      PersistenceId("Box", boxId),
      State.empty(maxCapacity),
      (state, command) => commandHandler(boxId, state, command),
      (state, event) => eventHandler(state, event)
    )
  }

  def commandHandler(boxId: String, state: State, command: Command): Effect[Event, State] = {
    command match {
      case AddItem(description, size, replyTo) => {
        if (size < state.roomLeft) {
          Effect
            .persist(ItemAdded(boxId, description, size))
            .thenRun(state => replyTo ! Accepted(state.roomLeft))
        } else {
          replyTo ! Rejected(Item(description, size), state.roomLeft)
          Effect.none
        }
      }
    }
  }
  def eventHandler(state: State, event: Event): State = {
    event match {
      case ItemAdded(boxId, description, size) => {
        state.addItem(Item(description, size))
      }
    }
  }

}
