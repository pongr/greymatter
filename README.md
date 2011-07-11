# Grey Matter

[Apache James](http://james.apache.org) is a Java-based mail server and provides a simple [Mailet API](http://james.apache.org/mailet/index.html) that you can use to write your own email-processing apps. Each Mailet subclass is instantiated once, while multiple mail spool threads can call it concurrently to process mail.

Grey Matter provides a simple Mailet trait that converts each received email into a message that is then sent to an [Akka](http://akka.io) actor, making concurrent email processing much easier. Clients simply extend the [GreyMatterMailet](https://github.com/pongr/greymatter/blob/master/src/main/scala/GreyMatterMailet.scala) trait, override a few methods, and then implement the rest of the mail processing app as Akka actors.

Currently uses Akka 1.0 & Scala 2.8.1.  Future versions will use Akka 1.1.x & Scala 2.9.0-x.

# sbt

```
val scalaToolsSnapshots = "scala-tools snapshots" at "http://scala-tools.org/repo-snapshots/"
val greyMatter = "com.pongr" %% "greymatter" % "0.1-SNAPSHOT"
```

# Example

Grey Matter ships with an example [LogMailet and LogActor](https://github.com/pongr/greymatter/blob/master/src/main/scala/LogMailet.scala) (guess what they do...). Here are some simple commands to set up a James 3.0 mail server on EC2 that sends all incoming email through LogMailet to LogActor:

(insert example here)

# License

Jetray is licensed under the [Apache 2 License](http://www.apache.org/licenses/LICENSE-2.0.txt).

