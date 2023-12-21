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

import org.bukkit.Bukkit;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.listeners.AuthmeActions;
import ro.niconeko.astralbooks.utils.MessageUtils;

public class AuthMeHook extends Hook {
    @Override
    public void load(AstralBooksPlugin plugin) {
        if (Bukkit.getPluginManager().isPluginEnabled("Authme")) {
            MessageUtils.sendMessage(Bukkit.getConsoleSender(), "&aAuthme found! &eTrying to hook into it...");
            Bukkit.getPluginManager().registerEvents(new AuthmeActions(plugin), plugin);
            super.enabled = true;
            MessageUtils.sendMessage(Bukkit.getConsoleSender(), "&aSuccessfully hooked into Authme!");
        } else
            MessageUtils.sendMessage(Bukkit.getConsoleSender(), "&cAuthme not found!");
    }
}