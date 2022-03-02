/*

    CitizensBooks
    Copyright (c) 2022 @ Drăghiciu 'NicoNekoDev' Nicolae

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */

package ro.nicuch.citizensbooks.dist;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.NicoNekoDev.SimpleTuples.func.TripletFunction;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public abstract class Distribution {
    protected final Gson gson = new Gson();
    private final String version;
    protected final TripletFunction<Player, String, Optional<NPC>, String> papiReplaceStr;
    protected final TripletFunction<Player, List<String>, Optional<NPC>, List<String>> papiReplaceStrList;

    public Distribution(final String version, TripletFunction<Player, String, Optional<NPC>, String> papiReplaceStr, TripletFunction<Player, List<String>, Optional<NPC>, List<String>> papiReplaceStrList) {
        this.version = version;
        this.papiReplaceStr = papiReplaceStr;
        this.papiReplaceStrList = papiReplaceStrList;
    }

    public final String getVersion() {
        return this.version;
    }

    public abstract void sendRightClick(Player player);

    public abstract void setItemInHand(Player player, ItemStack item);

    public abstract ItemStack getItemInHand(Player player);

    public abstract JsonObject convertBookToJson(ItemStack book);

    public abstract ItemStack convertJsonToBook(JsonObject jsonBook);

    public abstract ItemStack applyPlaceholders(Player player, ItemStack book, NPC npc);

    public abstract ItemStack applyPlaceholders(Player player, ItemStack book);
}
