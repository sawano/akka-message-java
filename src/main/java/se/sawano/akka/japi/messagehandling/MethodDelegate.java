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

import net.jcip.annotations.Immutable;

import java.lang.reflect.Method;

import static java.util.Objects.requireNonNull;
import static se.sawano.akka.japi.messagehandling.MethodRepository.repositoryFor;

/**
 * Delegates received messages to the appropriate method as declared by the target's implemented interfaces. I.e. {@link Messages},{@link
 * Requests} and {@link Responses}, or any of their ancestors. Messages are mapped by type so it is not possible to have more than one
 * message receiving method for any given message type. E.g. one cannot map a class implementing the interface {@code Messages2&lt;Double,
 * Double&gt;}. But {@code Messages2&lt;Double, Integer&gt;} would be fine.
 * <p/>
 * A typical use case would be to create a base actor that takes care of the message delegation and then have other actors extend that base
 * class. For example an implementation of a base actor could look like this:
 * <pre>
 * public MessageDelegatingActor() {
 *    methodDelegate = new MethodDelegate(this);
 * }
 *
 * public void onReceive(Object message) {
 *     if (methodDelegate.onReceive(message)) {
 *         return;
 *     }
 *     unhandled(message);
 * } </pre>
 *
 * @author Daniel Sawano
 * @see Messages
 * @see Requests
 * @see Responses
 * @see MessageDelegatingActor
 */
@Immutable
public class MethodDelegate {

    private final Object target;
    private final MethodRepository methodRepository;

    /**
     * Creates a new delegate for the given target. All methods that are defined in the interfaces {@link Messages},{@link Requests} and
     * {@link Responses}, or any of their ancestors will be registered.
     *
     * @param target
     *         the target to delegate to
     *
     * @throws IllegalArgumentException
     *         if unable to map the target
     * @throws IllegalStateException
     *         if unable to map the target
     */
    public MethodDelegate(final Object target) {
        requireNonNull(target);

        this.target = target;
        this.methodRepository = repositoryFor(target.getClass());
    }

    /**
     * Delegates a message to the target.
     *
     * @param message
     *         the message to delegate
     *
     * @return {@code true} if a matching method was found and the message was delegated to the target, {@code false} otherwise
     *
     * @throws DelegateException
     *         if an exception occurred while invoking the target method
     */
    public boolean onReceive(final Object message) throws DelegateException {
        notNull(message);

        final Method method = methodRepository.methodFor(message);
        if (method == null) {
            return false;
        }

        try {
            method.invoke(target, message);
        } catch (Exception e) {
            throw new DelegateException("Exception while invoking target method", e);
        }
        return true;
    }

    private void notNull(final Object message) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
    }

    int numberOfMappedMethods() {
        return methodRepository.numberOfMappedMethods();
    }

}
