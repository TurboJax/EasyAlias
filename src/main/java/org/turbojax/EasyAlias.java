package org.turbojax;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class EasyAlias extends JavaPlugin implements Listener {
    private final HttpRequest request;
    private final HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
    private final String GITLAB_TOKEN = "glpat-utSA04EDb8SnhthMS5nakW86MQp1OjcH.01.0w17ewqmt";
    private final MainConfig config = new MainConfig(this);

    private final Map<String,Command> enabledCommands = new HashMap<>();

    public EasyAlias() {
        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI("https://gitlab.turbojax.org/api/v4/projects/3/releases")).GET()
                    .header("PRIVATE-TOKEN", GITLAB_TOKEN)
                    .build();
        } catch (URISyntaxException err) {
            throw new RuntimeException(err);
        }
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        config.load();

        // Registering the main command
        registerCommand("easyalias", new MainCommand(this, config));

        // Registering aliases
        registerCommands();
    }

    /** Registers all the commands in the config. */
    public void registerCommands() {
        config.getAliases().forEach(this::registerCommand);
    }

    /**
     * Binds an alias to a command.
     * 
     * @param alias The alias to create.
     * @param command The command that will run when people run the alias.
     * 
     * @return true if the command was registered successfully, false otherwise
     */
    public boolean registerCommand(String alias) {
        // Creating the Command to register
        Command bukkitCommand = new Command(alias) {
            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String @NotNull [] args) {
                List<String> commands = config.getExecutedCommands(alias);
                
                for (String command : commands) {
                    if (command.contains("<player>")) {
                        if (!(sender instanceof Player)) {
                            sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
                            return true;
                        }

                        command = command.replace("<player>", sender.getName());
                    }

                    try {
                        Bukkit.dispatchCommand(config.getRunAsConsole(alias) ? Bukkit.getConsoleSender() : sender, command);
                    } catch (CommandException e) {
                        String msg = e.getCause().getLocalizedMessage();
                        StringBuilder formatted = new StringBuilder();
                        
                        Matcher matcher = Pattern.compile("at position ([0-9]*):").matcher(msg);
                        if (matcher.find()) {
                            int cmdIndex = Integer.valueOf(matcher.group(1));

                            formatted.append("<red>");
                            formatted.append(msg.substring(0, matcher.start()));
                            formatted.append("\n");
                            formatted.append("<gray>");

                            int hereIndex = msg.length() - 9;

                            formatted.append(msg.substring(matcher.end(), hereIndex));
                            formatted.append("<red><u>");
                            formatted.append(command.substring(cmdIndex));
                            formatted.append("</u><i>");
                            formatted.append(msg.substring(hereIndex));
                        }

                        sender.sendMessage(MiniMessage.miniMessage().deserialize(formatted.toString()));
                        return true;
                    }
                }

                return true;
            }
        };

        // Registering the command and printing an error message if it fails.
        if (!getServer().getCommandMap().register("easyalias", bukkitCommand)) {
            getLogger().severe("Could not register /" + alias);
            return false;
        }

        // Recording the command in the enabled map
        enabledCommands.put(alias, bukkitCommand);

        // Updating every player's command list
        Bukkit.getOnlinePlayers().forEach(Player::updateCommands);

        return true;
    }

    /** Unregisters all commands from the config. */
    public void unregisterCommands() {
        config.getAliases().forEach(this::unregisterCommand);
    }

    /**
     * Unregisters an alias from the server.
     * 
     * @param alias The alias to unregister.
     * 
     * @return true if the unregistration was successful (the alias exists and was registered under {@link Bukkit#getCommandMap()}), false otherwise
     */
    public boolean unregisterCommand(String alias) {
        // Only allowing commands made by this plugin to be passed through this function
        if (!enabledCommands.containsKey(alias)) {
            getLogger().warning("/" + alias + " is not a registered alias.  Cannot remove it from the command map.");
        }

        // Removing the command from the plugin's map
        enabledCommands.remove(alias);

        // Removing the command from the server command map
        Command command = Bukkit.getCommandMap().getKnownCommands().remove(alias);

        // Unregistering the command from the CommandMap
        if (!command.unregister(Bukkit.getCommandMap())) {
            getLogger().severe("Failed to unregister /" + alias + ".  The wrong CommandMap was passed to the command.");
        }

        // Updating every player's command list
        Bukkit.getOnlinePlayers().forEach(Player::updateCommands);

        return true;
    }

    @Override
    public void onDisable() {
        // Unregistering all commands
        unregisterCommands();
    }

    public boolean hasUpdate() {
        return getLatestVersion().compareTo(getPluginVersion()) > 0;
    }

    public String getPluginVersion() {
        return getPluginMeta().getVersion();
    }

    public String getLatestVersion() {
        try {
            // Sending the request
            HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 200) {
                JsonArray jsonResp = new Gson().fromJson(resp.body(), JsonArray.class);

                return jsonResp.get(0).getAsJsonObject().get("tag_name").getAsString();
            }

            // Parsing the version from the redirected url.
            return "";
        } catch (IOException | InterruptedException e) {
            // Log smth here
            throw new RuntimeException(e);
        }
    }
}