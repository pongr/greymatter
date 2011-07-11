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

import org.apache.mailet.Mail
import org.apache.mailet.base.GenericMailet
import akka.actor.ActorRef

/**
 * Simple abstract mailet that converts each mail into a message that is sent
 * to an Akka actor. Subclasses must implement the newActor and messageFor methods.
 */
trait GreyMatterMailet extends GenericMailet {
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
}