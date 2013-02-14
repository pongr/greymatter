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

/** Sends each mail to an actor. It is entirely up to the subclass to provide the actor. */
trait ActorMailet extends GenericMailet {
  val GhostParameter = "ghost"
  val DefaultGhost = "true"
  /** Whether or not to set each mail's state to GHOST, so that it will not be processed any further by James. */
  val ghost: Boolean = try { getInitParameter(GhostParameter, DefaultGhost).toBoolean } catch { case _ => true }

  /** The actor to send each mail to. Subclasses must provide this. */
  def actor: Option[ActorRef]

  /** Sets mail state to GHOST and then sends the mail to the actor. */
  override def service(mail: Mail) {
    if (ghost) mail.setState(Mail.GHOST)
    actor foreach { _ ! mail }
  }
}

/** Creates a new actor system and a new actor in the init() method. It is entirely up to the subclass to create the actor. */
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

  /** Creates a new actor using the specified actor system. Subclasses must provide this. */
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
