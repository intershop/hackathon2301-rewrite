package com.example;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;

import com.intershop.tools.test.QualifiedNameGenerator;

@DisplayNameGeneration(QualifiedNameGenerator.class)
public class DummyComparerTest
{

    @Test
    public void compareEqual()
    {
        DummyComparer dummyComparer = new DummyComparer();
        assertEquals(0,dummyComparer.compare("test", "test"));
    }
}
