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
