package org.turbojax;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.command.CommandSender;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
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

                printAlias(sender, args[1]);
                return;
            case "list":
                // Permissions check "easyalias.list"
                if (!sender.hasPermission("easyalias.list")) {
                    sender.sendMessage(toComponent("<red>You don't have permission to do that."));
                    return;
                }

                // Sending the aliases and their respective commands
                sender.sendMessage(toComponent("<#FFA500>EasyAlias Aliases:"));
                config.getAliases().forEach(a -> printAlias(sender, a));
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
        if (commandSourceStack.getSender().hasPermission("easyalias.get")) suggestions.add("get");
        if (commandSourceStack.getSender().hasPermission("easyalias.help")) suggestions.add("help");
        if (commandSourceStack.getSender().hasPermission("easyalias.list")) suggestions.add("list");
        if (commandSourceStack.getSender().hasPermission("easyalias.reload")) suggestions.add("reload");

        if (args.length == 0) return suggestions;
        
        return suggestions.stream().filter(s -> s.startsWith(args[0])).toList();
    }

    public void sendHelp(CommandSender sender) {
        // Building the body of the help message based on the permissions the sender has.
        Component base = Component.empty();
        if (sender.hasPermission("easyalias.help")) base = base.append(toComponent("<#FFA500>/easyalias help<white>: Prints this help message.\n"));
        if (sender.hasPermission("easyalias.list")) base = base.append(toComponent("<#FFA500>/easyalias list<white>: Lists all of the aliases available to the player and the command they run.\n"));
        if (sender.hasPermission("easyalias.reload")) base = base.append(toComponent("<#FFA500>/easyalias reload<white>: Reloads the config.\n"));
        if (sender.hasPermission("easyalias.get")) base = base.append(toComponent("<#FFA500>/easyalias get <alias><white>: Gets the command that the alias executes.\n"));

        // If the user does not have the ability to see any command, send an error.
        if (base.equals(Component.empty())) {
            sender.sendMessage(toComponent("<red>You don't have permission to do that."));
            return;
        }

        // Sending the help message to the sender with only visible components sent.
        sender.sendMessage(toComponent("-----------< <#FFA500>EasyAlias Commands <white>>-----------\n").append(base));
    }

    public void printAlias(Audience audience, String alias) {
        // Handling when the alias isn't bound to a command.
        if (!config.getAliases().contains(alias)) {
            audience.sendMessage(toComponent("<gold>/" + alias + " <red>is not bound to a command."));
            return;
        }

        // Sending the alias and it's respective command
        audience.sendMessage(toComponent("<gold>/" + alias + "<white>: <green>(" + (config.getRunAsConsole(alias) ? "Console" : "Player") + ")"));
        config.getExecutedCommands(alias).forEach(cmd -> {
            audience.sendMessage(toComponent("<gold>- <yellow>\"" + cmd + "\""));
        });
    }

    public Component toComponent(String miniMessageStr) {
        return miniMessage.deserialize(miniMessageStr);
    }
}
