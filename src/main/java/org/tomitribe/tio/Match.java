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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.File;

@Data
@Builder(builderClassName = "Progress", toBuilder = true)
@AllArgsConstructor
public class Match {
    private final Dir dir;
    private final File file;
    private final Line line;
    private final String match;

    public Unique<Match> uniqueMatchText() {
        return Unique.of(this, Match::getMatch);
    }

    public String path() {
        final File parent = dir.dir();
        final File file = this.file;

        return Paths.childPath(parent, file);
    }

    public String userPath() {
        return Paths.childPath(new File(""), this.file);
    }

    public String toString() {
        return String.format("%s:%s:%s:%s",
                Paths.childPath(new File(""), file),
                line.getNumber(),
                match,
                line.getText()
        );
    }

    public static class Progress {
    }
}
