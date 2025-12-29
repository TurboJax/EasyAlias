package org.turbojax;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class MainConfig {
    public final EasyAlias plugin;
    public final File file;
    public final FileConfiguration config = new YamlConfiguration();

    public MainConfig(EasyAlias plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "config.yml");
    }

    /**
     * Reloads the configuration.
     *
     * @return Whether the configuration was loaded successfully.
     */
    public boolean load() {
        if (plugin == null) {
            Bukkit.getLogger().severe("ClearLag plugin not loaded, cannot load config.");
            return false;
        }

        // Creating the file if it doesn't exist.
        // If the function returns false, the load function fails too.
        if (!createFile(false)) {
            return false;
        }

        // Handling config version mismatch
        if (!plugin.getPluginMeta().getVersion().equals(getVersion())) {
            try {
                // Backing up old configs
                Files.copy(file.toPath(), Paths.get("plugins/ClearLag/config.yml.bak"), StandardCopyOption.REPLACE_EXISTING);

                plugin.getLogger().info("Old configs have been backed up.");

                // Loading configs for this version
                Files.copy(plugin.getResource("config.yml"), Paths.get("plugins/ClearLag/config.yml.new"), StandardCopyOption.REPLACE_EXISTING);

                plugin.getLogger().info("Configs for this version have been loaded.");
            } catch (IOException e) {
                plugin.getLogger().warning("Could not back up the configs.");
            }
        }

        // Loading the config
        try {
            config.load(file);
            plugin.getLogger().info("Successfully loaded config.yml");
            return true;
        } catch (InvalidConfigurationException err) {
            plugin.getLogger().warning("config.yml contains an invalid YAML configuration.  Verify the contents of the file.");
        } catch (IOException err) {
            plugin.getLogger().severe("Could not find config.yml.  Check that it exists.");
        }

        return false;
    }

    /**
     * Writes the config to the file.
     * 
     * @return Whether or not the config was successfully written.
     */
    public boolean save() {
        // Getting a plugin instance to use
        if (plugin == null) {
            Bukkit.getLogger().severe("ClearLag plugin not loaded, cannot save the config.");
            return false;
        }

        // Creating the file if it doesn't exist.
        // If the function returns false, the load function fails too.
        if (!createFile(false)) {
            return false;
        }

        // Saving the config
        try {
            config.save(file);
            plugin.getLogger().info("Successfully saved the config to config.yml");
            return true;
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save to config.yml.  Make sure the user has write permissions.");
        }

        return false;
    }

    /**
     * Creating the config file. If it doesn't exist, it loads the default config. If the file does
     * exist, it will only replace it if the parameter is true.
     * 
     * @param replace Whether or not to replace the config file with the default configs.
     * @return Whether or not the file was created successfully.
     */
    public boolean createFile(boolean replace) {
        // Getting a plugin instance to use
        if (plugin == null) {
            Bukkit.getLogger().severe("ClearLag plugin not loaded, cannot create default config.");
            return false;
        }

        // Creating the file if it doesn't exist.
        if (!file.exists()) {
            plugin.saveResource(file.getName(), replace);
        }

        // Checking if the file still doesn't exist.
        if (!file.exists()) {
            plugin.getLogger().severe("Could not create config.yml.  Check if it already exists.");

            return false;
        }

        return true;
    }

    /**
     * Gets the safe radius of the player.
     * 
     * @return The safe radius of the player.
     */
    public Map<String,String> getCommands() {
        Map<String,String> commands = new HashMap<>();

        Set<String> aliases = config.getConfigurationSection("commands").getKeys(false);
        aliases.forEach(alias -> commands.put(alias, config.getString("commands." + alias)));

        return commands;
    }

    public String getCommand(String alias) {
        return getCommands().get(alias);
    }

    public void setCommand(@NotNull String alias, @NotNull String command) {
        Map<String,String> commands = getCommands();
        if (command == null) {
            commands.remove(alias);
        } else {
            commands.put(alias, command);
        }
        
        setCommands(commands);
    }

    public void removeCommand(@NotNull String alias) {
        Map<String,String> commands = getCommands();
        commands.remove(alias);
        setCommands(commands);
    }

    public void setCommands(Map<String,String> commands) {
        config.set("commands", commands);
        save();
    }

    /**
     * Sets the "safe" radius of the player.
     * 
     * @param radius The new radius in blocks.
     */
    public void setPlayerSafeRadius(int radius) {
        config.set("player-safe-radius", radius);

        save();
    }

    /**
     * Gets the version number of the config.
     * 
     * @return The version number defined by the config.
     */
    public String getVersion() {
        return config.getString("version");
    }

    /**
     * Sets the version number of the config.
     * 
     * @param version The new version number.
     */
    public void setVersion(String version) {
        config.set("version", version);

        save();
    }
}
