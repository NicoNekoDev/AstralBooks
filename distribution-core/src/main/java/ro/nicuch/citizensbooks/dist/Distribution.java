/*

    CitizensBooks
    Copyright (c) 2021 @ Drăghiciu 'nicuch' Nicolae

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

import com.google.gson.JsonObject;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class Distribution {
    final String version;

    public Distribution(final String version) {
        this.version = version;
    }

    public final String getVersion() {
        return this.version;
    }

    public abstract void sendRightClick(Player player);

    public abstract void setItemInHand(Player player, ItemStack item);

    public abstract ItemStack getItemInHand(Player player);

    public abstract JsonObject convertBookToJson(ItemStack book);

    public abstract ItemStack convertJsonToBook(JsonObject jsonBook);
}
