package com.yejianfengblue.java;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DeepCopyTest {

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    static class Address implements Serializable {

        private String street;

        private String city;

        /**
         * Deep copy constructor
         */
        Address(Address that) {
            this(that.getStreet(), that.getCity());
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    static class User implements Serializable {

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

    @Test
    void givenDeepCopyViaApacheCommonsLang_whenModifyOriginalMutableObjectField_thenFieldShouldNotChange() {

        User apple = new User("Apple", new Address("Apple Street", "Apple City"));

        // given
        User copy = SerializationUtils.clone(apple);

        // when
        apple.getAddress().setStreet("Banana Street");

        // then
        assertThat(copy.getAddress().getStreet())
                .isNotEqualTo(apple.getAddress().getStreet());

    }

    @Test
    void givenDeepCopyViaGson_whenModifyOriginalMutableObjectField_thenFieldShouldNotChange() {

        User apple = new User("Apple", new Address("Apple Street", "Apple City"));
        Gson gson = new Gson();

        // given
        User copy = gson.fromJson(gson.toJson(apple), User.class);

        // when
        apple.getAddress().setStreet("Banana Street");

        // then
        assertThat(copy.getAddress().getStreet())
                .isNotEqualTo(apple.getAddress().getStreet());

    }

    @Test
    void givenDeepCopyViaJackson_whenModifyOriginalMutableObjectField_thenFieldShouldNotChange() throws JsonProcessingException {

        User apple = new User("Apple", new Address("Apple Street", "Apple City"));
        ObjectMapper objectMapper = new ObjectMapper();

        // given
        User copy = objectMapper.readValue(objectMapper.writeValueAsString(apple), User.class);

        // when
        apple.getAddress().setStreet("Banana Street");

        // then
        assertThat(copy.getAddress().getStreet())
                .isNotEqualTo(apple.getAddress().getStreet());

    }

}
