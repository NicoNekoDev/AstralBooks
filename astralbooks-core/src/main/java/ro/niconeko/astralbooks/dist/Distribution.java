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

package ro.niconeko.astralbooks.dist;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.clip.placeholderapi.PlaceholderAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ro.niconeko.astralbooks.AstralBooksPlugin;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

public abstract class Distribution {
    protected final Gson gson = new Gson();
    protected final AstralBooksPlugin plugin;
    protected final Field pagesField;

    public Distribution(final AstralBooksPlugin plugin, Field pagesField) {
        this.plugin = plugin;
        this.pagesField = pagesField;
        this.pagesField.setAccessible(true);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected final String placeholders(Player player, String str, Optional<NPC> optionalNPC) {
        if (!this.plugin.isPlaceholderAPIEnabled())
            return str;
        if (optionalNPC.isEmpty())
            return PlaceholderAPI.setPlaceholders(player, str);
        else {
            NPC npc = optionalNPC.get();
            return PlaceholderAPI.setPlaceholders(player, str).replace("%npc_name%", npc.getName())
                    .replace("%npc_id%", String.valueOf(npc.getId()))
                    .replace("%npc_loc_x%", String.valueOf(npc.getEntity().getLocation().getX()))
                    .replace("%npc_loc_y%", String.valueOf(npc.getEntity().getLocation().getY()))
                    .replace("%npc_loc_z%", String.valueOf(npc.getEntity().getLocation().getZ()))
                    .replace("%npc_loc_world%", npc.getEntity().getWorld().getName());
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected final List<String> placeholders(Player player, List<String> strList, Optional<NPC> optionalNPC) {
        if (!this.plugin.isPlaceholderAPIEnabled())
            return strList;
        if (optionalNPC.isEmpty())
            return PlaceholderAPI.setPlaceholders(player, strList);
        else {
            NPC npc = optionalNPC.get();
            return PlaceholderAPI.setPlaceholders(player, strList).stream().map(str -> str.replace("%npc_name%", npc.getName())
                    .replace("%npc_id%", String.valueOf(npc.getId()))
                    .replace("%npc_loc_x%", String.valueOf(npc.getEntity().getLocation().getX()))
                    .replace("%npc_loc_y%", String.valueOf(npc.getEntity().getLocation().getY()))
                    .replace("%npc_loc_z%", String.valueOf(npc.getEntity().getLocation().getZ()))
                    .replace("%npc_loc_world%", npc.getEntity().getWorld().getName())).toList();
        }
    }

    public abstract String getVersion();

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void setConfigComment(ConfigurationSection config, String path, Optional<List<String>> comments) {
    }

    public abstract void sendRightClick(Player player);

    public abstract JsonObject convertBookToJson(ItemStack book) throws IllegalAccessException;

    public abstract ItemStack convertJsonToBook(JsonObject jsonBook) throws IllegalAccessException;

    public abstract ItemStack applyPlaceholders(Player player, ItemStack book, NPC npc) throws IllegalAccessException;

    public abstract ItemStack applyPlaceholders(Player player, ItemStack book) throws IllegalAccessException;
}
