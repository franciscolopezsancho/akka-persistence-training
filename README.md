# Step 1. An empty Box
### first commit with message (step 1. Empty Box with types)

The gist of this Box is to be able to accept objects as long as is not full. We'll model this with akka-persistence.

What we have so far is just the elements that compose the Box. This is the Box itself with its Commands, Events and State.

In order to create a persistence entity we'll define a Behavior. And from there the kind of Actor that will model that Persistence Entity. The kind of behavior we are creating here is already defined in `akka.persistence.typed.scaladsl.EventSourcedBehavior`
This kind of behavior is created with 
1. A `PersistenceId`, composed by `boxId` value that we will pass when instantiating an Actor of this kind. Plus the constant String `Box`.  Bare in  mind that we will manage, as  users of this entity, just  the `boxId` in order to interact with the Actor. Not the `PersistenceId("Box",boxId) no the id`. This is necesary for cases we'll not discuss in here. You can check though in CQRS example
2. State. Which is the object where we'll keep the state of the actor
3. (state, command) => Effect.none, which is the command handler. A function that given an actor state and a message of type command will produce some Effect. Monad kind of thing I assume, not Side Effect.
4. (state, event) => state , is the event handler that will use an state and event message as an input to produce a new state.

Having the following signature.  
```   
    EventSourcedBehavior.apply[Command, Event, State](
      persistenceId: PersistenceId,
      emptyState: State,
      commandHandler: (State, Command) => Effect[Event, State],
      eventHandler: (State, Event) => State): EventSourcedBehavior[Command, Event, State
```


# Step2. Create logic

We just added the minimal amount of code so we can try to model the commandHandler and the eventHandler as follows. The idea of the flow is Command -> Event -> State such as will send a message `AddItem` to the Box. Handling that Command would imply to check there's enough room to add the object. If so then it will trigger an `ItemAdded` event that will update the `State` of the Box.

Effect.persist(x); the x here is the Event you'd like to persist and this will be done in two phases. Both of which have to be succesful.
    1. First the event handler has to receive this Event and react accordingly. In our case would be updating the State of the Box. Adding the item.
    2. Then it will persist in the DB of choice the event appending an entry in the journal. In our case we'll use an db in memory that can be accessed started and acceses inside a test.

    In case any of the two fail the actor we'll be stopped and when ressurrected it will keep the same state as before this message. 

Effect.none -> as it suggests it will no modify the state nor persist in the journal. 

What I would suggest is to try to model the command handler and eventHandler. If you are looking as you should to the final solution. You'll see there among other things we are replying after persisting. Let's not worry about that yet.

To check you're doing fine, maybe you could `prinln` when state get's updated. So you may see all the items

when `git checkout [step 3]` you'll get the solution


