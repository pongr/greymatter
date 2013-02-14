/*
 * Copyright (c) 2011 Pongr, Inc.
 *   
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pongr.greymatter

import org.apache.mailet._
import org.apache.mailet.base.GenericMailet
import akka.actor._

/*
Cannot just pass an ActorRef to mailet constructor
Mailets are defined declaratively in XML like this:

<mailet match="All" class="com.pongr.fourarms.mailet.MeterMailet">
  <param>value</param>
</mailet>

Specify FQCN of mailet class, James will instantiate it
Inner params available to mailet

In Akka 2.0.x to create an actor you need:
 - a Props object
 - an ActorRefFactory (somebody needs to create an ActorSystem somehow)
Could create the ActorSystem in the mailet init() method since it's called once on setup
And also create the ActorRef in the init() method

service() needs to:
  - possibly set ghost state of Mail
  - send mail to ActorRef (don't do Mail => Any conversion anymore, ActorRef can do that)
*/

/** Sends each mail to an actor. It is entirely up to the subclass to provide the actor. */
trait ActorMailet extends GenericMailet {
  val GhostParameter = "ghost"
  val DefaultGhost = "true"
  val ghost: Boolean = try { getInitParameter(GhostParameter, DefaultGhost).toBoolean } catch { case _ => true }

  def actor: Option[ActorRef]

  override def service(mail: Mail) {
    if (ghost) mail.setState(Mail.GHOST)
    actor foreach { _ ! mail }
  }
}

trait ActorSystemMailet extends ActorMailet {
  val ActorSystemNameParameter = "actor-system-name"
  val DefaultActorSystemName = "james"
  /** Gets the actor system name from mailet config. Do something like this:
    * <pre>
    * <mailet ...>
    *   <actor-system-name>foo</actor-system-name>
    * </mailet>
    * </pre>
    */
  def actorSystemName: String = getInitParameter(ActorSystemNameParameter, DefaultActorSystemName)

  /** Creates a new ActorSystem. */
  def newActorSystem() = ActorSystem(actorSystemName)

  /** Creates a new actor using the specified actor system. */
  def newActor(system: ActorSystem): ActorRef

  /** The ActorSystem used to create the Actor. */
  var system: Option[ActorSystem] = None
  /** The Actor to send all mail to. */
  var actor: Option[ActorRef] = None

  /** Creates a new ActorSystem and then creates a new Actor using the Props. */
  override def init() {
    system = Some(newActorSystem())
    actor = Some(newActor(system.get))
  }
}

/**
 * Simple abstract mailet that converts each mail into a message that is sent
 * to an Akka actor. Subclasses must implement the newActor and messageFor methods.
 */
/*trait GreyMatterMailet extends GenericMailet {
  /** The actor that this mailet will send all mail messages to. */
  lazy val actor = newActor

  /** Creates the actor that this mailet will send all mail messages to. */
  def newActor: ActorRef

  /** Converts the specified mail into an immutable message to send to the actor. */
  def messageFor(mail: Mail): Any

  /**
   * Returns true if the specified mail does not need to be processed further
   * by any other matchers or mailets. If true then the mail's state will be set to GHOST.
   *
   *  This implementation always returns false. Subclasses may override to customize.
   */
  def ghost(mail: Mail): Boolean = false

  /**
   * Converts the specified mail into a message and sends the message to the actor.
   * Sets the mail's state to GHOST if necessary.
   */
  override def service(mail: Mail) {
    val msg = messageFor(mail)

    if (ghost(mail)) {
      mail.setState(Mail.GHOST)
      log("Set state for mail " + mail.getName + " to GHOST")
    }

    log("Sending message " + msg + " for mail " + mail.getName + " to actor...")
    actor ! msg
  }
}*/
