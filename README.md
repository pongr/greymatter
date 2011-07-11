# Grey Matter

[Apache James](http://james.apache.org) is a 100% Java-based mail server and provides a simple [Mailet API](http://james.apache.org/mailet/index.html) that you can use to write your own email-processing apps. Each Mailet subclass is instantiated once, while multiple mail spool threads can call it concurrently to process mail.

Grey Matter provides a simple Mailet trait that converts each received email into a message that is then sent to an [Akka](http://akka.io) actor. Clients simply extend the GreyMatterMailet trait, override a few methods, and then implement the rest of the mail processing app as Akka actors.

# Example

Grey Matter ships with an example LogMailet and LogActor (guess what they do...). Here are some simple commands to set up a James 3.0 mail server on EC2 that sends all incoming email through LogMailet to LogActor:

<insert example here>
