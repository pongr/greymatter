import sbt._
import assembly.AssemblyBuilder

class Project(info: ProjectInfo) 
    extends DefaultProject(info) 
    with AkkaProject 
    with AssemblyBuilder
    with LicenseHeaders
    with ApacheLicense2 {
  
  def copyrightLine = "Copyright (c) 2011 Pongr, Inc."
  
  val javaNetRepo = "javaNetRepo" at "http://download.java.net/maven/2/"
  
  val mailetBase = "org.apache.james" % "apache-mailet-base" % "1.1"
  
  //James logs using log4j, so include slf4j-log4j12 but exclude log4j since James already has it
  override def ivyXML =
    <dependencies>
      <dependency org="org.slf4j" name="slf4j-log4j12" rev="1.6.0">
        <exclude org="log4j" name="log4j" />
      </dependency>
    </dependencies>
  
  override def managedStyle = ManagedStyle.Maven
  def publishUrlSuffix = if (version.toString.endsWith("-SNAPSHOT")) "snapshots/" else "releases/"
  val publishTo = "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/" + publishUrlSuffix
  Credentials(Path.userHome / ".ivy2" / ".scala_tools_credentials", log)
}
