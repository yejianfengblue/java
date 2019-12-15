package com.yejianfengblue.java;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author yejianfengblue
 */
class ProxyTest {

    static class DynamicInvocationHandler implements InvocationHandler {

        private static Logger LOGGER = LoggerFactory.getLogger(DynamicInvocationHandler.class);

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            LOGGER.info("Invoked method: {}", method.getName());

            return 42;
        }
    }

    @Test
    void createProxyInstance() {

        Map proxyInstance = (Map) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{Map.class},
                new DynamicInvocationHandler());

        proxyInstance.put("hello", "world");
    }

    @Test
    void defineInvocationHandlerViaLambda() {

        Map proxyInstance = (Map) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{Map.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("get")) {
                        return 42;
                    } else {
                        throw new UnsupportedOperationException(
                                "Unsupported method: " + method.getName());
                    }
                });
        assertEquals(42, (int) proxyInstance.get("Hello"));
    }

    static class TimingDynamicInvocationHandler implements InvocationHandler {

        private static Logger log = LoggerFactory.getLogger(TimingDynamicInvocationHandler.class);

        private final Map<String, Method> methods = new HashMap<>();

        private Object target;

        TimingDynamicInvocationHandler(Object target) {

            this.target = target;
            Arrays.stream(target.getClass().getDeclaredMethods())
                    .forEach(method -> this.methods.put(method.getName(), method));
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            LocalDateTime startTime = LocalDateTime.now();
            Object returnValue = methods.get(method.getName()).invoke(target, args);
            log.info("Execute {} finished in {} ns",
                    method.getName(),
                    startTime.until(LocalDateTime.now(), ChronoUnit.NANOS));

            return returnValue;
        }
    }

    @Test
    void givenDefinedTimingInvocationHandler_whenCallMethodViaProxy_thenMethodElapseTimeIsLogger() {

        Map mapProxyInstance = (Map) Proxy.newProxyInstance(
                ProxyTest.class.getClassLoader(),
                new Class[]{Map.class},
                new TimingDynamicInvocationHandler(new HashMap()));

        mapProxyInstance.put("hello", "world");

        CharSequence charSequenceProxyInstance = (CharSequence) Proxy.newProxyInstance(
                ProxyTest.class.getClassLoader(),
                new Class[]{CharSequence.class},
                new TimingDynamicInvocationHandler("Hello World")
        );

        charSequenceProxyInstance.length();
    }
}
