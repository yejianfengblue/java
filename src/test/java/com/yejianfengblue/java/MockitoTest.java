package com.yejianfengblue.java;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author yejianfengblue
 */
@SuppressWarnings("unchecked")
class MockitoTest {

    Logger log = LoggerFactory.getLogger(getClass());

    @Test
    void givenMockObject_whenMakeInteractions_thenCanVerify() {

        // create a mock object
        List mockedList = mock(List.class);

        // use mock object
        mockedList.add("one");
        // actual nothing is added
        assertEquals(0, mockedList.size());
        mockedList.clear();

        // verify order doesn't matter here
        verify(mockedList).clear();
        verify(mockedList).add("one");
    }

    @Test
    void givenStubThenReturn_whenCall_thenReturnCorrectValue() {

        LinkedList mockedList = mock(LinkedList.class);

        // stubbing
        when(mockedList.get(0)).thenReturn("first");
        when(mockedList.get(1)).thenThrow(new RuntimeException());

        assertEquals("first", mockedList.get(0));
        assertThrows(RuntimeException.class, () -> mockedList.get(1));

        // null / 0 / false / empty collection if not stubbed
        assertNull(mockedList.get(999));
    }

    @Test
    void givenMockObject_whenStubUsingArgMatcher_thenReturnExpectedValue() {

        LinkedList mockedList = mock(LinkedList.class);

        when(mockedList.get(anyInt())).thenReturn("hi");

        assertEquals("hi", mockedList.get(1));
        // if verify with arg matcher, then do not call multiple times
//        assertEquals("hi", mockedList.get(999));

        verify(mockedList).get(anyInt());
    }

    @Test
    void verify_exactNumberOfInvocation_atLeast_never() {

        LinkedList mockedList = mock(LinkedList.class);

        mockedList.add("once");

        mockedList.add("twice");
        mockedList.add("twice");

        mockedList.add("three times");
        mockedList.add("three times");
        mockedList.add("three times");

        //exact number of invocations verification
        verify(mockedList, times(1)).add("once");
        verify(mockedList, times(2)).add("twice");
        verify(mockedList, times(3)).add("three times");

        //verification using never(). never() is an alias to times(0)
        verify(mockedList, times(0)).add("never happened");
        verify(mockedList, never()).add("never happened");

        verify(mockedList, atMostOnce()).add("once");
        verify(mockedList, atLeastOnce()).add("three times");
        verify(mockedList, atLeast(2)).add("three times");
        verify(mockedList, atMost(5)).add("three times");
    }

    @Test
    void stubVoidMethodWithException() {

        LinkedList mockedList = mock(LinkedList.class);

        doThrow(new RuntimeException()).when(mockedList).clear();

        assertThrows(RuntimeException.class, () -> mockedList.clear());
    }

    @Test
    void verifyInOrder() {

        LinkedList mockedList = mock(LinkedList.class);
        mockedList.add("1");
        mockedList.add("2");

        InOrder singleInOrder = inOrder(mockedList);
        singleInOrder.verify(mockedList).add("1");
        singleInOrder.verify(mockedList).add("2");

        List mockedList2 = mock(LinkedList.class);
        List mockedList3 = mock(LinkedList.class);
        mockedList2.add("two");
        mockedList3.add("three");

        InOrder multipleInOrder = inOrder(mockedList2, mockedList3);
        multipleInOrder.verify(mockedList2).add("two");
        multipleInOrder.verify(mockedList3).add("three");
    }

    @Test
    void verifyZeroInteractionsOnMock() {

        LinkedList mockedList = mock(LinkedList.class);
        verifyNoInteractions(mockedList);
    }

    @Test
    void stubConsecutiveCalls() {

        LinkedList mockedList = mock(LinkedList.class);
        when(mockedList.get(anyInt()))
                .thenReturn("1")
                .thenThrow(new RuntimeException())
                .thenReturn("3");

        assertEquals("1", mockedList.get(0));
        assertThrows(RuntimeException.class, () -> mockedList.get(111));
        assertEquals("3", mockedList.get(999));
    }

    @Test
    void stubWithCallback() {

        LinkedList mockedList = mock(LinkedList.class);
        when(mockedList.get(0)).thenAnswer(
                (Answer) invocation -> {
                    Object[] args = invocation.getArguments();
                    Object mock = invocation.getMock();
                    Method method = invocation.getMethod();
                    return "called with arguments: " + Arrays.toString(args);
                }
        );

        assertEquals("called with arguments: [0]", mockedList.get(0));
    }

    @Test
    void spyOnRealObject() {

        List realList = new LinkedList();
        // spy is actually a copy of the passed real instance
        List spyList = spy(realList);

        // optionally stub out some methods
        when(spyList.size()).thenReturn(100);

        // call real methods
        spyList.add("1");
        spyList.add("2");

        // but nothing is added to real object
        assertTrue(realList.isEmpty());

        assertEquals("1", spyList.get(0));
        assertEquals(100, spyList.size());

        // spy can be verified also
        verify(spyList).size();
        verify(spyList).add("1");
        verify(spyList).add("2");

        // exception instead of null in the mock case
        assertThrows(IndexOutOfBoundsException.class, () -> when(spyList.get(999)).thenReturn("999"));
        // use doReturn() in such case
        doReturn("999").when(spyList).get(999);
        assertEquals("999", spyList.get(999));
    }

    @Test
    void multipleStubbingThenReturn() {

        List mockedList = mock(List.class);

        when(mockedList.get(0)).thenReturn("0");
        // multiple stub with same matchers or args, override the previous one
        when(mockedList.get(0)).thenReturn("1");

        assertEquals("1", mockedList.get(0));
    }

    @Test
    void multipleStubbingThenThrowsAndThenReturn() {

        List mockedList = mock(List.class);

        when(mockedList.get(0)).thenThrow(new RuntimeException());
        // because mockedList.get(0) (exception-stubbed) is called
        assertThrows(RuntimeException.class, () -> when(mockedList.get(0)).thenReturn("0"));

        // to override, have to use doReturn()
        doReturn("0").when(mockedList).get(0);
        assertEquals("0", mockedList.get(0));
    }

    @Test
    void captureArgs() {

        Map<Integer, String> mockedMap = mock(Map.class);
        ArgumentCaptor<Integer> keyCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);

        mockedMap.put(1, "one");
        mockedMap.put(2, "two");

        // put(k, v) was called 2 times
        verify(mockedMap, times(2)).put(keyCaptor.capture(), valueCaptor.capture());

        // verify the captured arguments
        assertEquals(Arrays.asList(Integer.valueOf(1), Integer.valueOf(2)), keyCaptor.getAllValues());
        assertEquals(Arrays.asList("one", "two"), valueCaptor.getAllValues());
    }

    @Test
    void behaviorDrivenDevelopmentAlias() {

        List<String> mockedList = mock(List.class);

        // given
        BDDMockito.given(mockedList.get(0)).willReturn("hi");

        // when
        String result = mockedList.get(0);

        // then
        BDDMockito.then(mockedList).should(times(1)).get(0);
        assertEquals("hi", result);
    }
}
