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


# Step2. Create flow Command -> Event -> State

We just added the minimal amount of code so we can try to model the commandHandler and the eventHandler as follows. The idea of the flow is Command -> Event -> State such as will send a message `AddItem` to the Box. Handling that Command would imply to check there's enough room to add the object. If so then it will trigger an `ItemAdded` event that will update the `State` of the Box.

Effect.persist(x); the x here is the Event you'd like to persist and this will be done in two phases. Both of which have to be succesful.
    1. First the event handler has to receive this Event and react accordingly. In our case would be updating the State of the Box. Adding the item.
    2. Then it will persist in the DB of choice the event appending an entry in the journal. In our case we'll use an db in memory that can be accessed started and acceses inside a test.

    In case any of the two fail the actor we'll be stopped and when ressurrected it will keep the same state as before this message. 

Effect.none -> as it suggests it will no modify the state nor persist in the journal. 

What I would suggest is to try to model the command handler and eventHandler. If you are looking as you should to the final solution. You'll see there among other things we are replying after persisting. Let's not worry about that yet.

To check you're doing fine, maybe you could `prinln` when state get's updated. So you may see all the items. Please bare in mind that test can stop faster that the println get's into the console. `Thread.sleep(1000)` would solve that.

Also you'll need to add a store to keep the journal through configuration. This can be done in application.conf addind then `ScalaTestWithActorTestKit(com.typesafe.config.ConfigFactory.load())` or directly on the test with a simple string such as `ScalaTestWithActorTestKit(""" as many lines of configuration as desired """)`. These two can even be combined with `ScalaTestWithActorTestKit(com.typesafe.config.ConfigFactory.load().withFallback( """ yada yada """)`

when `git checkout [step 3]` you'll get the solution


# Step 3. Add replyTo


## main point
Let's add now the reply. When some actor send's an AddItem now will also have to pass a reference to an Actor such as the AddItem Command will reply to.

The idea here is to add a replyTo the the AddItem and `thenRun` after the persist. I recommend to have a look to the `akka.persistence.typed.scaladsl.EffectBuilder` api to check how to.

### add ons

Also useful to know that there's a `thenReply` instead of `thenRun` that can help to enforce replies when `EventSourcedBehavior.withEnforcedReplies[Command, Event, State] ...` is added.

If running the test you'll now see test a WARN saying you do not have snapshot store in place. For detail see 'reference.conf'. This can be found at https://doc.akka.io/docs/akka/current/general/configuration-reference.html#akka-persistence

In our case you could add `akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot-store.local"` in the BoxSpec configuration.

Another thought when testing. Take into account that if you want to check if two messages have been processed you may do 
    ```probe.expectMessage([first];
       probe.expectMessage([second]`
    ```
    or just use eventually as below.
      cart ! Box.AddItem("bar", probe.ref)
      cart ! Box.AddItem("foo", probe.ref)
      eventually{
       probe.expectMessage(Box.State(List(Box.Item("foo"),Box.Item("bar"))))
      }
    ```