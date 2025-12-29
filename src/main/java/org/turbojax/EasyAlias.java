package org.turbojax;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

public final class EasyAlias extends JavaPlugin implements Listener {
    private final HttpRequest request;
    private final HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
    private final String GITLAB_TOKEN = "glpat-utSA04EDb8SnhthMS5nakW86MQp1OjcH.01.0w17ewqmt";

    private final MainConfig config = new MainConfig(this);

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

    public void registerCommands() {
        config.getCommands().forEach((alias, command) -> {
            getServer().getCommandMap().register("easyalias", new Command(alias) {
                @Override
                public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String @NotNull [] args) {
                    Bukkit.dispatchCommand(sender, command);
                    return true;
                }
            });
        });
    }

    public void registerCommand(String alias) {
        getServer().getCommandMap().register("easyalias", new Command(alias) {
            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String @NotNull [] args) {
                Bukkit.dispatchCommand(sender, config.getCommand(alias));
                return true;
            }
        });
    }

    public void registerCommand(String alias, String command) {
        getServer().getCommandMap().register("easyalias", new Command(alias) {
            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String @NotNull [] args) {
                Bukkit.dispatchCommand(sender, command);
                return true;
            }
        });
    }

    public void removeCommand(String alias) {
        Bukkit.getServer().getCommandMap().getKnownCommands().remove(alias);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        config.save();
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