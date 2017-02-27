package net.dungeonrealms.game.affair.party;

import com.google.common.collect.Lists;
import lombok.Data;
import net.dungeonrealms.game.handler.ScoreboardHandler;
import net.dungeonrealms.game.player.json.JSONMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/1/2016
 */

@Data
public class Party {

    private Player owner;

    private List<Player> members;

    private boolean updateScoreboard;

    private Scoreboard partyScoreboard;

    private LootMode lootMode;

    public Party(Player owner, List<Player> members) {
        this.owner = owner;
        this.members = members;
        this.partyScoreboard = createScoreboard();
        this.updateScoreboard = false;
    }

    public Scoreboard createScoreboard() {
        Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
        ScoreboardHandler handler = ScoreboardHandler.getInstance();
        handler.setCurrentPlayerLevels(sb);
        handler.registerHealth(sb);
        return sb;
    }

    public void setLootMode(LootMode lootMode) {
        this.lootMode = lootMode;

        JSONMessage message = new JSONMessage(ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "<P> " +
                ChatColor.LIGHT_PURPLE + "Party Loot Mode has been changed to ");
        message.addHoverText(Lists.newArrayList(lootMode.getLore()), lootMode.getColor() + lootMode.getName());
        message.addText(ChatColor.LIGHT_PURPLE + " by " + getOwner().getName());
        getAllMembers().forEach((pl) -> {
            message.sendToPlayer(pl);
            pl.sendMessage(ChatColor.GRAY + "Hover over the loot mode to view more info.");
            pl.playSound(pl.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 1.4F);
        });
    }

    public Player getOwner() {
        return owner;
    }

    public List<Player> getMembers() {
        return members;
    }


    public List<Player> getAllMembers() {
        List<Player> pls = Lists.newArrayList();
        pls.addAll(getMembers());
        pls.add(getOwner());
        return pls;
    }

}
