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
         *  |- remove
         *  |   \- <alias>: Removes an alias from the plugin.
         *  |
         *  \- list: Lists all of the commands available to the player and what they do.
         */

        switch(args[0].toLowerCase()) {
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
            case "add":
                // Permissions check "easyalias.add"
                if (!sender.hasPermission("easyalias.add")) {
                    sender.sendMessage(toComponent("<red>You don't have permission to do that."));
                    return;
                }

                // Getting the alias parameter
                if (args.length < 2) {
                    sender.sendMessage(toComponent("<red>Please specify an alias to add."));
                    return;
                }

                String alias = args[1];

                // Getting the run_as_console parameter
                if (args.length < 3) {
                    sender.sendMessage(toComponent("<red>Please specify a value for the run_as_console parameter."));
                }

                boolean runAsConsole = Boolean.parseBoolean(args[2]);

                // Getting the command parameter
                if (args.length < 4) {
                    sender.sendMessage(toComponent("<red>Please write out the command the alias will run."));
                }

                StringBuilder builder = new StringBuilder();
                for (int i = 3; i < args.length; i++) {
                    builder.append(args[i]);
                    builder.append(" ");
                }

                String command = builder.substring(0, builder.length() - 1);

                // Adding the alias to the config
                config.addAlias(alias, command, runAsConsole);

                plugin.registerCommand(alias);
                sender.sendMessage(toComponent("<green>Added <gold>/" + alias + "<green> as a new alias."));
                return;
            case "edit":
                // Permissions check "easyalias.edit"
                if (!sender.hasPermission("easyalias.edit")) {
                    sender.sendMessage(toComponent("<red>You don't have permission to do that."));
                    return;
                }

                // Getting the alias parameter
                if (args.length < 2) {
                    sender.sendMessage(toComponent("<red>Please specify an alias to edit"));
                    return;
                }

                alias = args[1];

                // Getting the option parameter
                if (args.length < 3) {
                    sender.sendMessage(toComponent("<red>Please specify the option to edit for /" + alias));
                }

                String option = args[2];

                // Editing the command config
                if (option.equals("command")) {
                    // Getting the option value
                    builder = new StringBuilder();
                    for (int i = 3; i < args.length; i++) {
                        builder.append(args[i]);
                        builder.append(" ");
                    }

                    command = builder.substring(0, builder.length() - 1);

                    // Editing the config
                    config.setCommand(alias, command);
                    sender.sendMessage(toComponent("Edited the command for <gold>/" + alias + "<white> to <yellow>" + command));
                    return;
                }

                // Editing the run_as_console config
                if (option.equals("run_as_console")) {
                    // Getting the option value
                    runAsConsole = Boolean.parseBoolean(args[3]);

                    // Editing the config
                    config.setRunAsConsole(alias, runAsConsole);
                    sender.sendMessage(toComponent("Set run_as_console for <gold>/" + alias + "<white> to " + runAsConsole));
                    return;
                }

                sender.sendMessage(toComponent("<red>\"" + option + "\" is not a configurable option for an alias.  Please use \"command\" or \"run_as_console\"."));
                return;
            case "get":
                // Permissions check "easyalias.get"
                if (!sender.hasPermission("easyalias.get")) {
                    sender.sendMessage(toComponent("<red>You don't have permission to do that."));
                    return;
                }

                // Getting the alias parameter
                if (args.length < 2) {
                    sender.sendMessage(toComponent("<red>Please specify an alias to read"));
                    return;
                }

                alias = args[1];

                // Handling when the alias isn't bound to a command.
                if (!config.getAliases().contains(alias)) {
                    sender.sendMessage(toComponent("<gold>/" + alias + " <red>is not bound to a command."));
                    return;
                }

                // Sending the alias and it's respective command
                sender.sendMessage(toComponent("<gold>/" + alias + "<white>: <yellow>\"/" + config.getCommand(alias) + "\" <green>(" + (config.getRunAsConsole(alias) ? "Console" : "Player") + ")"));
                return;
            case "remove":
                // Permissions check "easyalias.remove"
                if (!sender.hasPermission("easyalias.remove")) {
                    sender.sendMessage(toComponent("<red>You don't have permission to do that."));
                    return;
                }

                // Getting the alias parameter
                if (args.length < 2) {
                    sender.sendMessage(toComponent("<red>Please specify an alias to remove"));
                    return;
                }

                alias = args[1];

                // Removing the command
                config.removeAlias(alias);
                plugin.unregisterCommand(alias);

                sender.sendMessage(toComponent("Removed <gold>/" + alias));
                return;
            case "list":
                // Permissions check "easyalias.list"
                if (!sender.hasPermission("easyalias.list")) {
                    sender.sendMessage(toComponent("<red>You don't have permission to do that."));
                    return;
                }

                // Sending the aliases and their respective commands
                sender.sendMessage(toComponent("<#FFA500>EasyAlias Aliases:"));
                config.getAliases().forEach(a -> {
                    sender.sendMessage(toComponent("<gold>/" + a + "<white>: <yellow>\"/" + config.getCommand(a) + "\" <green>(" + (config.getRunAsConsole(a) ? "Console" : "Player") + ")"));
                });
                return;
            default:
                // Error: invalid command
                sendHelp(sender);
        }
    }
    
    @Override
    public Collection<String> suggest(CommandSourceStack commandSourceStack, String[] args) {
        if (args.length > 1) return List.of();
        
        List<String> suggestions = new ArrayList<>();
        if (commandSourceStack.getSender().hasPermission("easyalias.add")) suggestions.add("add");
        if (commandSourceStack.getSender().hasPermission("easyalias.get")) suggestions.add("get");
        if (commandSourceStack.getSender().hasPermission("easyalias.help")) suggestions.add("help");
        if (commandSourceStack.getSender().hasPermission("easyalias.list")) suggestions.add("list");
        if (commandSourceStack.getSender().hasPermission("easyalias.reload")) suggestions.add("reload");
        if (commandSourceStack.getSender().hasPermission("easyalias.remove")) suggestions.add("remove");
        if (commandSourceStack.getSender().hasPermission("easyalias.edit")) suggestions.add("edit");

        if (args.length == 0) return suggestions;
        
        return suggestions.stream().filter(s -> s.startsWith(args[0])).toList();
    }

    public void sendHelp(CommandSender sender) {
        // Building the body of the help message based on the permissions the sender has.
        Component base = Component.empty();
        if (sender.hasPermission("easyalias.help")) base = base.append(toComponent("<#FFA500>/easyalias help<white>: Prints this help message.\n"));
        if (sender.hasPermission("easyalias.list")) base = base.append(toComponent("<#FFA500>/easyalias list<white>: Lists all of the aliases available to the player and the command they run.\n"));
        if (sender.hasPermission("easyalias.reload")) base = base.append(toComponent("<#FFA500>/easyalias reload<white>: Reloads the config.\n"));
        if (sender.hasPermission("easyalias.add")) base = base.append(toComponent("<#FFA500>/easyalias add <alias> <run_as_console> <command><white>: Creates a new alias with the specified parameters.\n"));
        if (sender.hasPermission("easyalias.get")) base = base.append(toComponent("<#FFA500>/easyalias get <alias><white>: Gets the command that the alias executes.\n"));
        if (sender.hasPermission("easyalias.edit")) base = base.append(toComponent("<#FFA500>/easyalias edit <alias> <option> <option_value><white>: Edits an alias.  Valid options are \"command\" to edit the command that runs or \"run_as_console\" to set whether or not the command should run as the console.\n"));
        if (sender.hasPermission("easyalias.remove")) base = base.append(toComponent("<#FFA500>/easyalias remove <alias><white>: Removes an alias.\n"));

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
