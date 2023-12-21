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

import org.bukkit.command.CommandSender;
import ro.niconeko.astralbooks.managers.HooksManager;

public record Permission(String permission) {
    public boolean has(CommandSender sender) {
        if (HooksManager.PERMISSIONS.isEnabled()) {
            return HooksManager.PERMISSIONS.hasPermission(sender, permission);
        }
        return sender.hasPermission(permission);
    }
}
