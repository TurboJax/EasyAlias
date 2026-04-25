package org.turbojax;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

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
        // Creating the file if it doesn't exist.
        if (!file.exists()) {
            plugin.saveResource(file.getName(), false);
        }

        // If the file still doesn't exist, fail loading
        if (!file.exists()) {
            plugin.getLogger().severe("Could not create config.yml.");

            return false;
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
     * Creating the config file. If it doesn't exist, it loads the default config. If the file does
     * exist, it will only replace it if the parameter is true.
     * 
     * @param replace Whether or not to replace the config file with the default configs.
     * @return Whether or not the file was created successfully.
     */
    public boolean createFile(boolean replace) {
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

    /** Gets all the aliases defined by the config. */
    @NotNull
    public Set<String> getAliases() {
        if (!config.isConfigurationSection("commands")) return Set.of();

        return config.getConfigurationSection("commands").getKeys(false);
    }

    /**
     * Gets the series of commands an alias executes.
     * 
     * @param alias The alias to get the commands for.
     */
    @NotNull
    public List<String> getExecutedCommands(String alias) {
        String key = "commands." + alias + ".executes";
        if (config.isString(key)) {
            return List.of(config.getString(key));
        }

        return config.getStringList(key);
    }

    /**
     * Gets whether the alias should run as the console or as the player/command block running the alias.
     * Defaults to false.
     * 
     * @param alias The alias to get the executor of.
     */
    public boolean getRunAsConsole(String alias) {
        return config.getBoolean("commands." + alias + ".run_as_console", false);
    }

    /**
     * Gets the permission required to use the alias.
     * If none is specified, it returns an empty string.
     * 
     * @param alias The alias to get the permission of.
     */
    @Nullable
    public String getPermission(String alias) {
        return config.getString("commands." + alias + ".permission", "");
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
     * This function applies config changes to old configs to allow for old configs to be automatically updated over time.
     * 
     * Empty for now because no config changes have been made since 1.0.0 release
     */
    public void applyUpdates() {}
}