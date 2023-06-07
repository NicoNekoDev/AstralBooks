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

package ro.niconeko.astralbooks.persistent.chunk;

import io.github.NicoNekoDev.SimpleTuples.Pair;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import ro.niconeko.astralbooks.AstralBooksCore;
import ro.niconeko.astralbooks.dist.Distribution;

import java.util.Map;

public interface ChunkData {

    Map<Block, Pair<ItemStack, ItemStack>> loadChunk(Distribution distribution) throws IllegalAccessException;

    void unloadChunk(Distribution distribution, AstralBooksCore core) throws IllegalAccessException;

}
