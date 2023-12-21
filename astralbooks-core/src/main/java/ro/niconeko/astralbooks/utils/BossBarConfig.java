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
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

@Getter
public class BossBarConfig {
    private int in = 0, out = 0, stay = 20 * 6;
    private double progress = 100;
    private BarColor barColor = BarColor.RED;
    private BarStyle barStyle = BarStyle.SOLID;

    public BossBarConfig(String config) {
        String[] configs = config.split(",");
        for (String configItem : configs) {
            String[] keyValue = configItem.split(":");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().toLowerCase();
                String value = keyValue[1].trim();
                switch (key) {
                    case "in" -> {
                        try {
                            in = Integer.parseInt(value);
                        } catch (Exception ex) {
                            in = 0;
                        }
                    }
                    case "out" -> {
                        try {
                            out = Integer.parseInt(value);
                        } catch (Exception ex) {
                            out = 0;
                        }
                    }
                    case "stay" -> {
                        try {
                            stay = Integer.parseInt(value);
                        } catch (Exception ex) {
                            stay = 20 * 6;
                        }
                    }
                    case "progress" -> {
                        try {
                            progress = Double.parseDouble(value);
                            if (progress > 100) progress = 100;
                            if (progress < 0) progress = 0;
                        } catch (Exception ex) {
                            progress = 100;
                        }
                    }
                    case "color" -> barColor = BossBarUtils.toBarColor(value);
                    case "style" -> barStyle = BossBarUtils.toBarStyle(value);
                }
            }
        }
    }
}
