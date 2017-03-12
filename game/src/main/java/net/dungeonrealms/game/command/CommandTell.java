package net.dungeonrealms.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * Created by Nick on 12/5/2015.
 */
public class CommandTell extends BaseCommand {
    public CommandTell() {
        super("tell", "/<command> [args]", "Send a private message to a player.");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length >= 1) {
            String targetPlayer = args[0];
            
            Bukkit.getOnlinePlayers().stream().limit(1).filter(player -> player.getName().equalsIgnoreCase(targetPlayer)).forEach(player1 -> {
                StringBuilder message = new StringBuilder();

                for (int i = 1; i < args.length; i++) {
                    message.append(args[i]).append(" ");
                }
                player1.sendMessage(ChatColor.GRAY.toString() + ChatColor.BOLD + "FROM: " + ChatColor.AQUA + sender.getName() + ChatColor.GRAY + ": " + ChatColor.WHITE + message);
                sender.sendMessage(ChatColor.GRAY.toString() + ChatColor.BOLD + "TO: " + ChatColor.AQUA + player1.getName() + ChatColor.GRAY + ": " + ChatColor.WHITE + message);
            });

        } else {
            sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "SYNTAX" + ChatColor.GRAY + ":" + " " + ChatColor.GRAY + "Try: /tell <playerName> <message>");
        }
        return true;
    }
}
