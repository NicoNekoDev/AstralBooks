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

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

public class BossBarUtils {
    public static BarStyle toBarStyle(String style) {
        return switch (style.toLowerCase()) {
            case "segmented_6", "seg_6" -> BarStyle.SEGMENTED_6;
            case "segmented_10", "seg_10" -> BarStyle.SEGMENTED_10;
            case "segmented_12", "seg_12" -> BarStyle.SEGMENTED_12;
            case "segmented_20", "seg_20" -> BarStyle.SEGMENTED_20;
            default -> BarStyle.SOLID;
        };
    }

    public static String fromBarStyle(BarStyle style) {
        return switch (style) {
            case SEGMENTED_6 -> "seg_6";
            case SEGMENTED_10 -> "seg_10";
            case SEGMENTED_12 -> "seg_12";
            case SEGMENTED_20 -> "seg_20";
            case SOLID -> "solid";
        };
    }

    public static BarColor toBarColor(String color) {
        return switch (color.toLowerCase()) {
            case "green" -> BarColor.GREEN;
            case "yellow" -> BarColor.YELLOW;
            case "blue" -> BarColor.BLUE;
            case "purple" -> BarColor.PURPLE;
            case "pink" -> BarColor.PINK;
            case "white" -> BarColor.WHITE;
            default -> BarColor.RED;
        };
    }

    public static String fromBarColor(BarColor color) {
        return switch (color) {
            case RED -> "red";
            case GREEN -> "green";
            case YELLOW -> "yellow";
            case BLUE -> "blue";
            case PURPLE -> "purple";
            case PINK -> "pink";
            case WHITE -> "white";
        };
    }
}
