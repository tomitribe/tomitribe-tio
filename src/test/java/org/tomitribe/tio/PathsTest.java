package org.tomitribe.tio;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.tomitribe.util.Files;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class PathsTest {

    @Test
    void childPath() {
        final File orange = Files.tmpdir();
        final File red = Files.tmpdir();
        final File file = new File(orange, "hot/air/ballon.txt");

        Assert.assertEquals("hot/air/ballon.txt", Paths.childPath(orange, file));
        Assert.assertEquals(file.getAbsolutePath(), Paths.childPath(red, file));

    }
}