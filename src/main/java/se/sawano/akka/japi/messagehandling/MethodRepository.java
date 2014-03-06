/*
 * Copyright 2014 Daniel Sawano
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

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;

import static java.util.Objects.requireNonNull;

@Immutable
final class MethodRepository {

    public static MethodRepository repositoryFor(final Class targetClass) {
        final MethodRepository repository = new MethodRepository(targetClass);
        repository.registerMethods();
        return repository;
    }

    private final Class targetClass;
    private final HashMap<Type, Method> responseMethods = new HashMap<>();
    private final HashMap<Type, Method> requestMethods = new HashMap<>();
    private final HashMap<Type, Method> messageMethods = new HashMap<>();

    private MethodRepository(final Class targetClass) {
        requireNonNull(targetClass);

        this.targetClass = targetClass;
    }

    public Method methodFor(final Object message) {
        requireNonNull(message);

        final Class<?> messageClass = message.getClass();
        final Method messageMethod = messageMethods.get(messageClass);
        if (messageMethod != null) {
            return messageMethod;
        }
        final Method responseMethod = responseMethods.get(messageClass);
        if (responseMethod != null) {
            return responseMethod;
        }
        return requestMethods.get(messageClass);
    }

    @PostConstruct
    private void registerMethods() {
        for (final Class<?> interfaceClass : targetClass.getInterfaces()) {
            if (Requests.class.isAssignableFrom(interfaceClass)) {
                addRequestMethods(interfaceClass.getMethods());
            }
            else if (Responses.class.isAssignableFrom(interfaceClass)) {
                addResponseMethods(interfaceClass.getMethods());
            }
            else if (Messages.class.isAssignableFrom(interfaceClass)) {
                addMessageMethods(interfaceClass.getMethods());
            }
        }
    }

    private void addRequestMethods(final Method[] interfaceMethods) {
        addMethodsToMap(interfaceMethods, requestMethods);
    }

    private void addResponseMethods(final Method[] interfaceMethods) {
        addMethodsToMap(interfaceMethods, responseMethods);
    }

    private void addMessageMethods(final Method[] interfaceMethods) {
        addMethodsToMap(interfaceMethods, messageMethods);
    }

    private void addMethodsToMap(final Method[] interfaceMethods, final HashMap<Type, Method> methodMap) {
        for (final Method method : interfaceMethods) {
            final Type messageType = parameterTypeFor(method);
            assertNotMapped(messageType);
            methodMap.put(messageType, method);
        }
    }

    private void assertNotMapped(final Type type) {
        if (alreadyMapped(type)) {
            failOnClashingMessageType(type);
        }
    }

    private boolean alreadyMapped(final Type messageType) {
        return responseMethods.containsKey(messageType) || requestMethods.containsKey(messageType) || messageMethods
                .containsKey(messageType);
    }

    private void failOnClashingMessageType(final Type type) {
        throw new IllegalArgumentException(
                "Message type: " + type + " already mapped in target class: " + targetClass + ". Can only be mapped once.");
    }

    private Type parameterTypeFor(final Method anInterfaceMethodWithObjectTypeParameters) {
        final Method targetMethod = getMethodFromTarget(anInterfaceMethodWithObjectTypeParameters);
        final Type[] parameterTypes = targetMethod.getParameterTypes();
        assertOnlyOneParameter(parameterTypes);
        return parameterTypes[0];
    }

    private Method getMethodFromTarget(final Method interfaceMethod) {
        Method found = null;
        for (final Method targetMethod : targetClass.getDeclaredMethods()) {
            if (!interfaceMethod.equals(targetMethod) && isSimilar(interfaceMethod, targetMethod)) {
                if (found != null) {
                    throw new IllegalStateException("Found multiple matching methods: " + found + " and " + targetMethod);
                }
                found = targetMethod;
            }
        }
        if (found == null) {
            throw new IllegalStateException("Unable to find target method on delegate for method: " + interfaceMethod);
        }
        return found;
    }

    private void assertOnlyOneParameter(final Type[] parameterTypes) {
        if (parameterTypes.length != 1) {
            throw new IllegalArgumentException("Method must have exactly one (1) parameter. Found " + parameterTypes.length);
        }
    }

    private boolean isSimilar(final Method interfaceMethod, final Method targetMethod) {
        if (interfaceMethod.getName().equals(targetMethod.getName()) && interfaceMethod.getParameterTypes().length == targetMethod
                .getParameterTypes().length) {
            if (isGenericMethod(interfaceMethod)) {
                return isNonObjectClass(targetMethod.getGenericParameterTypes()[0]);
            }
            return hasSameParameterTypes(interfaceMethod, targetMethod);
        }
        return false;
    }

    private boolean isGenericMethod(final Method method) {
        return !(method.getGenericParameterTypes()[0] instanceof Class);
    }

    private boolean isNonObjectClass(final Type type) {
        return type instanceof Class && !Object.class.equals(type);
    }

    private boolean hasSameParameterTypes(final Method interfaceMethod, final Method targetMethod) {
        return interfaceMethod.getParameterTypes()[0].equals(targetMethod.getParameterTypes()[0]);
    }

    int numberOfMappedMethods() {
        return messageMethods.size() + requestMethods.size() + responseMethods.size();
    }

}
