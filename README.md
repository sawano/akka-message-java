Extended message handling for Java based Akka
=============================================

A simple tool to get compile time warnings for not handling messages that you should. It can also help making your actor code more explicit and readable.
This is basically a Java take on what is exemplified in the Scala example in the "How can I get compile time errors for missing messages in receive?"
section of the Akka FAQ at http://akka.io/faq/

The basic idea is that you define a contract that specifies the messages that your actor needs to handle. By using a contract you explicitly
state what messages your actor can handle. Making this explicit statement makes your code more readable and easy to follow compared to some
if-else mess, or similar, in your `onReceive` method. The contract is in the form of an interface and thus the compiler will warn if you do
not implement the specified methods.

There are several possibilities and degrees of freedom with what you can do with this but lets show by example and start with the basics.

Basics
-------
For quick and easy use you can use one of the predefined interfaces. Say you have an actor that takes a message `SomeMessage`. By using the
predefined interface `Messages1` your actor would look something like this.

...java
public class SomeActor extends MessageDelegatingActor implements Messages1<SomeMessage> {

    @Override
    public void onMessage(SomeMessage message) {
        // do stuff ...
    }
}
...

Since the method `onMessage(SomeMessage)` is defined in the interface `Messages1` you will get a compile time error if you forget to implement
the method. The base class `MessageDelegatingActor` is merely there for convenience and you do not have to extend that if you prefer not to.

It also makes the code more readable since the handled message is more explicit when it is given its own method.

An example would be:

...java
public interface UserManager extends Messages {

    public void userRegistered(UserRegistered event);

    public void userChangedName(NameChanged event);
}

public class UserManagementActor extends MessageDelegatingActor implements UserManager {

    @Override
    public void userRegistered(UserRegistered event) {
        // Do stuff
    }

    @Override
    public void userChangedName(NameChanged event) {
        // Do other stuff
    }
}
...

Custom contracts
----------------
Instead of using the predefined interfaces that are available you can easily define your own contracts.
Please take a look at the example contract/interface `UserManager`that can be found among the test classes to se an example of how to create
a custom contract.

Other usage
------------
Please consult the javadoc.
There are also several example tests in package `se.sawano.akka.japi.messagehandling.examples` that displays several ways of usage.