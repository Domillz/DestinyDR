package net.dungeonrealms.game.command.moderation;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.player.inventory.menus.guis.support.CharacterSelectionGUI;
import net.dungeonrealms.game.world.realms.Realm;
import net.dungeonrealms.game.world.realms.Realms;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * Realmsee. Loads a user's realm from FTP.
 *
 * @author Kneesnap
 */
public class CommandRealmsee extends BaseCommand {

    public CommandRealmsee() {
        super("realmsee", "/<command> <player>", "Loads a player's realm.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player) || !Rank.isTrialGM((Player) sender))
            return true;

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Syntax: /" + label + " <player>");
            return true;
        }

        Block block = ((Player) player).getTargetBlock((Set<Material>) null, 6);

        if (block.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "Please face the block you'd like to place this realm portal on.");
            return true;
        }
        SQLDatabaseAPI.getInstance().getUUIDFromName(args[0], false, (uuid) -> {
            if (uuid == null) {
                player.sendMessage(ChatColor.RED + "Player not found in database.");
                return;
            }

            Integer accountID = SQLDatabaseAPI.getInstance().getAccountIdFromUUID(uuid);
                if(accountID == null) {
                    sender.sendMessage(org.bukkit.ChatColor.RED + "This player has never logged in with Dungeon Realms");
                    return;
                }


                new CharacterSelectionGUI(player,accountID, (charID) -> {
                PlayerWrapper.getPlayerWrapper(uuid, charID,false, false, (wrapper) -> {
                    if (wrapper.isPlaying()) {
                        player.sendMessage(ChatColor.RED + "This user is online on another shard. Changes to their realm may not save.");
                    }

                    Realm realm = Realms.getInstance().getOrCreateRealm(uuid, wrapper.getCharacterID());
                    if (realm != null && realm.isOpen()) {
                        sender.sendMessage(ChatColor.RED + "This user is on this shard and their realm is open already.");
                        return;
                    }
                    if (realm != null)
                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> realm.openPortal(player, block.getLocation()));
                });
            }).open(player,null);




        });
        return true;
    }
}
