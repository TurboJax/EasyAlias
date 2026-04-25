# Commands
- `/easyalias help`: Prints the help message
- `/easyalias reload`: Reloads the config and registers/unregisters any commands.
- `/easyalias get <alias>`: Prints part of "/easyalias list" for a specific alias
- `/easyalias list`: Lists all of the commands available to the player and what they do.

# The Config
All commands should be defined under the "commands" section of `config.yml`.  

```yml
commands:
  <alias>:
    permission: <required permission>
    run_as_console: <true|false>
    executes:
      - <list of commands to run>
```

- `permission` is the permission required to run the alias.  If not specified, any player can use the command.  
- `run_as_console` is a boolean that determines if the command will run as the player or as the console.  
- `executes` is the list of commands that the alias will run.  The commands should not have the forward slash ("/") at the beginning of each line.  It can also be a string if only one command needs to be executed