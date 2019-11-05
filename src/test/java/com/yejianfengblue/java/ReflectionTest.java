package com.yejianfengblue.java;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author yejianfengblue
 */
class ReflectionTest {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Test
    void givenObject_whenGetFieldNameAtRuntime_thenCorrect() {

        Object person = new Person();
        List<String> actualFieldList = Arrays.stream(person.getClass().getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toList());

        assertTrue(actualFieldList.containsAll(Arrays.asList("name", "age")));
    }

    @Data
    private static class Person {

        private String name;
        private int age;
    }

    ///////////////////////////////////////////////////////////////////////////

    private static interface ReflectionTestInterface {

        String reflectionTestInterfaceMethod();
    }

    @Data
    private static abstract class ReflectionTestAbstractClass implements ReflectionTestInterface {

        public static String STATIC_FIELD = "STATIC_FIELD";

        private String privateField;

        protected abstract String protectedMethod();
    }

    private static interface AnotherInterface {

        String anotherInterfaceMethod();
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    private class ReflectionTestClass extends ReflectionTestAbstractClass implements AnotherInterface {

        @Override
        public String reflectionTestInterfaceMethod() {
            return "ReflectionTestClass.reflectionTestInterfaceMethod()";
        }

        @Override
        protected String protectedMethod() {
            return "ReflectionTestClass.protectedMethod()";
        }

        @Override
        public String anotherInterfaceMethod() {
            return "ReflectionTestClass.anotherInterfaceMethod()";
        }
    }

    @Test
    void givenObject_whenGetsClassName_thenCorrect() {

        Object reflectionTestClass = new ReflectionTestClass();
        Class<?> clazz = reflectionTestClass.getClass();

        assertEquals("ReflectionTestClass", clazz.getSimpleName());
        assertEquals("com.yejianfengblue.java.ReflectionTest$ReflectionTestClass", clazz.getName());
        assertEquals("com.yejianfengblue.java.ReflectionTest.ReflectionTestClass", clazz.getCanonicalName());

        assertEquals("Entry", Map.Entry.class.getSimpleName());
        assertEquals("java.util.Map$Entry", Map.Entry.class.getName());
        assertEquals("java.util.Map.Entry", Map.Entry.class.getCanonicalName());
    }

    @Test
    void givenClass_whenGetModifiers_thenCorrect() {

        Class<?> clazz = ReflectionTest.class;

        assertTrue(Modifier.isInterface(ReflectionTestInterface.class.getModifiers()));
        assertTrue(Modifier.isAbstract(ReflectionTestAbstractClass.class.getModifiers()));
        assertTrue(Modifier.isStatic(ReflectionTestInterface.class.getModifiers()));
        assertFalse(Modifier.isStatic(ReflectionTestClass.class.getModifiers()));
    }

    @Test
    void givenClass_whenGetPackageInfo_thenCorrect() {

        assertEquals("java.lang", Integer.class.getPackage().getName());
    }

    @Test
    void givenClass_whenGetSuperClass_thenCorrect() {

        assertEquals(ReflectionTestAbstractClass.class, ReflectionTestClass.class.getSuperclass());
    }

    @Test
    void givenClass_whenGetImplementedInterfaces_thenReturnDirectlyImplementedInterfaces() {

        Class<?>[] reflectionTestAbstractClass_interfaces = ReflectionTestAbstractClass.class.getInterfaces();
        assertEquals(1, reflectionTestAbstractClass_interfaces.length);
        assertEquals(ReflectionTestInterface.class, reflectionTestAbstractClass_interfaces[0]);

        Class<?>[] reflectionTestClass_interfaces = ReflectionTestClass.class.getInterfaces();
        assertEquals(1, reflectionTestClass_interfaces.length);
        assertEquals(AnotherInterface.class, reflectionTestClass_interfaces[0]);
    }

    ///////////////////////////////////////////////////////////////////////////
    private class IntegerComparator implements Comparator<Integer> {

        @Override
        public int compare(Integer o1, Integer o2) {
            return 0;
        }
    }

    @Test
    void ifAGenericTypeIsPartOfClassSignature_thenGenericTypeIsAvailableAtRuntime() {

        assertEquals("java.lang.Integer",
                ((ParameterizedType)IntegerComparator.class.getGenericInterfaces()[0])
                        .getActualTypeArguments()[0]
                        .getTypeName());
    }
}
