package com.yejianfengblue.java;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeepCopyTest {

    @AllArgsConstructor
    @Getter
    @Setter
    static class Address {

        private String street;

        private String city;

    }

    @AllArgsConstructor
    @Getter
    @Setter
    static class User {

        private String name;

        private Address address;

    }

    @Test
    void whenShallowCopy_thenObjectShouldNotBeSame() {

        User apple = new User("Apple", new Address("Apple Street", "Apple City"));

        User appleCopy = new User(apple.getName(), apple.getAddress());

        assertNotSame(apple, appleCopy);
    }

    @Test
    void givenShallowCopy_whenModifyOriginalMutableObjectField_thenFieldInCopyShouldChange() {

        User apple = new User("Apple", new Address("Apple Street", "Apple City"));

        // given
        User appleCopy = new User(apple.getName(), apple.getAddress());

        // when
        apple.getAddress().setStreet("Banana Street");
        assertEquals("Banana Street", apple.getAddress().getStreet());

        // then
        assertEquals(apple.getAddress().getStreet(), appleCopy.getAddress().getStreet());
        // because the mutable object in original and copy are the same object
        assertSame(apple.getAddress(), appleCopy.getAddress());
    }

}
