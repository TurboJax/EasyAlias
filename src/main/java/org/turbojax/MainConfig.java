package org.turbojax;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

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
        // Handling config version mismatch if the file exists
        if (file.exists() && !plugin.getPluginMeta().getVersion().equals(getVersion())) {
            plugin.getLogger().info(plugin.getPluginMeta().getVersion());
            plugin.getLogger().info(getVersion());
            try {
                // Backing up old configs
                Files.copy(file.toPath(), Paths.get("plugins/EasyAlias/config.yml.bak"), StandardCopyOption.REPLACE_EXISTING);

                plugin.getLogger().info("Old configs have been backed up.");

                // Loading configs for this version
                Files.copy(plugin.getResource("config.yml"), Paths.get("plugins/EasyAlias/config.yml.new"), StandardCopyOption.REPLACE_EXISTING);

                plugin.getLogger().info("Configs for this version have been loaded.");
            } catch (IOException e) {
                plugin.getLogger().warning("Could not back up the configs.");
            }
        }

        // Creating the file if it doesn't exist.
        // If the file fails to be made, the load function fails too.
        if (!createFile(false)) {
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
     * Writes the config to the file.
     * 
     * @return Whether or not the config was successfully written.
     */
    public boolean save() {
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

    public Set<String> getAliases() {
        return config.getConfigurationSection("commands").getKeys(false);
    }

    public void addAlias(String alias, String command, boolean runAsConsole) {
        setCommands(alias, List.of(command));
        setRunAsConsole(alias, runAsConsole);
    }

    public void removeAlias(String alias) {
        config.set("commands." + alias, null);
        save();
    }

    public List<String> getCommands(String alias) {
        String key = "commands." + alias + ".commands";
        if (config.isString(key)) {
            return List.of(config.getString(key));
        }

        return config.getStringList(key);
    }

    public void setCommands(String alias, List<String> commands) {
        config.set("commands." + alias + ".commands", commands);
        save();
    }

    public boolean getRunAsConsole(String alias) {
        return config.getBoolean("commands." + alias + ".run_as_console", false);
    }

    public void setRunAsConsole(String alias, boolean runAsConsole) {
        config.set("commands." + alias + ".run_as_console", runAsConsole);
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
