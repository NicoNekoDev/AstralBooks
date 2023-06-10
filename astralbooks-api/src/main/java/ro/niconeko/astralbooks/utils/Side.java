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

public enum Side {
    LEFT("left_side"), RIGHT("right_side");

    private final String side;

    Side(String side) {
        this.side = side;
    }

    @Override
    public String toString() {
        return this.side;
    }

    public static Side fromString(String side) {
        if (side.equalsIgnoreCase("left_side"))
            return LEFT;
        return RIGHT;
    }
}
