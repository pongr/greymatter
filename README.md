# Grey Matter

[Apache James](http://james.apache.org) is a Java-based mail server and provides a simple [Mailet API](http://james.apache.org/mailet/index.html) that you can use to write your own email-processing apps. Each Mailet subclass is instantiated once, while multiple mail spool threads can call it concurrently to process mail.

Grey Matter provides a simple Mailet trait that converts each received email into a message that is then sent to an [Akka](http://akka.io) actor, making concurrent email processing much easier. Clients simply extend the [GreyMatterMailet](https://github.com/pongr/greymatter/blob/master/src/main/scala/GreyMatterMailet.scala) trait, override a few methods, and then implement the rest of the mail processing app as Akka actors.

This simple example creates a mailet that wraps every Mail in a MyMessage, which will get sent to the MyActor:

``` scala
class ExampleMailet extends GreyMatterMailet {
  def newActor = actorOf[MyActor].start
  def messageFor(mail: Mail) = MyMessage(mail)
}
```

Currently uses Akka 1.0 & Scala 2.8.1.  Future versions will use Akka 1.x & Scala 2.9.x. Grey Matter has only been tested with James 3.0, but may very well work with James 2.3.x.

# sbt

``` scala
//Grey Matter is available at http://scala-tools.org/repo-releases/
val greyMatter = "com.pongr" %% "greymatter" % "0.8"
```

# Example

Grey Matter ships with an example [LogMailet and LogActor](https://github.com/pongr/greymatter/blob/master/src/main/scala/LogMailet.scala) (guess what they do...). Here are some simple commands to [set up a James 3.0 mail server](http://james.apache.org/server/3/quick-start.html) on EC2 that sends all incoming email through LogMailet to LogActor:

```
#Run an ec2 instance with port 25 open to 0.0.0.0/0 (for SMTP) and port 22 open to your IP (for SSH), for example:
ec2run ami-1aad5273 -k your-key -t m1.large -g your-smtp-group -g your-ssh-group

#Set up your DNS like this:
 - your.domain.com MX 10 mail.your.domain.com
 - mail.your.domain.com A <public IP of EC2 instance>

#SSH in to your new EC2 instance and perform all of the below:

sudo apt-get update

#32-bit instance
sudo apt-get install -y libc6 libc6-dev default-jre-headless

#64-bit instance
sudo apt-get install -y libc6 libc6-dev libc6-i386 libc6-dev-i386 default-jre-headless

#Download James and set it up at /usr/local/james
cd /usr/local
sudo wget http://apache.mirrors.tds.net//james/server/james-server-container-spring-3.0-M2-bin.tar.gz
sudo tar xvzf james-server-container-spring-3.0-M2-bin.tar.gz
sudo rm james-server-container-spring-3.0-M2-bin.tar.gz
sudo ln -s james-server-container-spring-3.0-M2 james

#In /usr/local/james/conf, set enabled="false" in imapserver.xml, pop3server.xml & remotemanager.xml

#In /usr/local/james/conf/domainlist.xml:
 - <domainname>your.domain.com</domainname>
 - <autodetect>false</autodetect>
 - <autodetectIP>false</autodetectIP>
 
#In /usr/local/james/conf/smtpserver.xml:
 - Comment out: <!-- <handler class="org.apache.james.smtpserver.fastfail.ValidRcptHandler"/> -->
 
#In /usr/local/james/conf/log4j.properties:
 - Add FILE to: log4j.rootLogger=DEBUG, FILE
 
#In /usr/local/james/conf/mailetcontainer.xml:
 - Add to <mailetpackages>: <mailetpackage>com.pongr.greymatter.example</mailetpackage>
 - Comment-out all existing mailets in root processor
 - Add to root processor: <mailet match="All" class="LogMailet" />
 
#Put greymatter-assembly-0.8-SNAPSHOT.jar in /usr/local/james/conf/lib (use "sbt assembly" to build this, it will have all dependency classes in one fat jar)
 
sudo /usr/local/james/bin/james start

tail -f /usr/local/james/log/james-server.log

Send email to user@your.domain.com and watch logging appear in james-server.log
```

# License

Grey Matter is licensed under the [Apache 2 License](http://www.apache.org/licenses/LICENSE-2.0.txt).

