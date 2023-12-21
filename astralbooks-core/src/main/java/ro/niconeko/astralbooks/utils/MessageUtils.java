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

package ro.niconeko.astralbooks.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ro.niconeko.astralbooks.managers.BossBarManager;
import ro.niconeko.astralbooks.managers.HooksManager;
import ro.niconeko.astralbooks.values.Messages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtils {
    private static final Pattern HEX_PATTERN = Pattern.compile("#([A-Fa-f0-9]{6})");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("\\[G:#([A-Fa-f0-9]{6}):#([A-Fa-f0-9]{6})](.*?)\\[/G]");

    private static final Pattern TITLE_PATTERN = Pattern.compile("^<T(?::\\((.*?)\\))?:\\[(.*?)](?::\\[(.*?)])?>$");
    private static final Pattern BOSSBAR_PATTERN = Pattern.compile("^<B(?::\\((.*?)\\))?>");
    private static final Pattern ACTIONBAR_PATTERN = Pattern.compile("^<A>");

    public static void sendMessage(CommandSender sender, String message) {
        Matcher matcher;
        String msg;
        if ((matcher = ACTIONBAR_PATTERN.matcher(message)).find()) {
            if (sender instanceof Player player) {
                msg = message.substring(matcher.end());
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(parseMessage(sender, msg)));
            }
        } else if ((matcher = BOSSBAR_PATTERN.matcher(message)).find()) {
            if (sender instanceof Player player) {
                msg = message.substring(matcher.end());
                String config = matcher.group(1);
                BossBarConfig bossBarConfig = new BossBarConfig(config);
                BossBarManager.sendBossBar(player, bossBarConfig, (p) -> parseMessage(p, msg));
            }
        } else if ((matcher = TITLE_PATTERN.matcher(message)).find()) {
            if (sender instanceof Player player) {
                String config = matcher.group(1);
                String title = matcher.group(2);
                String subtitle = matcher.group(3);
                TitleConfig titleConfig = new TitleConfig(config);
                player.sendTitle(parseMessage(sender, title), parseMessage(sender, subtitle), titleConfig.getIn(), titleConfig.getStay(), titleConfig.getOut());
            }
        } else
            sender.spigot().sendMessage(TextUtils.components(parseMessage(sender, message)));
    }

    public static String parseMessage(CommandSender sender, String message) {
        message = message.replaceAll("\\[header]", Messages.HEADER.get());
        if (HooksManager.PAPI.isEnabled()) {
            if (sender instanceof Player player) {
                return HooksManager.PAPI.parse(hex(gradient(legacy(message)))).apply(player);
            }
            return HooksManager.PAPI.parseSup(hex(gradient(legacy(message)))).get();
        }
        return hex(gradient(legacy(message)));
    }

    public static String hex(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder buffer = new StringBuilder(message.length() + 4 * 8);
        while (matcher.find())
            matcher.appendReplacement(buffer, new Color(matcher.group(1)).minecraftHex());
        return matcher.appendTail(buffer).toString();
    }

    public static String legacy(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String gradient(String message) {
        Matcher matcher = GRADIENT_PATTERN.matcher(message);
        StringBuilder buffer = new StringBuilder(message.length() + 4 * 8);
        while (matcher.find())
            matcher.appendReplacement(buffer, toGradient(
                    new Color(matcher.group(1)),
                    new Color(matcher.group(2)),
                    matcher.group(3)));
        return matcher.appendTail(buffer).toString();
    }

    private static String toGradient(Color start, Color end, String content) {
        content = ChatColor.stripColor(content);
        StringBuilder message = new StringBuilder(content.length() + (content.length() * 7));
        int length = content.length();
        for (int index = 0; index < length; index++) {
            int red = (int) (start.red() + (float) (end.red() - start.red()) / (length - 1) * index);
            int green = (int) (start.green() + (float) (end.green() - start.green()) / (length - 1) * index);
            int blue = (int) (start.blue() + (float) (end.blue() - start.blue()) / (length - 1) * index);
            message.append(new Color(red, green, blue).minecraftHex())
                    .append(content.charAt(index));
        }
        return message.toString();
    }
}
