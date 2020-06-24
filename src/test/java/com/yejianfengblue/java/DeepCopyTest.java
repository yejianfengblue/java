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

        /**
         * Deep copy constructor
         */
        Address(Address that) {
            this(that.getStreet(), that.getCity());
        }
    }

    @AllArgsConstructor
    @Getter
    @Setter
    static class User {

        private String name;

        private Address address;

        /**
         * Deep copy constructor
         */
        User(User that) {
            this(that.getName(), new Address(that.getAddress()));
        }
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

    @Test
    void givenDeepCopy_whenModifyOriginalMutableObjectField_thenFieldInCopyShouldNotChange() {

        User apple = new User("Apple", new Address("Apple Street", "Apple City"));

        // given
        User appleCopy = new User(apple);

        // when
        apple.getAddress().setStreet("Banana Street");

        // then
        assertNotEquals(apple.getAddress().getStreet(), appleCopy.getAddress().getStreet());
        // because the mutable object in original and copy are diff object
        assertNotSame(apple.getAddress(), appleCopy.getAddress());
    }

}
