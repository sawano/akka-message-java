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

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sawano.akka.japi.messagehandling.predef.*;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class MethodDelegateTest {

    @Test
    public void shouldInvokeMethodRequest() {
        givenDelegate();

        whenReceiving(Long.valueOf(1));

        thenTheOnlyMethodsInvokedAre("onRequest");
    }

    @Test
    public void shouldInvokeMethodRequest2() {
        givenDelegate();

        whenReceiving(Integer.valueOf(1));

        thenTheOnlyMethodsInvokedAre("onRequest2");
    }

    @Test
    public void shouldInvokeMethodResponse() {
        givenDelegate();

        whenReceiving(Short.valueOf((short) 1));

        thenTheOnlyMethodsInvokedAre("onResponse");
    }

    @Test
    public void shouldInvokeMethodOnMessage() {
        givenDelegate();

        whenReceiving(new Byte("1"));

        thenTheOnlyMethodsInvokedAre("onMessage");
    }

    @Test
    public void shouldInvokeMethodOnMessage2() {
        givenDelegate();

        whenReceiving(Double.valueOf(1));

        thenTheOnlyMethodsInvokedAre("onMessage2");
    }

    @Test
    public void shouldNotInvokeAnyMethod() {
        givenDelegate();

        thenNothingShouldBeInvokedWhenReceiving(String.valueOf(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnNullMessage() {
        givenDelegate();

        whenReceiving(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfSameTypeExistsForBothRequestAndResponse() {
        givenDelegateFor(new ClashingClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfSameTypeExistsForMultipleRequestMethods() {
        givenDelegateFor(new ClashingRequests());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfSameTypeExistsForMultipleResponseMethods() {
        givenDelegateFor(new ClashingResponses());
    }

    @Test
    public void shouldInvokeNonGenericMethods() {
        givenNonGenericDelegate();

        whenReceiving(Integer.valueOf(1));
        whenReceiving(Long.valueOf(1));

        thenTheOnlyMethodsInvokedAre("someMethod", "someOtherMethod");
    }

    @Test
    public void shouldInvokeNonGenericMethodsWithSameName() {
        givenSameMethodNameDelegate();

        whenReceiving(Integer.valueOf(1));
        whenReceiving(Long.valueOf(1));

        thenTheOnlyMethodsInvokedAre("someMethodInteger", "someMethodLong");
    }

    /**
     * 0.012ms, 0.029, 0.03
     */
    @Test
    public void shouldDemonstrateTimeToMapMethods() throws Exception {
        final Logger logger = LoggerFactory.getLogger(getClass());
        final ExampleClass exampleClass = new ExampleClass();

        final ArrayList<MethodDelegate> warmup = new ArrayList<>();
        for (int i = 0; i < 10000; ++i) {
            warmup.add(new MethodDelegate(exampleClass));
        }
        logger.info("Warmed up with {} delegates", warmup.size());

        final long t1 = System.nanoTime();
        final MethodDelegate methodDelegate = new MethodDelegate(exampleClass);
        final long total = System.nanoTime() - t1;

        final double totalMillis = total / 1e6;
        if (totalMillis >= 1) {
            logger.warn("Time to map {} methods was: {} (ms) but should be less than 1 ms", methodDelegate.numberOfMappedMethods(),
                    totalMillis);
        }
        else {
            logger.info("Time to map methods: {} (ms)", totalMillis);
        }
    }

    /**
     * This test class has an additional interface "Comparable" to demonstrate that other interfaces does not interfere with the delegate's
     * ability to function properly.
     */
    public final class ExampleClass
            implements Requests2<Integer, Long>, Responses1<Short>, Messages2<Double, Byte>, Comparable<ExampleClass> {

        @Override
        public void onRequest(Long request) {
            invokedMethods.add("onRequest");
        }

        @Override
        public void onRequest2(Integer request) {
            invokedMethods.add("onRequest2");
        }

        @Override
        public void onResponse(Short response) {
            invokedMethods.add("onResponse");
        }

        @Override
        public void onMessage(Byte message) {
            invokedMethods.add("onMessage");
        }

        @Override
        public void onMessage2(Double message) {
            invokedMethods.add("onMessage2");
        }

        @Override
        public int compareTo(ExampleClass o) {
            return 0;
        }
    }

    public final class NonGenericExampleClass implements NonGenericInterface {

        @Override
        public void someMethod(Integer i) {
            invokedMethods.add("someMethod");
        }

        @Override
        public void someOtherMethod(Long l) {
            invokedMethods.add("someOtherMethod");
        }
    }

    public interface NonGenericInterface extends Messages {
        public void someMethod(Integer i);

        public void someOtherMethod(Long l);
    }

    public final class SameMethodNameExampleClass implements NonGenericInterfaceWithSimilarMethods {

        @Override
        public void someMethod(Integer i) {
            invokedMethods.add("someMethodInteger");
        }

        @Override
        public void someMethod(Long l) {
            invokedMethods.add("someMethodLong");
        }
    }

    public interface NonGenericInterfaceWithSimilarMethods extends Messages {
        public void someMethod(Integer i);

        public void someMethod(Long l);
    }

    public final static class ClashingClass implements Requests1<Double>, Responses1<Double> {
        @Override
        public void onRequest(Double request) {
        }

        @Override
        public void onResponse(Double response) {
        }
    }

    public final static class ClashingRequests implements Requests2<Double, Double> {

        @Override
        public void onRequest2(Double request) {
        }

        @Override
        public void onRequest(Double request) {
        }
    }

    public final static class ClashingResponses implements Responses2<Double, Double> {

        @Override
        public void onResponse2(Double response) {
        }

        @Override
        public void onResponse(Double response) {
        }
    }

    private MethodDelegate delegate;
    private ArrayList<String> invokedMethods;

    @Before
    public void setUp() throws Exception {
        invokedMethods = new ArrayList<>();
    }

    private void givenNonGenericDelegate() {
        delegate = new MethodDelegate(new NonGenericExampleClass());
    }

    private void givenSameMethodNameDelegate() {
        delegate = new MethodDelegate(new SameMethodNameExampleClass());
    }

    private void givenDelegate() {
        delegate = new MethodDelegate(new ExampleClass());
    }

    private void givenDelegateFor(Object target) {
        delegate = new MethodDelegate(target);
    }

    private void whenReceiving(Object aMessage) {
        assertTrue("Expected delegate to invoke method for message type: '" + ((null == aMessage) ? null : aMessage.getClass()) + "'",
                delegate.onReceive(aMessage));
    }

    private void thenTheOnlyMethodsInvokedAre(String... methods) {
        assertEquals(methods.length, invokedMethods.size());
        for (String method : methods) {
            assertInvoked(method);
        }
    }

    private void assertInvoked(String method) {
        assertTrue("Method: '" + method + "' was not invoked", invokedMethods.contains(method));
    }

    private void thenNothingShouldBeInvokedWhenReceiving(Object aMessage) {
        assertFalse(delegate.onReceive(aMessage));
        assertTrue(invokedMethods.isEmpty());
    }

}
