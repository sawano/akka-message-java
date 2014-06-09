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

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.testkit.JavaTestKit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class BossAndWorkerTest {

    LoggingAdapter log;
    ActorSystem system;

    @Before
    public void setUp() throws Exception {
        system = ActorSystem.create();
        log = Logging.getLogger(system, this);
    }

    @After
    public void tearDown() throws Exception {
        JavaTestKit.shutdownActorSystem(system);
    }

    @Test
    public void shouldName() throws Exception {
        new JavaTestKit(system) {
            {
                log.info("Telling boss to make worker work...");
                getSystem().actorOf(Props.create(Boss.class), "boss-actor").tell(new RequestMessage("Make worker work!"), getRef());

                final String answer = expectAnswerFromBoss();
                log.info("Boss says: " + answer);
                assertEquals("Worker is done", answer);
            }

            private String expectAnswerFromBoss() {
                return new JavaTestKit.ExpectMsg<String>(Duration.create(2, TimeUnit.SECONDS), null) {
                    @Override
                    protected String match(Object o) {
                        return ((ResponseMessage) o).answer;
                    }
                }.get();
            }
        };
    }

}
