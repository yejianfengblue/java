package com.yejianfengblue.java;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CloneTest {

    /**
     * A class override {@link Object#clone()} but not implements {@link Cloneable}
     */
    private static class CloneNotSupportClass {

        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

    @Test
    void givenAClassOverrideCloneButNotImplementsCloneable_whenCallClone_thenCloneNotSupportedException() {

        CloneNotSupportClass cloneNotSupportClass = new CloneNotSupportClass();

        assertThatThrownBy(() -> cloneNotSupportClass.clone())
                .isInstanceOf(CloneNotSupportedException.class);
    }


    @AllArgsConstructor
    @Getter
    private static class CloneCatchClass {

        private final String value;

        // Use covariant return type to eliminate the need for casting in client side
        @Override
        public CloneCatchClass clone() {

            try {
                return (CloneCatchClass)super.clone();
            } catch (CloneNotSupportedException e) {
                return new CloneCatchClass("clone value");
            }
        }
    }

    @Test
    void givenAClassOverrideCloneAndCatchCloneNotSupportedException_whenCallClone_thenNoException() {

        CloneCatchClass original = new CloneCatchClass("x");

        assertDoesNotThrow(() -> original.clone());

        CloneCatchClass clone = original.clone();
        assertThat(clone)
                .hasSameClassAs(original);

        assertThat(clone.getClass())
                .isSameAs(original.getClass());

        assertThat(clone)
                .isNotSameAs(original);

        assertThat(clone.getValue())
                .isEqualTo("clone value");
    }


    /**
     * A class override {@link Object#clone()} and implements {@link Cloneable}
     */
    private static class CloneableClass implements Cloneable {

        @Override
        protected CloneableClass clone() throws CloneNotSupportedException {
            return (CloneableClass)super.clone();
        }
    }

    @Test
    void givenAClassOverrideCloneAndImplementsCloneable_whenCallClone_thenNoException() {

        CloneableClass cloneableClass = new CloneableClass();

        assertDoesNotThrow(() -> cloneableClass.clone());
    }

}
