package org.turbojax;

import java.util.Collection;
import org.bukkit.command.CommandSender;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class MainCommand implements BasicCommand {
    private final MainConfig config;

    public MainCommand(MainConfig config) {
        this.config = config;
    }

    @Override
    public void execute(CommandSourceStack commandSourceStack, String[] args) {
        CommandSender sender = commandSourceStack.getSender();

        if (args.length == 0) {
            sendHelp(sender);
            return;
        }
        /*
         * /easyalias
         *  |- help: Prints this help message
         *  |- reload: Reloads the config and registers/unregisters any commands.
         *  |- set
         *  |   \- <alias>
         *  |       \- <command>: Sets the <alias> to the specified <command>.
         *  |- get
         *  |   \- <alias>: Gets the command that the <alias> runs.
         *  |
         *  \- list: Lists all of the commands available to the player and what they do.
         */

        switch(args[1].toLowerCase()) {
            case "help":
                sendHelp(sender);
                return;
            case "reload":
                // TODO: Permissions check "easyalias.reload"
                // Reloading the config
                if (config.load()) {
                    sender.sendMessage(Component.text("Config reloaded!", NamedTextColor.GREEN));
                } else {
                    sender.sendMessage(Component.text("Config failed to reload.", NamedTextColor.RED));
                }
                return;
            case "set":
                // TODO: Permissions check "easyalias.set"
                // Getting the alias parameter
                // Getting the command parameter
                // Setting the alias to the command
                return;
            case "get":
                // TODO: Permissions check "easyalias.get"
                // Getting the alias parameter
                // Sending the alias and it's respective command
                return;
            case "list":
                // TODO: Permissions check "easyalias.list"
                // Sending the aliases and their respective commands
                return;
            default:
                // Error: invalid command
        }
    }
    

    @Override
    public Collection<String> suggest(CommandSourceStack commandSourceStack, String[] args) {
        return BasicCommand.super.suggest(commandSourceStack, args);
    }

    public void sendHelp(CommandSender sender) {

    }
}
