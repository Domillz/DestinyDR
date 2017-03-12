package net.dungeonrealms.game.command;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Kieran Quigley (Proxying) on 08-Jul-16.
 */
public class CommandPlayed extends BaseCommand {

    public CommandPlayed(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            if (args.length == 0) {
                Player player = (Player) sender;
                int minutesPlayed = (int) DatabaseAPI.getInstance().getData(EnumData.TIME_PLAYED, player.getUniqueId());
                player.sendMessage(ChatColor.YELLOW.toString() + ChatColor.UNDERLINE + ChatColor.BOLD + "Time Played:" + ChatColor.YELLOW.toString() + " " + GameAPI.formatTime(minutesPlayed));
                return true;
            }
        }
        return false;
    }
}
