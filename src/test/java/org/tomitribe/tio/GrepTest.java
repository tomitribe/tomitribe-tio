/*
 * Copyright (c) 2019 Tomitribe and Contributors
 *
 *  See the NOTICE file(s) distributed with this work for additional
 *  information regarding copyright ownership.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.tio;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.tomitribe.util.Files;
import org.tomitribe.util.IO;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GrepTest {

    /**
     * When parens are used the group inside is used as the
     * match allowing us to select data from inside a string
     * discarding the surrounding parts we do not want
     */
    @Test
    public void grepWithParens() throws IOException {
        final Pattern compile = Pattern.compile(".(ht.p).");

        assertPattern(compile);
    }

    @Test
    public void grepNoParens() throws IOException {
        final Pattern compile = Pattern.compile("ht.p");

        assertPattern(compile);
    }

    public static void assertPattern(final Pattern compile) throws IOException {
        final File tmpdir = Files.tmpdir();
        final String s = "o-draft-service.yml\n" +
                "sre:\n" +
                "  notify: widget-dev@supertribe.pagerduty.com # Emergency notification email address\n" +
                "  dashboards: # Array of dashboard URLs\n" +
                "  - https://supertribe.wavefront.com/dashboard/red\n" +
                "  - https://supertribe.wavefront.com/dashboard/green\n" +
                "  - https://supertribe.wavefront.com/htAp/dashboard/blue\n" +
                "  - ht1p..ht2ps://supertribe.wavefront.com/dashboard/draft-service-ht3p-purple-staging\n" +
                "  pagerduty: https://supertribe.pagerduty.com/services/orange # URL to pagerduty service page\n" +
                "  slack_channel_id: FOO # Widget (Brown)\n" +
                "mailing_list: widget-dev@supertribe.com # TODO should become widget-announce@supertribe.com \n";

        final File file = new File(tmpdir, "greptest.yml");
        IO.copy(IO.read(s), file);

        final Dir dir = Dir.from(tmpdir);
        final Grep grep = Grep.builder().matcher(compile)
                .dir(dir)
                .build();


        final List<Match> matches = dir.searchFiles()
                .flatMap(grep::grep)
                .collect(Collectors.toList());


        final Iterator<Match> iterator = matches.iterator();

        assertMatch(tmpdir, file, iterator.next(),
                5, "http", "greptest.yml",
                "  - https://supertribe.wavefront.com/dashboard/red");
        assertMatch(tmpdir, file, iterator.next(),
                6, "http", "greptest.yml",
                "  - https://supertribe.wavefront.com/dashboard/green");
        assertMatch(tmpdir, file, iterator.next(),
                7, "http", "greptest.yml",
                "  - https://supertribe.wavefront.com/htAp/dashboard/blue");
        assertMatch(tmpdir, file, iterator.next(),
                7, "htAp", "greptest.yml",
                "  - https://supertribe.wavefront.com/htAp/dashboard/blue");
        assertMatch(tmpdir, file, iterator.next(),
                8, "ht1p", "greptest.yml",
                "  - ht1p..ht2ps://supertribe.wavefront.com/dashboard/draft-service-ht3p-purple-staging");
        assertMatch(tmpdir, file, iterator.next(),
                8, "ht2p", "greptest.yml",
                "  - ht1p..ht2ps://supertribe.wavefront.com/dashboard/draft-service-ht3p-purple-staging");
        assertMatch(tmpdir, file, iterator.next(),
                8, "ht3p", "greptest.yml",
                "  - ht1p..ht2ps://supertribe.wavefront.com/dashboard/draft-service-ht3p-purple-staging");
        assertMatch(tmpdir, file, iterator.next(),
                9, "http", "greptest.yml",
                "  pagerduty: https://supertribe.pagerduty.com/services/orange # URL to pagerduty service page");

        Assert.assertFalse(iterator.hasNext());
    }

    public static void assertMatch(final File tmpdir, final File file, final Match match, final int expectedLineNumber, final String expectedMatchText, final String expectedShortPath, final String expectedLineText) {
        Assert.assertEquals(expectedLineNumber, match.getLine().getNumber());
        Assert.assertEquals(expectedMatchText, match.getMatch());
        Assert.assertEquals(expectedShortPath, match.path());
        Assert.assertEquals(expectedLineText, match.getLine().getText());
        Assert.assertEquals(file.getAbsolutePath(), match.getFile().getAbsolutePath());
        Assert.assertEquals(tmpdir.getAbsolutePath(), match.getDir().dir().getAbsolutePath());
    }
}