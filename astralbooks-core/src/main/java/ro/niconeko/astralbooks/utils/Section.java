/*
 *     CitizensBooks
 *     Copyright (c) 2023 @ Drăghiciu 'NicoNekoDev' Nicolae
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package ro.niconeko.astralbooks.utils;

import lombok.Getter;

import java.util.List;

@Getter
public abstract class Section {
    private final String name;
    private final List<String> comments;
    private final List<String> inlineComments;

    public Section(final String name, List<String> comments, List<String> inlineComments) {
        this.name = name;
        this.comments = comments;
        this.inlineComments = inlineComments;
    }

    public Section(final String name, List<String> comments) {
        this(name, comments, List.of());
    }

    public Section(final String name) {
        this(name, List.of());
    }
}
