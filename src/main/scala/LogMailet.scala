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

import com.pongr.greymatter._
import org.apache.mailet.{ Mail, MailAddress }
import akka.actor._
import scala.collection.JavaConversions._

/** Example of an ActorSystemMailet. Add it to James like this:
  * {{{
  * <mailet matcher="All" class="com.pongr.greymatter.example.LogMailet" />
  * }}}
  */
class LogMailet extends ActorSystemMailet {
  override def newActor(system: ActorSystem): ActorRef = {
    //val logActor = system.actorOf(Props[LogActor])
    //system.actorOf(Props(new MailToLogMessageActor(logActor)))
    system.actorOf(Props[LogActor])
  }

  override def messageFor(mail: Mail): Any = LogMessage(mail.getName, mail.getSender, mail.getRecipients.map(_.asInstanceOf[MailAddress]).toSeq)
}

/** Example of the first actor in the pipeline, which converts a Mail object into some other domain object. */
class MailToLogMessageActor(next: ActorRef) extends Actor {
  def receive = {
    case mail: Mail => next ! LogMessage(mail.getName, mail.getSender, mail.getRecipients.map(_.asInstanceOf[MailAddress]).toSeq)
  }
}

/** Simple message to send to [[com.pongr.greymatter.example.LogActor]]. */
case class LogMessage(name: String, sender: MailAddress, recipients: Seq[MailAddress])

/** Logs each received mail message. */
class LogActor extends Actor with ActorLogging {
  def receive = {
    case LogMessage(name, sender, recipients) => log.info("Received mail " + name + " from " + sender + " to " + recipients)
    case msg => log.warning("Unknown message: " + msg)
  }
}
