package org.tomitribe.tio;

import org.junit.Assert;

public class MatchAsserts {

    public static void assertMatch(final int line, final String expected, final Match match) {
        Assert.assertEquals(line, match.getLine().getNumber());
        Assert.assertEquals(expected, match.getMatch());
    }
}
