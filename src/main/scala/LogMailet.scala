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
package com.pongr.greymatter.example

import com.pongr.greymatter.GreyMatterMailet
import org.apache.mailet.{ Mail, MailAddress }
import akka.actor.Actor
import akka.actor.Actor.actorOf
import scala.collection.JavaConversions._

/** Uses [[com.pongr.greymatter.example.LogActor]] to receive all mail messages. */
class LogMailet extends GreyMatterMailet {
  def newActor = actorOf[LogActor].start

  def messageFor(mail: Mail) = LogMessage(
    mail.getName,
    mail.getSender,
    mail.getRecipients map (_.asInstanceOf[MailAddress]))

  override def ghost(mail: Mail) = true

  override def init() { log("LogMailet starting up...") }
}

/** Simple message to send to [[com.pongr.greymatter.example.LogActor]]. */
case class LogMessage(name: String, sender: MailAddress, recipients: Iterable[MailAddress])

/** Logs each received mail message. */
class LogActor extends Actor {
  def receive = {
    case LogMessage(name, sender, recipients) => 
      log.info("Received mail " + name + " from " + sender + " to " + recipients)
    case msg@_ => log.warn("Unknown message: " + msg)
  }
}
