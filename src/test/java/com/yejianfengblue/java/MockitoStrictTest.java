package com.yejianfengblue.java;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.mockito.Mockito.when;

/**
 * @author yejianfengblue
 */
// @MockitoSettings has @ExtendWith(MockitoExtension.class)
@MockitoSettings
class MockitoStrictTest {

    @Test
    void simpleMockitoStrictTest(@Mock List<String> mockedList) {

        when(mockedList.get(0)).thenReturn("zero");
        when(mockedList.get(1)).thenReturn("one");

        mockedList.get(0);
        mockedList.get(1);
    }
}
