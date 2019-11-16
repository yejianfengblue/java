package com.yejianfengblue.java;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Any interface with a single abstract method is a functional interface, and its impl
 * may be treated as lambda expressions, method references, or constructor references
 * @author yejianfengblue
 */
class FunctionInterfaceTest {

    @Test
    void functionTest() {

        Function<Object, String> intToString = Object::toString;
        assertEquals("hi", intToString.apply("hi"));
    }

    @Test
    void supplierTest() {

        Supplier<Integer> return2 = () -> 2;
        assertEquals(Integer.valueOf(2), return2.get());
    }

    @Test
    void consumerTest() {

        Consumer<String> print = s -> s.length();
        print.accept("hi");
    }

    @Test
    void predicateTest() {

        Predicate<String> isEmpty = str -> str.isEmpty();
        assertFalse(isEmpty.test("hi"));
    }

    @Test
    void operatorTest() {

        UnaryOperator<String> toUpperCase = str ->  str.toUpperCase();
        assertEquals("HI", toUpperCase.apply("hi"));

        BinaryOperator<Integer> sum = (i1, i2) -> Integer.sum(i1, i2);
    }
}
