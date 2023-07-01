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

package ro.niconeko.astralbooks.api;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ro.niconeko.astralbooks.utils.Side;
import ro.niconeko.astralbooks.utils.tuples.PairTuple;

import java.util.Set;

/**
 * AstralBooks API implementation
 */
public interface AstralBooksAPI {
    /**
     * Returns the book associated with the block
     *
     * @param block The block
     * @param side  The click side
     * @return The book
     */
    ItemStack getBookOfBlock(Block block, Side side);

    /**
     * Returns the book associated with the entity
     *
     * @param entity The entity
     * @param side   The click side
     * @return The book
     */
    ItemStack getBookOfEntity(Entity entity, Side side);

    /**
     * Removes all books from an entity
     *
     * @param entity The entity
     */
    void removeBookOfEntity(Entity entity);

    /**
     * Removes a book from an entity
     *
     * @param entity The entity
     * @param side   The click side
     */
    void removeBookOfEntity(Entity entity, Side side);

    /**
     * Removes all bocks from a block
     *
     * @param block The block
     */
    void removeBookOfBlock(Block block);

    /**
     * Removes a book from a block
     *
     * @param block The block
     * @param side  The click side
     */
    void removeBookOfBlock(Block block, Side side);

    /**
     * Puts a book on an entity
     *
     * @param entity The entity
     * @param book   The book
     * @param side   The click side
     */
    void putBookOnEntity(Entity entity, ItemStack book, Side side);

    /**
     * Puts a book on a block
     *
     * @param block The block
     * @param book  The book
     * @param side  The click side
     */
    void putBookOnBlock(Block block, ItemStack book, Side side);

    /**
     * Open a book for player
     *
     * @param player The player
     * @param book   The book
     * @return If successful
     * @throws NullPointerException     If the book is null
     * @throws IllegalArgumentException If the book is not really a book
     */
    boolean openBook(Player player, ItemStack book);

    /**
     * Sets the join book
     * Note: it won't set <i>join_book_enabled</i> in the settings
     *
     * @param book The book
     * @return If successful
     */
    boolean setJoinBook(ItemStack book);

    /**
     * Removes the join book
     *
     * @return If successful
     */
    boolean removeJoinBook();

    /**
     * Returns the join book
     *
     * @return The join book or null if it's not set
     */
    ItemStack getJoinBook();

    /**
     * Checks if the join book is set
     *
     * @return If the join book is set
     */
    boolean hasJoinBook();

    /**
     * Gets the last time the join book was set
     *
     * @return Last time the join book was set or <i>0</i> if it's not set
     */
    long getJoinBookLastChange();

    /**
     * Gets the last time a player have seen the join book
     *
     * @return Last time the join book was seen by the player or <i>0</i> if it's not set
     */
    long getJoinBookLastSeen(Player player);

    /**
     * Sets the last time a player have seen the join book
     *
     * @param player   The player
     * @param lastSeen The time in milliseconds
     * @return If successful, <i>false</i> otherwise or if the book is not set
     */
    boolean setJoinBookLastSeen(Player player, long lastSeen);

    /**
     * Checks if a player have ever seen the join book
     *
     * @param player The player
     * @return <i>true</i> if the player have seen the book, <i>false</i> otherwise or if the book is not set
     */
    boolean hasJoinBookLastSeen(Player player);

    /**
     * Puts a book to an NPC (please also check if Citizens is also enable before)
     *
     * @param npcId The ID of the NPC
     * @param side  The click side
     * @param book  The book
     * @return If successful
     */
    boolean putNPCBook(int npcId, Side side, ItemStack book);

    /**
     * Removes a book from an NPC (please also check if Citizens is also enable before)
     *
     * @param npcId The ID of the NPC
     * @param side  The click side
     * @return If successful
     */
    boolean removeNPCBook(int npcId, Side side);

    /**
     * Gets the book associated with the NPC (please also check if Citizens is also enable before)
     *
     * @param npcId       The ID of the NPC
     * @param side        The click side
     * @param defaultBook Default book
     * @return The book associated with the NPC or defaultBook otherwise
     */
    ItemStack getNPCBook(int npcId, Side side, ItemStack defaultBook);

    /**
     * Gets the book associated with the NPC (please also check if Citizens is also enable before)
     *
     * @param npcId The ID of the NPC
     * @param side  The click side
     * @return The book associated with the NPC or null otherwise
     */
    ItemStack getNPCBook(int npcId, Side side);

    /**
     * Checks if the NPC have a book (please also check if Citizens is also enable before)
     *
     * @param npcId The ID of the NPC
     * @param side  The click side
     * @return If the NPC have a book
     */
    boolean hasNPCBook(int npcId, Side side);

    /**
     * Creates a new filter book
     *
     * @param filterName The name of the filter
     * @param book       The book
     * @return If successful
     */
    boolean putFilterBook(String filterName, ItemStack book);

    /**
     * Removes the filter book
     *
     * @param filterName The name of the filter
     * @return If successful
     */
    boolean removeFilterBook(String filterName);

    /**
     * Gets the book associated with the filter name
     *
     * @param filterName  The name of the filter
     * @param defaultBook Default book
     * @return The book associated with the filter name, or defaultBook otherwise
     */
    ItemStack getFilterBook(String filterName, ItemStack defaultBook);

    /**
     * Gets the book associated with the filter name
     *
     * @param filterName The name of the filter
     * @return The book associated with the filter name, or null otherwise
     */
    ItemStack getFilterBook(String filterName);

    /**
     * Checks of a filter book exists for the given filter name
     *
     * @param filterName The name of the filter
     * @return If a book exists for the filter
     */
    boolean hasFilterBook(String filterName);

    /**
     * Gets all filter names installed/present
     *
     * @return All filter names
     */
    Set<String> getFilterNames();

    /**
     * Creates a new command for the given filter name with a permission
     *
     * @param cmd        The command
     * @param filterName The name of the filter
     * @param permission The permission
     * @return If successful
     */
    boolean putCommandFilter(String cmd, String filterName, @NotNull String permission);

    /**
     * Removes the command
     *
     * @param cmd The command
     * @return If successful
     */
    boolean removeCommandFilter(String cmd);

    /**
     * Gets filter name and permission from the given command
     *
     * @param cmd The command
     * @return A pair of filter name and permission
     */
    PairTuple<String, String> getCommandFilter(String cmd);

    /**
     * Checks if the given command exists
     *
     * @param cmd The command
     * @return If the given command exists
     */
    boolean hasCommandFilter(String cmd);

    /**
     * Gets all the commands
     *
     * @return All the commands
     */
    Set<String> getCommandFilterNames();
}
