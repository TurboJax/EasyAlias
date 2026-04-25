# Defining Commands

## With the Config
All commands should be defined under the "commands" section of `config.yml`.  

```yml
commands:
  alias:
    command: /command
    run_as_console: false
```

- `command` is the command that the alias will run.  It may contain the string "\<player>" which will be replaced with the username of the sender when the alias is run.  If "\<player>" is present, the command can only be run by players.  
- `run_as_console` is an optional parameter that defaults to false.  It tells the plugin whether or not the command should be run as the console.  Otherwise, the command runs as the player or block running the alias.  

## With the Command


# Commands
- `/easyalias help`: Prints the help message
- `/easyalias reload`: Reloads the config and registers/unregisters any commands.
- `/easyalias add <alias> <run_as_console> <command>`: Adds a new alias to the list.
- `/easyalias get <alias>`: Prints part of "/easyalias list" for a specific alias
- `/easyalias edit <alias> command <command>`: Sets the command that the alias executes.
- `/easyalias edit <alias> run_as_console <run_as_console>`: Sets whether or not the command should run as console.
- `/easyalias remove <alias>`: Removes an alias from the plugin.
- `/easyalias list`: Lists all of the commands available to the player and what they do.



<gold>/alias<white>: <yellow>"/command" <green>(Console|Player)