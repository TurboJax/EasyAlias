package org.turbojax;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.command.CommandSender;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class MainCommand implements BasicCommand {
    private final EasyAlias plugin;
    private final MainConfig config;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MainCommand(EasyAlias plugin, MainConfig config) {
        this.plugin = plugin;
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
                // Permissions check "easyalias.reload"
                if (!sender.hasPermission("easyalias.reload")) {
                    sender.sendMessage(toComponent("<red>You don't have permission to do that."));
                    return;
                }

                // Reloading the config
                if (config.load()) {
                    // Reloading the commands
                    plugin.registerCommands();

                    sender.sendMessage(toComponent("<green>Commands reloaded!"));
                } else {
                    sender.sendMessage(toComponent("<red>Failed to load the config."));
                }

                return;
            case "set":
                // Permissions check "easyalias.set"
                if (!sender.hasPermission("easyalias.set")) {
                    sender.sendMessage(toComponent("<red>You don't have permission to do that."));
                    return;
                }

                // Getting the alias parameter
                if (args.length < 2) {
                    sender.sendMessage(toComponent("<red>Please specify an alias and command to add"));
                    return;
                }

                String alias = args[1];

                // Getting the command parameter
                if (args.length < 3) {
                    sender.sendMessage(toComponent("<red>Please specify the command to assign to /" + alias));
                }

                StringBuilder builder = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    builder.append(args[i]);
                    builder.append(" ");
                }

                String command = builder.substring(0, builder.length() - 1);

                // Setting the alias to the command
                config.setCommand(alias, command);
                sender.sendMessage(toComponent("Bound <gold>/" + alias + " <white>to <gold>/" + command));
                return;
            case "get":
                // Permissions check "easyalias.get"
                if (!sender.hasPermission("easyalias.get")) {
                    sender.sendMessage(toComponent("<red>You don't have permission to do that."));
                    return;
                }

                // Getting the alias parameter
                if (args.length < 2) {
                    sender.sendMessage(toComponent("<red>Please specify an alias and command to add"));
                    return;
                }

                alias = args[1];

                command = config.getCommand(alias);

                // Handling when the alias isn't bound to a command.
                if (command == null) {
                    sender.sendMessage(toComponent("<gold>/" + alias + " <red>is not bound to a command."));
                    return;
                }

                // Sending the alias and it's respective command
                sender.sendMessage(toComponent("<gold>/" + alias + " <white>executes <gold>\"/" + command + "\""));
                return;
            case "list":
                // Permissions check "easyalias.list"
                if (!sender.hasPermission("easyalias.list")) {
                    sender.sendMessage(toComponent("<red>You don't have permission to do that."));
                    return;
                }

                // Sending the aliases and their respective commands
                sender.sendMessage(toComponent("<#FFA500>EasyAlias Registered Aliases:"));
                config.getCommands().forEach((a, c) -> {
                    sender.sendMessage(toComponent("<gold>/" + a + ": <gold>/" + c));
                });
                return;
            default:
                // Error: invalid command
                sendHelp(sender);
        }
    }
    
    @Override
    public Collection<String> suggest(CommandSourceStack commandSourceStack, String[] args) {
        if (args.length != 1) return List.of();
        
        List<String> suggestions = new ArrayList<>();
        if (commandSourceStack.getSender().hasPermission("easyalias.get")) suggestions.add("get");
        if (commandSourceStack.getSender().hasPermission("easyalias.help")) suggestions.add("help");
        if (commandSourceStack.getSender().hasPermission("easyalias.list")) suggestions.add("list");
        if (commandSourceStack.getSender().hasPermission("easyalias.reload")) suggestions.add("reload");
        if (commandSourceStack.getSender().hasPermission("easyalias.set")) suggestions.add("set");
        return List.of();
    }

    public void sendHelp(CommandSender sender) {
        // Building the body of the help message based on the permissions the sender has.
        Component base = Component.empty();
        if (sender.hasPermission("easyalias.get")) base.append(toComponent("<#FFA500>/easyalias get <alias><white>: Gets the command that the alias executes.\n"));
        if (sender.hasPermission("easyalias.help")) base.append(toComponent("<#FFA500>/easyalias help<white>: Prints this help message.\n"));
        if (sender.hasPermission("easyalias.list")) base.append(toComponent("<#FFA500>/easyalias list<white>: Lists all of the aliases available to the player and the command they run.\n"));
        if (sender.hasPermission("easyalias.reload")) base.append(toComponent("<#FFA500>/easyalias reload<white>: Reloads the config.\n"));
        if (sender.hasPermission("easyalias.set")) base.append(toComponent("<#FFA500>/easyalias set <alias> <command><white>: Creates or overwrites an alias to the passed command.  Can contain spaces.\n"));

        // If the user does not have the ability to see any command, send an error.
        if (base.equals(Component.empty())) {
            sender.sendMessage(toComponent("<red>You don't have permission to do that."));
            return;
        }

        // Sending the help message to the sender with only visible components sent.
        sender.sendMessage(toComponent("-----------< <#FFA500>EasyAlias Commands <white>>-----------\n").append(base));
    }

    public Component toComponent(String miniMessageStr) {
        return miniMessage.deserialize(miniMessageStr);
    }
}
