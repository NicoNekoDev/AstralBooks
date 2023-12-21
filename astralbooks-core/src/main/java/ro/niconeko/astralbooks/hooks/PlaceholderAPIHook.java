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

package ro.niconeko.astralbooks.hooks;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.utils.MessageUtils;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class PlaceholderAPIHook extends Hook {

    @Override
    public void load(AstralBooksPlugin plugin) {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            MessageUtils.sendMessage(Bukkit.getConsoleSender(), "&aPlaceholderAPI found! &eTrying to hook into it...");
            super.enabled = true;
            MessageUtils.sendMessage(Bukkit.getConsoleSender(), "&aPlaceholderAPI hooked into PlaceholderAPI!");
        } else
            MessageUtils.sendMessage(Bukkit.getConsoleSender(), "&cPlaceholderAPI not found!");
    }

    public Function<Player, List<String>> parseList(List<String> list) {
        return player -> PlaceholderAPI.setPlaceholders(player, list);
    }

    public Function<Player, String> parse(String string) {
        return player -> PlaceholderAPI.setPlaceholders(player, string);
    }

    public Supplier<List<String>> parseListSup(List<String> list) {
        return () -> PlaceholderAPI.setPlaceholders(null, list);
    }

    public Supplier<String> parseSup(String string) {
        return () -> PlaceholderAPI.setPlaceholders(null, string);
    }
}
