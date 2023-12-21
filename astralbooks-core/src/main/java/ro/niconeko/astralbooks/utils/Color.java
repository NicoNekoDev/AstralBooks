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

import net.md_5.bungee.api.ChatColor;

public class Color {
    private final int rgb;

    public Color(String str) {
        this(Integer.valueOf(str.substring(0, 2), 16),
                Integer.valueOf(str.substring(2, 4), 16),
                Integer.valueOf(str.substring(4, 6), 16));
    }

    public Color(int red, int green, int blue) {
        this.rgb = (red << 16) + (green << 8) + blue;
    }

    public int red() {
        return (rgb >> 16) & 0xFF;
    }

    public int green() {
        return (rgb >> 8) & 0xFF;
    }

    public int blue() {
        return rgb & 0xFF;
    }

    public String hex() {
        return String.format("%06x", rgb);
    }

    public String minecraftHex() {
        String hex = hex();
        return ChatColor.COLOR_CHAR + "x" + ChatColor.COLOR_CHAR + hex.charAt(0) +
                ChatColor.COLOR_CHAR + hex.charAt(1) + ChatColor.COLOR_CHAR + hex.charAt(2) +
                ChatColor.COLOR_CHAR + hex.charAt(3) + ChatColor.COLOR_CHAR + hex.charAt(4) +
                ChatColor.COLOR_CHAR + hex.charAt(5);
    }
}