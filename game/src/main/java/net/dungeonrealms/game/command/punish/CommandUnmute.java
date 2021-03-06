package net.dungeonrealms.game.command.punish;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.database.punishment.PunishAPI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.UUID;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/2/2016
 */

public class CommandUnmute extends BaseCommand {

    public CommandUnmute(String command, String usage, String description, String... aliases) {
        super(command, usage, description, Arrays.asList(aliases));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!Rank.isPMOD(player)) return false;
        }

        if (args.length == 0) {
            sender.sendMessage(usage);
            return true;
        }

        String p_name = args[0];
        Player p = Bukkit.getPlayer(p_name);

        if (p == null) {
            sender.sendMessage(ChatColor.RED + p_name + " is not online.");
            return true;
        }

        UUID p_uuid = p.getUniqueId();

        if (!PunishAPI.isMuted(p_uuid)) {
            sender.sendMessage(ChatColor.RED + p_name + " is not muted.");
            return true;
        }

        PunishAPI.unmute(p_uuid);
        sender.sendMessage(ChatColor.RED.toString() + "You have unmuted " + ChatColor.BOLD + p_name + ChatColor.RED + ".");
        //GameAPI.sendNetworkMessage("StaffMessage", ChatColor.RED + ChatColor.BOLD.toString() + sender.getName() + ChatColor.RED + " has unmuted " + ChatColor.BOLD + p_name + ChatColor.RED + ".");
        GameAPI.sendStaffMessage(ChatColor.RED + ChatColor.BOLD.toString() + sender.getName() + ChatColor.RED + " has unmuted " + ChatColor.BOLD + p_name + ChatColor.RED + ".");
        return false;
    }
}
