# Grey Matter

[Apache James](http://james.apache.org) is a Java-based mail server and provides a simple [Mailet API](http://james.apache.org/mailet/index.html) that you can use to write your own email-processing apps. Each Mailet subclass is instantiated once, while multiple mail spool threads can call it concurrently to process mail.

Grey Matter provides a simple Mailet trait that converts each received email into a message that is then sent to an [Akka](http://akka.io) actor, making concurrent email processing much easier. Clients simply extend the [GreyMatterMailet](https://github.com/pongr/greymatter/blob/master/src/main/scala/GreyMatterMailet.scala) trait, override a few methods, and then implement the rest of the mail processing app as Akka actors.

This simple example creates a mailet that sends every Mail to a MyActor:

``` scala
class ExampleMailet extends ActorSystemMailet {
  override def newActor(system: ActorSystem): ActorRef = system.actorOf(Props[MyActor])
}
```

Currently uses Akka 2.0.x & Scala 2.9.1.  Future versions will use Akka 2.1.x & Scala 2.10.x. Grey Matter has only been tested with James 3.0, but may very well work with James 2.3.x.

# sbt

``` scala
//Grey Matter is available in central Maven repos
val greyMatter = "com.pongr" %% "greymatter" % "1.0"
```

# ActorMailet

The most basic component that Grey Matter provides is the ActorMailet trait. It is simply a Mailet that sends each received Mail to some ActorRef. The implementing class must provide the ActorRef. 

ActorMailet can also set the Mail's state to GHOST, so that it will not be further processed by James (which it does by default).

While ActorMailet is as high-level as possible, it will usually be more convenient to base your mailets on ActorSystemMailet instead.

# ActorSystemMailet

Grey Matter also provides a more convenient ActorSystemMailet trait. It creates an ActorSystem on mailet initialization and then defers to a subclass to create an ActorRef to send all Mail to. The implementing class may create a chain of actors as complex as needed, as long as it provides a single ActorRef for all Mail to enter.

# Integrating Mailets into James

TODO describe mailetcontainer.xml config...

# Example

Grey Matter ships with an example [LogMailet and LogActor](https://github.com/pongr/greymatter/blob/master/src/main/scala/LogMailet.scala) (guess what they do...). Here are some simple commands to [set up a James 3.0 mail server](http://james.apache.org/server/3/quick-start.html) on EC2 that sends all incoming email through LogMailet to LogActor.

For James 3.0-beta3:

```
#Run an Ubuntu 12.04 64-bit ec2 instance with port 25 open to 0.0.0.0/0 (for SMTP) and port 22 open to your IP (for SSH), for example:
ec2run ami-0145d268 -k your-key -t m1.large -g your-smtp-group -g your-ssh-group

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
sudo wget http://mirror.cogentco.com/pub/apache/james/apache-james/3.0beta3/apache-james-3.0-beta3-app.tar.gz
sudo tar xvzf apache-james-3.0-beta3-app.tar.gz
sudo rm apache-james-3.0-beta3-app.tar.gz
sudo ln -s apache-james-3.0-beta3 james

#In /usr/local/james/conf, set enabled="false" in imapserver.xml & pop3server.xml

#Replace /usr/local/james/conf/domainlist.xml with:
<domainlist class="org.apache.james.domainlist.xml.XMLDomainList">
  <domainnames>
    <domainname>your.domain.com</domainname>
  </domainnames>
  <autodetect>false</autodetect>
  <autodetectIP>false</autodetectIP>
</domainlist>
 
#In /usr/local/james/conf/smtpserver.xml:
 - Comment out: <!-- <handler class="org.apache.james.smtpserver.fastfail.ValidRcptHandler"/> -->
 
#In /usr/local/james/conf/log4j.properties:
 - Add FILE to: log4j.rootLogger=DEBUG, FILE
 
#In /usr/local/james/conf/mailetcontainer.xml:
 - Comment-out all existing mailets in root processor
 - Add to root processor: <mailet match="All" class="com.pongr.greymatter.example.LogMailet" />
 
#Put greymatter-assembly-1.0.0.jar in /usr/local/james/conf/lib (use "sbt assembly" to build this, it will have all dependency classes in one fat jar)
 
sudo /usr/local/james/bin/james start

tail -f /usr/local/james/log/james-server.log

Send email to user@your.domain.com and watch logging appear in james-server.log
```

For James 3.0-beta4:

```
#Download James and set it up at /usr/local/james
cd /usr/local
sudo wget http://mirror.cogentco.com/pub/apache/james/apache-james/3.0beta4/apache-james-3.0-beta4-app.tar.gz
sudo tar xvzf apache-james-3.0-beta4-app.tar.gz
sudo rm apache-james-3.0-beta4-app.tar.gz
sudo ln -s apache-james-3.0-beta4 james

#Create /usr/local/james/conf/domainlist.xml:
<domainlist class="org.apache.james.domainlist.xml.XMLDomainList">
  <domainnames>
    <domainname>your.domain.com</domainname>
  </domainnames>
  <autodetect>false</autodetect>
  <autodetectIP>false</autodetectIP>
</domainlist>

#Create conf/imapserver.conf
<imapservers>
  <imapserver enabled="false" />
</imapservers>

#In /usr/local/james/conf/log4j.properties:
 - Add FILE to: log4j.rootLogger=DEBUG, FILE

#Create conf/pop3server.conf
<pop3servers>
  <pop3server enabled="false" />
</pop3servers>

#Create conf/smtpserver.conf
<smtpservers>
  <smtpserver enabled="true">
    <jmxName>smtpserver</jmxName>
    <bind>0.0.0.0:25</bind>
    <connectionBacklog>200</connectionBacklog>
    <tls socketTLS="false" startTLS="false"></tls>
    <connectiontimeout>360</connectiontimeout>
    <connectionLimit>0</connectionLimit>
    <connectionLimitPerIP>0</connectionLimitPerIP>
    <authRequired>true</authRequired>
    <authorizedAddresses>127.0.0.0/8</authorizedAddresses>
    <verifyIdentity>true</verifyIdentity>
    <!--  This sets the maximum allowed message size (in kilobytes) for this -->
    <!--  SMTP service. If unspecified, the value defaults to 0, which means no limit. -->
    <maxmessagesize>0</maxmessagesize>
    <addressBracketsEnforcement>true</addressBracketsEnforcement>
    <handlerchain>
      <handler class="org.apache.james.smtpserver.CoreCmdHandlerLoader"/>
    </handlerchain>
  </smtpserver>
</smtpservers>

#Create conf/mailetcontainer.conf
<?xml version="1.0"?>
<mailetcontainer enableJmx="true">
  <context>
    <postmaster>postmaster@localhost</postmaster>
  </context>
  <spooler>
    <threads>20</threads>
  </spooler>
  <processors>
    <processor state="root" enableJmx="true">
      <mailet match="All" class="com.pongr.greymatter.example.LogMailet" />
    </processor>
    <!-- For some reason we have to include this processor and mailet, otherwise James won't start up... #donotremove -->
    <processor state="error" enableJmx="true">
      <mailet match="All" class="ToRepository">
        <repositoryPath>file://var/mail/error/</repositoryPath>
      </mailet>
    </processor>
  </processors>
</mailetcontainer>

#Add to conf/wrapper.conf: (via http://stackoverflow.com/questions/11999233/apache-james-gives-classnotfoundexception-com-mysql-jdbc-driver)
wrapper.java.classpath.120=../conf/lib/*

#Put greymatter-assembly-1.0.0.jar in /usr/local/james/conf/lib (use "sbt assembly" to build this, it will have all dependency classes in one fat jar)
 
sudo /usr/local/james/bin/james start

tail -f /usr/local/james/log/james-server.log

Send email to user@your.domain.com and watch logging appear in james-server.log
```

# License

Grey Matter is licensed under the [Apache 2 License](http://www.apache.org/licenses/LICENSE-2.0.txt).
