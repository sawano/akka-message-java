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

package se.sawano.akka.japi.messagehandling.predef;

/**
 * Predefined message interface with three {@code onMessage()} methods.
 *
 * @param <P>
 *         the third message type
 * @param <V>
 *         the second message type
 * @param <T>
 *         the first message type
 *
 * @author Daniel Sawano
 */
public interface Messages3<P, V, T> extends Messages2<V, T> {

    void onMessage3(P message);
}
