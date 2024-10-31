package de.nixgutyetihafen.pommesmaker.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DebugTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        // Check if the command name is "setdebug" and args length is 1 (the first argument)
        if (command.getName().equalsIgnoreCase("setdebug")) {
            if (args.length == 1) {
                // Return the options for the first argument
                return Arrays.asList("on", "off");
            }
        }
        return new ArrayList<>(); // Return empty list if no completions are found
    }
}
