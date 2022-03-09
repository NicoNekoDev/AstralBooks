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

package ro.nicuch.citizensbooks;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;
import ro.nicuch.citizensbooks.item.ItemData;
import ro.nicuch.citizensbooks.utils.CipherUtil;
import ro.nicuch.citizensbooks.utils.Message;
import ro.nicuch.citizensbooks.utils.PersistentKey;

import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.logging.Level;

public class CipherBookCommand implements CommandExecutor {
    private final CitizensBooksPlugin plugin;
    private final CitizensBooksAPI api;
    private final boolean lock;

    public CipherBookCommand(CitizensBooksPlugin plugin, boolean lock) {
        this.api = (this.plugin = plugin).getAPI();
        this.lock = lock;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        Optional<Player> player = this.isPlayer(sender) ? Optional.of((Player) sender) : Optional.empty();
        if (!this.plugin.getSettings().getBoolean("enable_encrypted_books", false)) {
            sender.sendMessage(this.plugin.getMessage(Message.ENCRYPT_IS_DISABLED));
            return true;
        }
        if (!(this.plugin.isNBTAPIEnabled() || this.api.noNBTAPIRequired())) {
            sender.sendMessage(this.plugin.getMessage(Message.NBTAPI_NOT_ENABLED));
            return true;
        }
        if (player.isEmpty()) {
            sender.sendMessage(this.plugin.getMessage(Message.CONSOLE_CANNOT_USE_COMMAND));
            return true;
        }
        if (this.lock) {
            if (!this.api.hasPermission(sender, "npcbook.command.encrypt")) {
                sender.sendMessage(this.plugin.getMessage(Message.NO_PERMISSION));
                return true;
            }
        } else {
            if (!this.api.hasPermission(sender, "npcbook.command.decrypt")) {
                sender.sendMessage(this.plugin.getMessage(Message.NO_PERMISSION));
                return true;
            }
        }
        if (args.length > 0) {
            if (!hasItemTypeInHand(player.get(), Material.WRITTEN_BOOK)) {
                sender.sendMessage(this.plugin.getMessage(Message.NO_WRITTEN_BOOK_IN_HAND));
                return true;
            }
            ItemStack book = this.getItemFromHand(player.get());
            BookMeta meta = (BookMeta) book.getItemMeta();
            if (this.plugin.getSettings().getBoolean("enable_encryption_on_copies", false)) {
                if (meta != null && meta.getGeneration() != BookMeta.Generation.ORIGINAL) {
                    sender.sendMessage(this.plugin.getMessage(Message.BOOK_NOT_ORIGINAL));
                    return true;
                }
            }

            try {
                String password = CipherUtil.sha256(args[0]);
                ItemData data = this.api.itemDataFactory(book);
                if (this.lock) {
                    if (data.hasStringKey(PersistentKey.BOOK_PASSWORD)) {
                        sender.sendMessage(this.plugin.getMessage(Message.BOOK_ALREADY_ENCRYPTED));
                        return true;
                    }
                    data.putString(PersistentKey.BOOK_PASSWORD, password);
                } else {
                    if (!data.hasStringKey(PersistentKey.BOOK_PASSWORD)) {
                        sender.sendMessage(this.plugin.getMessage(Message.BOOK_ALREADY_DECRYPTED));
                        return true;
                    }
                    String currentPassword = data.getString(PersistentKey.BOOK_PASSWORD);
                    if (!password.equals(currentPassword)) {
                        if (meta != null && meta.getAuthor() != null && !meta.getAuthor().equalsIgnoreCase(sender.getName())) {
                            if (this.plugin.getSettings().getBoolean("enable_encryption_fails", true)) {
                                int maxEncryptionFails = this.plugin.getSettings().getInt("encryption_fails", 10);
                                if (data.hasIntKey(PersistentKey.BOOK_PASSWORD_FAILS)) {
                                    int tries = data.getInt(PersistentKey.BOOK_PASSWORD_FAILS);
                                    if (tries >= maxEncryptionFails) {
                                        sender.sendMessage(this.plugin.getMessage(Message.BOOK_DECRYPTION_LOCKED));
                                        return true;
                                    }
                                    data.putInt(PersistentKey.BOOK_PASSWORD_FAILS, ++tries);
                                } else
                                    data.putInt(PersistentKey.BOOK_PASSWORD_FAILS, 1);
                                this.putItemInHand(player.get(), data.build()); // update item in hand
                            }
                        }
                        sender.sendMessage(this.plugin.getMessage(Message.BOOK_DECRYPT_FAILED)
                                .replace("%tries%", String.valueOf(this.plugin.getSettings().getInt("encryption_fails", 10) -
                                        (data.hasIntKey(PersistentKey.BOOK_PASSWORD_FAILS) ? data.getInt(PersistentKey.BOOK_PASSWORD_FAILS) : 0))));
                        return true;
                    }
                    data.removeKey(PersistentKey.BOOK_PASSWORD);
                }

                this.putItemInHand(player.get(), this.api.cipherBook(data.build(), password, this.lock));
                if (this.lock)
                    sender.sendMessage(this.plugin.getMessage(Message.BOOK_ENCRYPTED));
                else
                    sender.sendMessage(this.plugin.getMessage(Message.BOOK_DECRYPTED));
            } catch (NoSuchAlgorithmException ex) {
                player.get().sendMessage(ChatColor.DARK_RED + "" + ChatColor.ITALIC + "Something went wrong with the plugin! Please contact the server administrator(s) and tell them to look into the console!");
                this.plugin.getLogger().log(Level.WARNING, "Failed to find SHA256 algorithm!", ex);
                return true;
            }
        } else {
            if (this.lock)
                sender.sendMessage(this.plugin.getMessage(Message.USAGE_ENCRYPT));
            else
                sender.sendMessage(this.plugin.getMessage(Message.USAGE_DECRYPT));
        }
        return true;
    }

    @SuppressWarnings("SameParameterValue")
    private boolean hasItemTypeInHand(Player player, Material type) {
        ItemStack item = this.api.getDistribution().getItemInHand(player);
        if (item == null)
            return false;
        return item.getType() == type;
    }

    private boolean isPlayer(CommandSender sender) {
        return (sender instanceof Player);
    }

    private ItemStack getItemFromHand(Player player) {
        return this.api.getDistribution().getItemInHand(player);
    }

    private void putItemInHand(Player player, ItemStack item) {
        this.api.getDistribution().setItemInHand(player, item);
    }
}