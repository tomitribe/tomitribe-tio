package org.tomitribe.tio;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LineTest {

    @Test
    void testToString() {
        final Line line = new Line(123, "Orange submarine");
        Assert.assertEquals("123: Orange submarine", line.toString());
    }

    @Test
    void getNumber() {
        final Line line = new Line(123, "Orange submarine");
        Assert.assertEquals(123, line.getNumber());
    }

    @Test
    void getText() {
        final Line line = new Line(123, "Orange submarine");
        Assert.assertEquals("Orange submarine", line.getText());
    }
}