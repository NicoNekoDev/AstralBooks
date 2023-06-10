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
import io.github.NicoNekoDev.SimpleTuples.func.TripletFunction;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Chunk;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.List;
import java.util.Optional;

public abstract class Distribution {
    protected final Gson gson = new Gson();
    private final String version;
    private boolean noNBTAPIRequired = false;
    private boolean persistentDataContainerEnabled = false;
    protected final TripletFunction<Player, String, Optional<NPC>, String> papiReplaceStr;
    protected final TripletFunction<Player, List<String>, Optional<NPC>, List<String>> papiReplaceStrList;

    public Distribution(final String version, TripletFunction<Player, String, Optional<NPC>, String> papiReplaceStr, TripletFunction<Player, List<String>, Optional<NPC>, List<String>> papiReplaceStrList) {
        this.version = version;
        this.papiReplaceStr = papiReplaceStr;
        this.papiReplaceStrList = papiReplaceStrList;
    }

    public Distribution(final String version, TripletFunction<Player, String, Optional<NPC>, String> papiReplaceStr, TripletFunction<Player, List<String>, Optional<NPC>, List<String>> papiReplaceStrList, boolean noNBTAPIRequired) {
        this(version, papiReplaceStr, papiReplaceStrList);
        this.noNBTAPIRequired = noNBTAPIRequired;
    }

    public Distribution(final String version, TripletFunction<Player, String, Optional<NPC>, String> papiReplaceStr, TripletFunction<Player, List<String>, Optional<NPC>, List<String>> papiReplaceStrList, boolean noNBTAPIRequired, boolean persistentDataContainerEnabled) {
        this(version, papiReplaceStr, papiReplaceStrList, noNBTAPIRequired);
        this.persistentDataContainerEnabled = noNBTAPIRequired;
    }

    public final boolean noNBTAPIRequired() {
        return this.noNBTAPIRequired;
    }

    public final boolean isPersistentDataContainerEnabled() {
        return this.persistentDataContainerEnabled;
    }

    public final String getVersion() {
        return this.version;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void setConfigComment(ConfigurationSection config, String path, Optional<List<String>> comments) {
    }

    public PersistentDataContainer getEntityDataContainer(Entity entity) {
        return null;
    }

    public PersistentDataContainer getChunkDataContainer(Chunk chunk) {
        return null;
    }

    public abstract void sendRightClick(Player player);

    public abstract void setItemInHand(Player player, ItemStack item);

    public abstract ItemStack getItemInHand(Player player);

    public abstract JsonObject convertBookToJson(ItemStack book) throws IllegalAccessException;

    public abstract ItemStack convertJsonToBook(JsonObject jsonBook) throws IllegalAccessException;

    public abstract ItemStack applyPlaceholders(Player player, ItemStack book, NPC npc) throws IllegalAccessException;

    public abstract ItemStack applyPlaceholders(Player player, ItemStack book) throws IllegalAccessException;

}
