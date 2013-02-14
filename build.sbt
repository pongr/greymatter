import AssemblyKeys._

organization := "com.pongr"

name := "greymatter"

scalaVersion := "2.9.1"

resolvers ++= Seq(
  "Typesafe" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "org.apache.james" % "apache-mailet-base" % "1.1", //there is a 2.5.0 in central repo now
  "com.typesafe.akka" % "akka-actor" % "2.0.5",
  "com.typesafe.akka" % "akka-slf4j" % "2.0.5",
  "org.slf4j" % "slf4j-log4j12" % "1.6.0" //this is a 1.7.2 in central repo now
)

//James 3.0 logs using log4j, so include slf4j-log4j12 but exclude log4j since James already has it
ivyXML := 
  <dependencies>
    <exclude org="log4j" name="log4j" />
  </dependencies>

assemblySettings

seq(sbtrelease.Release.releaseSettings: _*)

//http://www.scala-sbt.org/using_sonatype.html
//https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide
publishTo <<= version { v: String =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots/")
  else                             Some("releases" at nexus + "service/local/staging/deploy/maven2/")
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

licenses := Seq("Apache-2.0" -> url("http://opensource.org/licenses/Apache-2.0"))

homepage := Some(url("http://github.com/pongr/greymatter"))

organizationName := "Pongr"

organizationHomepage := Some(url("http://pongr.com"))

description := "Integration between Apache James and Akka"

pomExtra := (
  <scm>
    <url>git@github.com:pongr/greymatter.git</url>
    <connection>scm:git:git@github.com:pongr/greymatter.git</connection>
  </scm>
  <developers>
    <developer>
      <id>zcox</id>
      <name>Zach Cox</name>
      <url>http://theza.ch</url>
    </developer>
  </developers>
)
