# AKKA PERSISTENCE
The gist of this exercises is to get familiar with event sourcing through [akka persistence](https://doc.akka.io/docs/akka/current/typed/persistence.html)

This assumes some familiarity with Akka Typed (at least what a Behavior is). And also a vague idea of what problem event sourcing and akka persistence try to solve.

### Recommended approach
Try to read all the steps, the first paragraph probably would suffice, to get a general view of what we are going to. 
Then if you want to check your understanding, pick a step to implement, checkout its branch and try to complete it. The solution of that step you'll find it in the beginning of the following step, except for the last one that it's solution is just `HEAD` of master branch. Don't be shy to peek a solution whenever you find you could use some help. I think is more useful to do the whole thing a few times, using some help, than spend an hour hooked in a single step and find the solution by yourself. But of course, that's just my way of looking at things.

### Step 1. An empty Box
We'll start with a basic implementation that contain the necesary types and the most basic component `akka.persistence.typed.scaladsl.EventSourcedBehavior` which is the very definition of an Persistence Entity Actor. Its persistence is achieved appending its events to a journal. 

*goto: s1_an_empty_box*

In here no exercise has to be done. Just having a look at the types

Our Persistence Entity will be a Box that will be able to accept objects. Items. The definition of this type is already in the provided basic code. What you'll find in there is just the Box and its elements Commands, Events and State.

Our Box, that is our behavior or actor, is defined with the following signature.  

```   
    EventSourcedBehavior.apply[Command, Event, State](
      persistenceId: PersistenceId,
      emptyState: State,
      commandHandler: (State, Command) => Effect[Event, State],
      eventHandler: (State, Event) => State): EventSourcedBehavior[Command, Event, State]
```

1. A `PersistenceId`, composed by a value named `boxId` that we will pass when instantiating an Actor of this kind. Plus the constant String `Box`.  Bare in mind that we'll just use `boxId` as users of this entity. It's what we'll pass when creating a new Actor. Its complete id: `PersistenceId("Box",boxId)` won't be used by us along these exercises.
2. `State`. Which is the object where we'll keep the **state** of the actor
3. `(state, command) => Effect.none`, which is the **command handler**. A function that given an actor state and a message of type command will produce some Effect. Monad kind of thing I assume, not Side Effect.
4. `(state, event) => state` , is the **event handler** that will use an state and event message as an input to produce a new state.




### Step2. Create flow Command -> Event -> State

Here you just start with two methods to model the commandHandler and the eventHandler. The idea of the flow is Command -> Event -> State. It all beging by sending a **command** message `AddItem` to the Box which then will trigger an `ItemAdded` **event** that will update the `State` of the Box.

*goto: s2_flow_ces to start the exercise*

To do this you'll need to know about: 

`Effect.persist(x)`; the x here is the Event you'd like to persist and this will be done, under the hood, in two phases. Both of which have to be succesful.
    1. First the event handler has to receive this Event and react accordingly. In our case would be updating the State of the Box. Adding the item.
    2. Then it will persist in the DB of choice the event appending an entry in the journal. In our case we'll use an db in memory that can be accessed started and acceses inside a test.

    In case any of the two fail the actor we'll be stopped and when ressurrected it will keep the same state as before this message. 

`Effect.none` -> as it suggests it will no modify the state nor persist in the journal. You won't need it now but you'll do later on. 

To check you're doing fine, maybe you could `println` when state get's updated. So you may see all the items. Please bare in mind that test can stop faster that the `println` get's into the console. You may have to do something about that.

Also you'll need to add a store to keep the state of the journal. This is configuration that can be done two ways. Through an `application.conf` and then loading it on `ScalaTestWithActorTestKit(com.typesafe.config.ConfigFactory.load())` or directly on the test with a simple string such as `ScalaTestWithActorTestKit(""" as many lines of configuration as desired """)`. Maybe worth to say that these two can even be combined with `ScalaTestWithActorTestKit(com.typesafe.config.ConfigFactory.load().withFallback( """ yada yada """)`


### Step 3. Add replyTo

Let's add now the reply. When some actor send's an `AddItem` now will also have to pass a reference to an Actor such as this `AddItem` Command will reply to.

*goto: s3_reply_to to start the exercise*


Apart from that now won't be enough with just `Effect.persist` as to reply to AddItem will need to concatenate `thenRun` after the persist. I recommend to have a look to the `akka.persistence.typed.scaladsl.EffectBuilder` api to check how to.

##### some extra knowledge

Also useful to know that there's a `thenReply` instead of `thenRun` that can help to enforce replies when `EventSourcedBehavior.withEnforcedReplies[Command, Event, State] ...` is added.

If running the test you'll now see test a WARN saying you do not have snapshot store in place. For detail see 'reference.conf'. This can be found at https://doc.akka.io/docs/akka/current/general/configuration-reference.html#akka-persistence

In our case you could add `akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot-store.local"` in the BoxSpec configuration.

Another thought when testing. Take into account that if you want to check if two messages have been processed you may need to do


    probe.expectMessage([first]
    probe.expectMessage([second]
	


 or just use eventually as below.
    

    cart ! Box.AddItem("bar", probe.ref)
    cart ! Box.AddItem("foo", probe.ref)
    eventually{
      probe.expectMessage(Box.State(List(Box.Item("foo"),Box.Item("bar"))))
    }
    


### 4. Let's add a bit of logic

Now let's see how we can add a max room in the `Box` so in case we add an `Item` of size bigger than the room we have left we'll get back a `Rejected(item,roomLeft)`

*goto: s4_business_logic to start the exercise*


Maybe a good approach is to create this new bit of logic an just adjust the already existing test before dealing with Rejections, if that makes sense.

### 5. Let's add an external DB

Let's add now an external DB, and connect to it with a JDBC driver. Is worth to mention that every time a ItemAdded is persisted, this will land in a table, called journal, we will have to create. All the required documentation to do this is in here:
https://doc.akka.io/docs/akka-persistence-jdbc/3.5.2/

*goto: s5_external_db to start the exercise*


From the link above I would not create the snapshot table until required, though. 

We'll use in here MySql (for not special reason). In Step 2 we mention about two methods of configuration. From a file or from a String in the Test itself to configure the connection to the DB. Now we are going to go for the `application.conf` file option. 

You can run start the mysql db with `docker-compose up -d` from `src/test/resources` and get into it with `docker exec -it mysql-test bash`.

Last but no least you may find a problem when trying to write to disk as we still didn't add any Serialization. Have a look at https://doc.akka.io/docs/akka/current/serialization.html#usage for a general understanding
I would recommend to use Jackson though and https://doc.akka.io/docs/akka/current/serialization-jackson.html is all you need to know. I would recommend don't solve this part until you get an error of this sort.

#### hopefully everything is done

Now everything is in place you should be able to see entries in the DB such as 
`|       13 | Box|box1830352989 |               2 |       0 | NULL | 0x0A52[....] |`

This is a representation of an AddedItem event to the box with `PersistenceId`: `Box|box1830352989`.


#### Feedback

I'd be more than happy to discuss if anything doesn't make sense and can be improved. In any case, hope this helps.

Cheers!