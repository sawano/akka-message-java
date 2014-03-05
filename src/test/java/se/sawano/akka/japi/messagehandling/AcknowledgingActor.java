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

package se.sawano.akka.japi.messagehandling;

import se.sawano.akka.japi.messagehandling.predef.Requests2;
import se.sawano.akka.japi.messagehandling.predef.Responses1;

public class AcknowledgingActor extends MessageDelegatingActor implements Requests2<Short, Float>, Responses1<Integer> {

    @Override
    public void onRequest(Float request) {
        ack(request);
    }

    @Override
    public void onRequest2(Short request) {
        ack(request);
    }

    @Override
    public void onResponse(Integer response) {
        ack(response);
    }

    private void ack(Object message) {
        sender().tell(message, self());
    }
}
