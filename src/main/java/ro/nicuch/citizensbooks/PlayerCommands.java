package ro.nicuch.citizensbooks;

import java.util.Collections;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.util.StringUtil;

import com.google.common.collect.Lists;

import net.milkbowl.vault.permission.Permission;

public class PlayerCommands implements TabExecutor {
	private final CitizensBooks plugin;
	private final CitizensBooksAPI api;

	public PlayerCommands(CitizensBooks plugin) {
		api = (this.plugin = plugin).getAPI();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Permission perm = this.plugin.getPermission();
		if (args.length > 0) {
			switch (args[0]) {
			case "about":
				this.sendAbout(sender);
				break;
			case "reload":
				if (perm.has(sender, "npcbook.command.reload")) {
					this.plugin.reloadSettings();
					sender.sendMessage(this.plugin.getMessage("lang.config_reloaded", LangDefaults.config_reloaded));
				} else
					sender.sendMessage(this.plugin.getMessage("lang.no_permission", LangDefaults.no_permission));
				break;
			case "set":
				if (perm.has(sender, "npcbook.command.set")) {
					sender.sendMessage(
							this.plugin.getMessage("lang.citizens_not_enabled", LangDefaults.citizens_not_enabled));
					break;
				} else
					sender.sendMessage(this.plugin.getMessage("lang.no_permission", LangDefaults.no_permission));
				break;
			case "remove":
				if (perm.has(sender, "npcbook.command.remove")) {
					sender.sendMessage(
							this.plugin.getMessage("lang.citizens_not_enabled", LangDefaults.citizens_not_enabled));
					break;
				} else
					sender.sendMessage(this.plugin.getMessage("lang.no_permission", LangDefaults.no_permission));
				break;
			case "getbook":
				if (perm.has(sender, "npcbook.command.getbook")) {
					sender.sendMessage(
							this.plugin.getMessage("lang.citizens_not_enabled", LangDefaults.citizens_not_enabled));
					break;
				} else
					sender.sendMessage(this.plugin.getMessage("lang.no_permission", LangDefaults.no_permission));
				break;
			case "openbook":
				if (perm.has(sender, "npcbook.command.getbook")) {
					if (this.hasBookInHand((Player) sender)) {
						this.openBook((Player) sender, this.getBookFromHand((Player) sender));
					} else
						sender.sendMessage(
								this.plugin.getMessage("lang.no_book_in_hand", LangDefaults.no_book_in_hand));
				} else
					sender.sendMessage(this.plugin.getMessage("lang.no_permission", LangDefaults.no_permission));
				break;
			case "setcmd":
				if (perm.has(sender, "npcbook.command.setcmd")) {
					if (args.length > 2) {
						this.plugin.getConfig().set("commands." + args[1], args[2]);
						this.plugin.saveSettings();
						sender.sendMessage(this.plugin
								.getMessage("lang.set_custom_command_successfully",
										LangDefaults.set_custom_command_successfully)
								.replaceAll("%command%", args[1]).replaceAll("%filter_name%", args[2]));
					} else
						sender.sendMessage(this.plugin.getMessage("lang.usage.setcmd", LangDefaults.usage_setcmd));
				} else
					sender.sendMessage(this.plugin.getMessage("lang.no_permission", LangDefaults.no_permission));
				break;
			case "remcmd":
				if (perm.has(sender, "npcbook.command.remcmd")) {
					if (args.length > 1) {
						this.plugin.getConfig().set("commands." + args[1], null);
						this.plugin.saveSettings();
						sender.sendMessage(this.plugin
								.getMessage("lang.remove_custom_command_successfully",
										LangDefaults.remove_custom_command_successfully)
								.replaceAll("%command%", args[1]));
					} else
						sender.sendMessage(this.plugin.getMessage("lang.usage.remcmd", LangDefaults.usage_remcmd));
				} else
					sender.sendMessage(this.plugin.getMessage("lang.no_permission", LangDefaults.no_permission));
				break;
			case "filter":
				if (args.length > 1) {
					switch (args[1]) {
					case "set":
						if (perm.has(sender, "npcbook.command.filter.set")) {
							if (args.length > 2) {
								if (this.hasBookInHand((Player) sender)) {
									this.api.createFilter(args[2], this.getBookFromHand((Player) sender));
									sender.sendMessage(
											this.plugin.getMessage("lang.filter_saved", LangDefaults.filter_saved)
													.replaceAll("%filter_name%", args[2]));
								} else
									sender.sendMessage(this.plugin.getMessage("lang.no_book_in_hand",
											LangDefaults.no_book_in_hand));
							} else
								sender.sendMessage(
										this.plugin.getMessage("lang.usage.filter.set", LangDefaults.usage_filter_set));
						} else
							sender.sendMessage(
									this.plugin.getMessage("lang.no_permission", LangDefaults.no_permission));
						break;
					case "remove":
						if (perm.has(sender, "npcbook.command.filter.remove")) {
							if (args.length > 2) {
								this.api.removeFilter(args[2]);
								sender.sendMessage(
										this.plugin.getMessage("lang.filter_removed", LangDefaults.filter_removed)
												.replaceAll("%filter_name%", args[2]));
							} else
								sender.sendMessage(this.plugin.getMessage("lang.usage.filter.remove",
										LangDefaults.usage_filter_remove));
						} else
							sender.sendMessage(
									this.plugin.getMessage("lang.no_permission", LangDefaults.no_permission));
						break;
					case "getbook":
						if (perm.has(sender, "npcbook.command.filter.getbook")) {
							if (args.length > 2) {
								if (this.api.hasFilter(args[2])) {
									ItemStack book = this.api.getFilter(args[2]);
									((Player) sender).getInventory().addItem(book);
									sender.sendMessage(
											this.plugin.getMessage("lang.book_recived", LangDefaults.book_recived));
								} else
									sender.sendMessage(this.plugin.getMessage("lang.no_book_for_filter",
											LangDefaults.no_book_for_filter));
							} else
								sender.sendMessage(this.plugin.getMessage("lang.usage.filter.getbook",
										LangDefaults.usage_filter_getbook));
						} else
							sender.sendMessage(
									this.plugin.getMessage("lang.no_permission", LangDefaults.no_permission));
						break;
					default:
						this.sendFilterHelp(sender);
						break;
					}
				} else
					this.sendFilterHelp(sender);
				break;
			default:
				if (perm.has(sender, "npcbook.command.help"))
					this.sendHelp(sender);
				else
					this.sendAbout(sender);
				break;
			}
		} else {
			if (perm.has(sender, "npcbook.command.help"))
				this.sendHelp(sender);
			else
				this.sendAbout(sender);
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> completions = Lists.newArrayList();
		List<String> commands = Lists.newArrayList();
		Permission perm = this.plugin.getPermission();
		if (args.length == 1) {
			if (perm.has(sender, "npcbook.command.set"))
				commands.add("set");
			if (perm.has(sender, "npcbook.command.remove"))
				commands.add("remove");
			if (perm.has(sender, "npcbook.command.getbook"))
				commands.add("getbook");
			if (perm.has(sender, "npcbook.command.openbook"))
				commands.add("openbook");
			if (perm.has(sender, "npcbook.command.filter"))
				commands.add("filter");
			if (perm.has(sender, "npcbook.command.setcmd"))
				commands.add("setcmd");
			if (perm.has(sender, "npcbook.command.remcmd"))
				commands.add("remcmd");
			StringUtil.copyPartialMatches(args[0], commands, completions);
		} else if (args.length == 2) {
			if (args[0].equals("filter")) {
				if (perm.has(sender, "npcbook.command.filter.set"))
					commands.add("set");
				if (perm.has(sender, "npcbook.command.filter.remove"))
					commands.add("remove");
				if (perm.has(sender, "npcbook.command.filter.getbook"))
					commands.add("getbook");
			}
			StringUtil.copyPartialMatches(args[1], commands, completions);
		}
		Collections.sort(completions);
		return completions;
	}

	private void sendFilterHelp(CommandSender sender) {
		sender.sendMessage("§6===========================");
		sender.sendMessage("");
		sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.filter.set", LangDefaults.help_filter_set));
		sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.filter.remove", LangDefaults.help_filter_remove));
		sender.sendMessage(
				this.plugin.getMessageNoHeader("lang.help.filter.getbook", LangDefaults.help_filter_getbook));
		sender.sendMessage("");
		sender.sendMessage("§6===========================");
	}

	private boolean hasBookInHand(Player player) {
		ItemStack item = player.getInventory().getItemInMainHand();
		if (item == null)
			return false;
		if (!item.getType().equals(Material.WRITTEN_BOOK))
			return false;
		return true;
	}

	private ItemStack getBookFromHand(Player player) {
		return player.getInventory().getItemInMainHand().clone();
	}

	private void openBook(Player player, ItemStack book) {
		BookMeta meta = (BookMeta) book.getItemMeta();
		ItemStack item = new ItemStack(Material.BOOK_AND_QUILL);
		item.setItemMeta(meta);
		player.getInventory().setItemInMainHand(item);
	}

	private void sendAbout(CommandSender sender) {
		sender.sendMessage("§8§m+----------------------+");
		sender.sendMessage("");
		sender.sendMessage("§eCitizensBooks");
		sender.sendMessage("§6Version: §c" + this.plugin.getDescription().getVersion());
		sender.sendMessage("§6Auhtor: §cnicuch");
		sender.sendMessage("");
		sender.sendMessage("§8§m+----------------------+");
	}

	private void sendHelp(CommandSender sender) {
		sender.sendMessage("§8§m+----------------------+");
		sender.sendMessage("");
		sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.about", LangDefaults.help_about));
		sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.set", LangDefaults.help_set));
		sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.remove", LangDefaults.help_remove));
		sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.reload", LangDefaults.help_reload));
		sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.getbook", LangDefaults.help_getbook));
		sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.openbook", LangDefaults.help_openbook));
		sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.setcmd", LangDefaults.help_setcmd));
		sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.remcmd", LangDefaults.help_remcmd));
		sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.filter.set", LangDefaults.help_filter_set));
		sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.filter.remove", LangDefaults.help_filter_remove));
		sender.sendMessage(
				this.plugin.getMessageNoHeader("lang.help.filter.getbook", LangDefaults.help_filter_getbook));
		sender.sendMessage("");
		sender.sendMessage("§8§m+----------------------+");
	}

}