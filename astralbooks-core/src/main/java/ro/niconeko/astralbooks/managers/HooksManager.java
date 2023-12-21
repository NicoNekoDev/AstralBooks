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

package ro.niconeko.astralbooks.managers;

import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.hooks.AuthMeHook;
import ro.niconeko.astralbooks.hooks.CitizensHook;
import ro.niconeko.astralbooks.hooks.PermissionsHook;
import ro.niconeko.astralbooks.hooks.PlaceholderAPIHook;

public class HooksManager {
    public final static PlaceholderAPIHook PAPI = new PlaceholderAPIHook();
    public final static PermissionsHook PERMISSIONS = new PermissionsHook();
    public final static AuthMeHook AUTHME = new AuthMeHook();
    public final static CitizensHook CITIZENS = new CitizensHook();

    public static void load(AstralBooksPlugin plugin) {
        PAPI.load(plugin);
        PERMISSIONS.load(plugin);
        AUTHME.load(plugin);
        CITIZENS.load(plugin);
    }
}
