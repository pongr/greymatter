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

/** Sends each mail to an actor. The subclass needs to provide the actor, as well as a function that converts a Mail object into a message for that actor. */
trait ActorMailet extends GenericMailet {
  val GhostParameter = "ghost"
  val DefaultGhost = "true"
  /** Whether or not to set each mail's state to GHOST, so that it will not be processed any further by James. */
  val ghost: Boolean = try { getInitParameter(GhostParameter, DefaultGhost).toBoolean } catch { case _ => true }

  /** The actor to send each mail to. Subclasses must provide this. */
  def actor: Option[ActorRef]

  /** Converts the specified mail into the message that will be sent to the actor. It is advised to extract all required information out of this mail object
    * and wrap it in a case class message, instead of sending the Mail, or MimeMessage, or any other raw mail objects to the actor. It appears that 
    * accessing the underlying MimeMessage is problematic in threads other than James threads. If you try to call methods on the MimeMessage in an 
    * actor, you are likely to get NullPointerExceptions. So extract what you need from the Mail & MimeMessage here, and encapulate those values in a 
    * case class instance, and then have your actor handle those case class messages.
    */
  def messageFor(mail: Mail): Any

  /** Sets mail state to GHOST, converts the mail into a message and then sends that message to the actor. */
  override def service(mail: Mail) {
    if (ghost) mail.setState(Mail.GHOST)
    actor foreach { _ ! messageFor(mail) }
  }
}

/** Creates a new actor system and a new actor in the init() method. It is entirely up to the subclass to create the actor. */
trait ActorSystemMailet extends ActorMailet {
  val ActorSystemNameParameter = "actor-system-name"
  val DefaultActorSystemName = "james"
  /** Gets the actor system name from mailet config. Do something like this:
    * {{{
    * <mailet ...>
    *   <actor-system-name>foo</actor-system-name>
    * </mailet>
    * }}}
    */
  def actorSystemName: String = getInitParameter(ActorSystemNameParameter, DefaultActorSystemName)

  /** Creates a new ActorSystem. */
  def newActorSystem() = ActorSystem(actorSystemName)

  /** Creates a new actor using the specified actor system. Subclasses must provide this. */
  def newActor(system: ActorSystem): ActorRef

  /** The ActorSystem used to create the Actor. */
  var system: Option[ActorSystem] = None
  /** The Actor to send all mail to. */
  var actor: Option[ActorRef] = None

  /** Creates a new actor system, creates a new actor and saves them both. */
  override def init() {
    if (system.isDefined) throw new IllegalStateException("Actor system already exists")
    if (actor.isDefined) throw new IllegalStateException("Actor already exists")
    system = Some(newActorSystem())
    actor = Some(newActor(system.get))
  }
}
