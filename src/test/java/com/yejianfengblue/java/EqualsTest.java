package com.yejianfengblue.java;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <p>
 * This is the equals method test that examines
 * <a href = "https://www.artima.com/lejava/articles/equality.html">How to Write an Equality Method in Java</a>
 * <p>
 * {@link Object#equals(Object)} states that the equals method implements an equivalence relation
 * on non-null object references: (below list is copied from jdk 12 javadoc)
 * <ul>
 * <li>It is <i>reflexive</i>: for any non-null reference value
 *     {@code x}, {@code x.equals(x)} should return
 *     {@code true}.
 * <li>It is <i>symmetric</i>: for any non-null reference values
 *     {@code x} and {@code y}, {@code x.equals(y)}
 *     should return {@code true} if and only if
 *     {@code y.equals(x)} returns {@code true}.
 * <li>It is <i>transitive</i>: for any non-null reference values
 *     {@code x}, {@code y}, and {@code z}, if
 *     {@code x.equals(y)} returns {@code true} and
 *     {@code y.equals(z)} returns {@code true}, then
 *     {@code x.equals(z)} should return {@code true}.
 * <li>It is <i>consistent</i>: for any non-null reference values
 *     {@code x} and {@code y}, multiple invocations of
 *     {@code x.equals(y)} consistently return {@code true}
 *     or consistently return {@code false}, provided no
 *     information used in {@code equals} comparisons on the
 *     objects is modified.
 * <li>For any non-null reference value {@code x},
 *     {@code x.equals(null)} should return {@code false}.
 * </ul>
 *
 * @author yejianfengblue
 */
class EqualsTest {

    @Nested
    @DisplayName("Pitfall #1: define equals with wrong signature Point.equals(Point)")
    class Pitfall1_defineEqualsWithWrongSignature {

        /**
         * A Point with coordinate x and y whose defined equals() has wrong signature equals(Point)
         */
        @Data
        private class Point {

            private final int x;
            private final int y;

            /**
             * A seemingly obvious, but wrong way to define equals()
             * which only compare the field value.
             * It is just an overload rather then override.
             * Overloading in Java is resolved by the static type of argument.
             *
             * @return true only if both x and y have same value
             */
            boolean equals(Point other) {
                return (this.getX() == other.getX() && this.getY() == other.getY());
            }
        }

        @Test
        @DisplayName("equals() works when compare between plain Point")
        @Order(1)
        void givenPoint_whenCallEquals_thenWorkOk() {

            Point p1 = new Point(1, 2);
            Point p2 = new Point(1, 2);

            assertTrue(p1.equals(p2));
            assertTrue(p2.equals(p1));

            Point q = new Point(2, 3);
            assertFalse(p1.equals(q));
            assertFalse(p2.equals(q));
        }

        @Test
        @DisplayName("equals() troubles when put Point into a collection")
        @Order(2)
        void givenHashSetOfPointWithOnePointAdded_whenCheckHashSetContainsAnotherPointObjectWithSameFieldValue_thenNotCantain() {

            HashSet<Point> points = new HashSet<>();
            Point p1 = new Point(1, 2);
            Point p2 = new Point(1, 2);

            points.add(p1);
            /* HashSet.contains uses generic equals(Object) instead of overloaded variant equals(Point).
             * The default Object.equals() return true only both refer to the same object,
             * while p1 and p2 are diff objects with same field value.
             */
            assertFalse(points.contains(p2));
        }

        @Test
        @DisplayName("Point.equals(Object) invokes the default Object.equals(Object)")
        @Order(3)
        void givenAliasOfClassObject_whenCallPointEqualsObject_thenDefaultObjectEqualsMethodIsCalled() {

            Point p1 = new Point(1, 2);
            Point p2 = new Point(1, 2);

            assertTrue(p1.equals(p2));
            assertTrue(p2.equals(p1));

            // define an alias of Object
            Object p2a = p2;
            // Point.equals(Object) will call default Object.equals, because we only define Point.equals(Point)
            assertTrue(p2.equals(p2a));
            assertFalse(p1.equals(p2a));
        }
    }

    /*
     * Object.hashCode() states:
     * If two objects are equal according to the equals(Object) method, then calling the hashCode() method
     * on each of the two objects must produce the same integer result.
     */
    @Nested
    @DisplayName("Pitfall #2: define equals without also defining hashcode")
    class Pitfall2_defineEqualsWithoutAlsoDefiningHashCode{

        /**
         * A Point with coordinate x and y whose defined equals() has correct signature equals(Object)
         */
        @Data
        private class Point {

            private final int x;
            private final int y;

            // To fix the mistake in pitfall #1, we have below overridden version
            @Override
            public boolean equals(Object other) {

                if (other instanceof Point) {
                    Point that = (Point)other;
                    return (this.getX() == that.getX() && this.getY() == that.getY());
                }
                return false;
            }
        }

        @Test
        @DisplayName("Point.equals(Object) invokes the redefined Point.equals(Object)")
        @Order(1)
        // repeat the test in Pitfall1_defineEqualsWithWrongSignature.givenAliasOfClassObject_whenCallPointEqualsObject_thenDefaultObjectEqualsMethodIsCalled()
        void givenAliasOfClassObject_whenCallPointEqualsObject_thenRedefinedEqualsMethodIsCalled() {

            Point p1 = new Point(1, 2);
            Point p2 = new Point(1, 2);

            assertTrue(p1.equals(p2));
            assertTrue(p2.equals(p1));

            Object p2a = p2;
            assertTrue(p2.equals(p2a));
            // now we get true as expected
            assertTrue(p1.equals(p2a));
        }

        @Test
        @DisplayName("equals() still troubles when put Point into a HashSet")
        @Order(2)
        void givenHashSetOfPointWithOnePointAdded_whenCheckHashSetContainsAnotherPointObjectWithSameFieldValue_thenNotCantain() {

            HashSet<Point> points = new HashSet<>();
            Point p1 = new Point(1, 2);
            Point p2 = new Point(1, 2);

            points.add(p1);
            /* HashSet firstly determines a hash bucket to look in and compare the given element with
             * all elements in that bucket.
             * The default Object.hashCode() is some transformation of the address of the allocated object.
             * With a high probability p1 and p2 belong to diff buckets.
             */
            // if below fails, just run again until p1 and p2 have diff hashCode
            assertFalse(points.contains(p2));
        }
    }

    /* Now that we redefine equals(Object) and hashCode together, we still have trouble
    in terms of mutable field and work with HashSet */
    @Nested
    class Pitfall3_defineEqualsInTermsOfMutableFields {

        /**
         * A Point with coordinate x and y with redefined equals and hashCode
         */
        @Data @AllArgsConstructor
        private class Point {

            private int x;
            private int y;

            // To fix the mistake in pitfall #2, we have bel
            @Override
            public boolean equals(Object other) {

                if (other instanceof Point) {
                    Point that = (Point)other;
                    return (this.getX() == that.getX() && this.getY() == that.getY());
                }
                return false;
            }

            @Override public int hashCode() {
                return (41 * (41 + getX()) + getY());
            }
        }

        @Test
        @DisplayName("After update field, HashSet.contains() doesn't check original hash bucket")
        void givenHashSetWithOnePointAdded_whenUpdateField_thenHashSetContainsYieldFalseWhileItIsFoundDuringIteration() {

            Point p = new Point(1, 2);
            HashSet<Point> points = new HashSet<>();
            points.add(p);

            assertTrue(points.contains(p));

            // now update field value
            p.setX(p.getX() + 1);
            assertFalse(points.contains(p));  // false with high probability

            // If we iterate the HashSet, the p be among the set
            boolean containP = false;
            for (Point point : points) {

                if (point.equals(p)) {
                    containP = true;
                    break;
                }
            }
            assertTrue(containP);

            /* The cause is the new p stays in the wrong hash bucket.
             In other words, its original hash bucket no longer corresponds to the new value of its hash code.
             When equals and hashCode depend on mutable field, if put objects into collections, never modify
             the depended-on state.
             If we need a comparison between updated object against HashSet, better define a comparison method
             equalContents or equalValues.
             */
        }
    }

    @Nested
    class Pitfall4_symmetryViolatedWhenConsiderSubclass {

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        private class Point {

            private int x;
            private int y;

            @Override
            public boolean equals(Object other) {

                if (other instanceof Point) {
                    Point that = (Point)other;
                    return (this.getX() == that.getX() && this.getY() == that.getY());
                }
                return false;
            }

            @Override public int hashCode() {
                return (41 * (41 + getX()) + getY());
            }
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        private class ColoredPoint extends Point {

            private String color;

            ColoredPoint(int x, int y, String color) {
                super(x, y);
                this.color = color;
            }

            @Override
            public boolean equals(Object other) {

                if (other instanceof ColoredPoint) {
                    ColoredPoint that = (ColoredPoint)other;
                    return this.color.equals(that.color) && super.equals(that);
                }
                return false;
            }

            /* hashCode is not necessary because ColoredPoint.equals(Object) is stricter than Point.equals(Object),
               the contract for hashCode stays valid. In other words, if two colored points are equal,
               they must have the same coordinates, so their hash codes are guaranteed to be equal as well. */
        }

        @Test
        void givenOnePointAndOneColoredPoint_whenInvokeEqualsBetweenColoredPointAndPoint_thenSymmetryViolated() {

            Point p = new Point(1, 2);
            ColoredPoint cp = new ColoredPoint(1, 2, "RED");

            assertTrue(p.equals(cp));
            assertFalse(cp.equals(p));
        }
        /* One solution is making the equals relation more general, i.e.,
        further check if arg Object is instance of Point, if they have same coordinate,
        the equals return true. */
    }

    @Nested
    class Pitfall4_transitivityViolatedWhenConsiderSubclass {

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        private class Point {

            private int x;
            private int y;

            @Override
            public boolean equals(Object other) {

                if (other instanceof Point) {
                    Point that = (Point)other;
                    return (this.getX() == that.getX() && this.getY() == that.getY());
                }
                return false;
            }

            @Override public int hashCode() {
                return (41 * (41 + getX()) + getY());
            }
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        private class ColoredPoint extends Point {

            private String color;

            ColoredPoint(int x, int y, String color) {
                super(x, y);
                this.color = color;
            }

            // now we check both subclass and superclass
            @Override
            public boolean equals(Object other) {

                if (other instanceof ColoredPoint) {
                    ColoredPoint that = (ColoredPoint)other;
                    return this.color.equals(that.color) && super.equals(that);
                } else if (other instanceof Point) {
                    Point that = (Point)other;
                    return that.equals(this);
                }
                return false;
            }
        }

        @Test
        void givenOnePointAndTwoColoredPointWithDiffColor_whenCompare_thenTransitivityViolated() {

            Point p = new Point(1, 2);
            ColoredPoint redP = new ColoredPoint(1, 2, "RED");
            ColoredPoint blueP = new ColoredPoint(1, 2, "BLUE");

            assertTrue(redP.equals(p));
            assertTrue(p.equals(blueP));
            assertFalse(redP.equals(blueP));
        }
        /* Making the equals relation more general doesn't work, now try to make is stricter, i.e.,
        always treat objects of diff classes as diff. */
    }

    @Nested
    class alwaysTreatObjectsOfDiffClassesAsDiff{

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        private class Point {

            private int x;
            private int y;

            @Override
            public boolean equals(Object other) {

                if (other instanceof Point) {
                    Point that = (Point)other;
                    return (this.getClass().equals(that.getClass())
                            && this.getX() == that.getX()
                            && this.getY() == that.getY());
                }
                return false;
            }

            @Override public int hashCode() {
                return (41 * (41 + getX()) + getY());
            }
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        private class ColoredPoint extends Point {

            private String color;

            ColoredPoint(int x, int y, String color) {
                super(x, y);
                this.color = color;
            }

            // now we check both subclass and superclass
            @Override
            public boolean equals(Object other) {

                if (other instanceof ColoredPoint) {
                    ColoredPoint that = (ColoredPoint)other;
                    return this.color.equals(that.color) && super.equals(that);
                } else if (other instanceof Point) {
                    Point that = (Point)other;
                    return that.equals(this);
                }
                return false;
            }
        }

        @Test
        void givenOnePointAndTwoColoredPointWithDiffColor_whenCompare_thenTransitivityViolated() {

            Point p = new Point(1, 2);
            ColoredPoint redP = new ColoredPoint(1, 2, "RED");
            ColoredPoint blueP = new ColoredPoint(1, 2, "BLUE");

            assertFalse(redP.equals(p));
            assertFalse(p.equals(blueP));
            assertFalse(redP.equals(blueP));
        }

    }
}
