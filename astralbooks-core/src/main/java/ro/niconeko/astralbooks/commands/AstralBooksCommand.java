package ro.niconeko.astralbooks.commands;

import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.commands.subs.HelpCommand;
import ro.niconeko.astralbooks.commands.subs.ReloadCommand;
import ro.niconeko.astralbooks.values.Messages;
import ro.niconeko.astralbooks.values.Permissions;

public class AstralBooksCommand extends AstralCommand {
    public AstralBooksCommand(AstralBooksPlugin plugin) {
        super(plugin, "abooks", Permissions.COMMAND);
        super.then(new ReloadCommand(plugin))
                .then(new HelpCommand(plugin))
                // etc...
                .then(super.greedyArgument("unknown").executes(context -> Messages.COMMAND_MAIN_UNKNOWN.send(getSender(context), replaceable(context, "%command%", "unknown"))));
        super.executes(context -> Messages.COMMAND_MAIN_HELP.send(getSender(context)));
    }
}
