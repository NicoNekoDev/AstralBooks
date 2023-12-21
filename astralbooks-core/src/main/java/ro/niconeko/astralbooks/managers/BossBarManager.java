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

import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.utils.BossBarConfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class BossBarManager {
    private static final List<BossBarPlayer> players = new ArrayList<>();
    private static BukkitTask task;

    public static void load(AstralBooksPlugin plugin) {
        if (task != null)
            task.cancel();
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Iterator<BossBarPlayer> iter = players.iterator();
            while (iter.hasNext()) {
                if (!iter.next().tick())
                    continue;
                iter.remove();
            }
        }, 1, 1);
    }

    public static void sendBossBar(Player player, BossBarConfig config, Function<Player, String> message) {
        players.add(new BossBarPlayer(player, config, message));
    }

    public static class BossBarPlayer {
        private final Player player;
        private final BossBar bar;
        private final Function<Player, String> message;
        private int fadeIn, stay, fadeOut;
        private final int fadeInTotal, fadeOutTotal;
        private final double maxProgress;

        public BossBarPlayer(Player player, BossBarConfig config, Function<Player, String> message) {
            this.player = player;
            this.message = message;
            this.fadeInTotal = this.fadeIn = config.getIn();
            this.stay = config.getStay();
            this.fadeOutTotal = this.fadeOut = config.getOut();
            this.maxProgress = config.getProgress();
            this.bar = Bukkit.createBossBar(this.message.apply(this.player), config.getBarColor(), config.getBarStyle());
        }

        public boolean tick() { // return 'true' if ended
            this.bar.setTitle(this.message.apply(player));
            if (this.fadeIn > 0) {
                this.fadeIn--;
                this.bar.setProgress((((this.fadeInTotal - (double) this.fadeIn) / this.fadeInTotal) * maxProgress) / 100);
                this.bar.addPlayer(this.player);
                return false;
            } else if (this.stay > 0) {
                this.stay--;
                this.bar.setProgress(maxProgress / 100);
                this.bar.addPlayer(this.player);
                return false;
            } else if (this.fadeOut > 0) {
                this.fadeOut--;
                this.bar.setProgress(((this.fadeOut / (double) this.fadeOutTotal) * maxProgress) / 100);
                this.bar.addPlayer(this.player);
                return false;
            } else {
                this.bar.removeAll();
                return true;
            }
        }
    }
}
