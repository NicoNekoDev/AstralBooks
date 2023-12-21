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

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.utils.MessageUtils;

public class PermissionsHook extends Hook {
    private Permission permissions;

    @Override
    public void load(AstralBooksPlugin plugin) {
        // Load Vault
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            MessageUtils.sendMessage(Bukkit.getConsoleSender(), "&aVault found! &eTrying to hook into it...");
            RegisteredServiceProvider<Permission> provider = plugin.getServer().getServicesManager().getRegistration(Permission.class);
            if (provider != null) {
                super.enabled = true;
                this.permissions = provider.getProvider();
                MessageUtils.sendMessage(Bukkit.getConsoleSender(), "&aSuccessfully hooked into Vault!");
            } else
                MessageUtils.sendMessage(Bukkit.getConsoleSender(), "&cFailed to hook into Vault!");
        } else
            MessageUtils.sendMessage(Bukkit.getConsoleSender(), "&cVault not found!");
    }

    public boolean hasPermission(CommandSender sender, String permission) {
        return this.permissions.has(sender, permission);
    }
}
