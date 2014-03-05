/*
 * Copyright 2013 Daniel Sawano
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.sawano.akka.japi.messagehandling.examples.bossandworker;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import se.sawano.akka.japi.messagehandling.MessageDelegatingActor;
import se.sawano.akka.japi.messagehandling.predef.Requests1;
import se.sawano.akka.japi.messagehandling.predef.Responses1;

/**
 * This is an example of separation of request and response messages.
 *
 * @author Daniel Sawano
 */
public class Boss extends MessageDelegatingActor implements Responses1<ResponseMessage>, Requests1<RequestMessage> {
    private LoggingAdapter log = Logging.getLogger(context().system(), this);
    private ActorRef client;

    @Override
    public void onResponse(ResponseMessage response) {
        log.info("Worker says: " + response.answer);
        client.tell(new ResponseMessage("Worker is done"), self());
        client = null;
    }

    @Override
    public void onRequest(RequestMessage request) {
        log.info("Client says: " + request.request);
        client = sender();
        context().actorOf(Props.create(Worker.class), "worker-actor").tell(new RequestMessage("Do work!"), self());
    }
}
