package ro.niconeko.astralbooks.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandListenerWrapper;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.utils.Permission;
import ro.niconeko.astralbooks.utils.Replaceable;

import java.util.ArrayList;
import java.util.List;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;

public abstract class AstralCommand extends LiteralArgumentBuilder<CommandListenerWrapper> {
    protected final AstralBooksPlugin plugin;

    protected AstralCommand(AstralBooksPlugin plugin, String literal, Permission permission) {
        super(literal);
        super.requires(sender -> permission.has(sender.getBukkitSender()));
        this.plugin = plugin;
    }

    protected RequiredArgumentBuilder<CommandListenerWrapper, Integer> integerArgument(String name) {
        return RequiredArgumentBuilder.argument(name, integer());
    }

    protected RequiredArgumentBuilder<CommandListenerWrapper, String> greedyArgument(String name) {
        return RequiredArgumentBuilder.argument(name, greedyString());
    }

    protected RequiredArgumentBuilder<CommandListenerWrapper, String> stringArgument(String name) {
        return RequiredArgumentBuilder.argument(name, string());
    }

    protected CommandSender getSender(CommandContext<CommandListenerWrapper> context) {
        return context.getSource().getBukkitSender();
    }

    protected SuggestionProvider<CommandListenerWrapper> integerRange(int min, int max) {
        return (context, builder) -> {
            List<String> rangeList = new ArrayList<>();
            for (int i = min; i <= max; i++)
                rangeList.add(i + "");
            for (String str : StringUtil.copyPartialMatches(builder.getInput(), rangeList, new ArrayList<>()))
                builder.suggest(str);
            return builder.buildFuture();
        };
    }

    protected SuggestionProvider<CommandListenerWrapper> stringRange(List<String> list) {
        return (context, builder) -> {
            for (String str : StringUtil.copyPartialMatches(builder.getInput(), list, new ArrayList<>()))
                builder.suggest(str);
            return builder.buildFuture();
        };
    }

    protected Replaceable replaceable(CommandContext<CommandListenerWrapper> context, String str, String value) {
        return new Replaceable(context, str, value);
    }
}
