import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  val akkaRepo = "akka" at "http://akka.io/repository/"
  val akkaPlugin = "se.scalablesolutions.akka" % "akka-sbt-plugin" % "1.0"
  
  val licensePlugin = "com.banno" % "sbt-license-plugin" % "0.0.2" from "http://cloud.github.com/downloads/T8Webware/sbt-license-plugin/sbt-license-plugin-0.0.2.jar"
  
  //only needed for testing LogMailet
  val codaRepo = "Coda Hale's Repository" at "http://repo.codahale.com/"
  val assembly = "com.codahale" % "assembly-sbt" % "0.1.1"
}
