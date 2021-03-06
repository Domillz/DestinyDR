package net.dungeonrealms.game.mechanic.dungeons;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.enchantments.EnchantmentAPI;
import net.dungeonrealms.game.item.items.core.ItemArmor;
import net.dungeonrealms.game.item.items.core.ItemWeaponMelee;
import net.dungeonrealms.game.mastery.AttributeList;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.base.DRWitherSkeleton;
import net.dungeonrealms.game.world.entity.type.monster.boss.type.subboss.InfernalGhast;
import net.dungeonrealms.game.world.entity.type.monster.boss.type.subboss.MadBanditPyromancer;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DungeonBoss - Contains utilities that dungeon bosses should implement.
 * Has to be an interface since classes cannot extend multiple classes (On a different hierarchy.)
 * <p>
 * <p>
 * Redone in April 2017.
 *
 * @author Kneesnap
 */
public interface DungeonBoss extends DRMonster {

    BossType getBossType();

    /**
     * Calls when the boss dies.
     */
    default void onBossDeath(Player player) {

    }

    /**
     * Called when the boss is damaged by a player.
     */
    default void onBossAttacked(Player attacker) {

    }

    default void playSound(Sound s, float volume, float pitch) {
        getDungeon().getWorld().playSound(getBukkit().getLocation(), s, volume, pitch);
    }

    //  OVERRIDDEN STUFF  //
    @Override
    default void setupMonster(int tier) {
        setupNMS();
    }

    @Override
    default void onMonsterAttack(Player p) {}

    @Override
    default void onMonsterDeath(Player killer) {
        say(getBossType().getDeathMessage());
        onBossDeath(killer);
        
        if (!getBossType().isFinalBoss())
            return;

        //Remove Nearby Fire
        getNearbyBlocks(getBukkit().getLocation(), 10).stream().filter(bk -> bk.getType() == Material.FIRE).forEach(bk -> bk.setType(Material.AIR));
        ParticleAPI.spawnParticle(Particle.FIREWORKS_SPARK, getBukkit().getLocation().add(0, 2, 0), 200, .2F);
        getDungeon().completeDungeon();
    }

    @Override
    default int getTier() {
        return getDungeon().getType().getTier();
    }

    default Entity spawnMinion(EnumMonster monsterType, String mobName, int tier) {
        return spawnMinion(monsterType, mobName, tier, true);
    }

    default Entity spawnMinion(EnumMonster monsterType, String mobName, int tier, boolean highPower) {
        Location loc = getBukkit().getLocation().clone().add(Utils.randInt(0, 6) - 3, 0, Utils.randInt(0, 6) - 3);
        LivingEntity le = (LivingEntity) EntityAPI.spawnCustomMonster(loc, getBukkit().getLocation().clone(), monsterType, Utils.getRandomFromTier(tier, highPower ? "high" : "low"), tier, null);
        le.setRemoveWhenFarAway(false);
        return le;
    }

    default Dungeon getDungeon() {
        return DungeonManager.getDungeon(getBukkit().getWorld());
    }

    default void setArmor() {

        if (this instanceof MadBanditPyromancer || this instanceof InfernalGhast) {
            //Just give him some standard gear?
            ItemArmor armor = (ItemArmor) new ItemArmor().setRarity(Item.ItemRarity.RARE).setTier(getTier()).setGlowing(true);
            getBukkit().getEquipment().setArmorContents(armor.generateArmorSet());
            getBukkit().getEquipment().setItemInMainHand(new ItemWeaponMelee().setTier(getTier()).setRarity(Item.ItemRarity.getRandomRarity(true)).setGlowing(true).generateItem());
            return;
        }

        // Set armor.
        Map<EquipmentSlot, ItemStack> gear = ItemGenerator.getEliteGear(getBossType().name().toLowerCase());
        
        if (gear.isEmpty()) {
            setGear();
            return;
        }
        
        for (EquipmentSlot e : gear.keySet()) {
            ItemStack i = gear.get(e);
            EnchantmentAPI.addGlow(i);
            GameAPI.setItem(getBukkit(), e, i);
        }
    }

    default void say(String msg) {
        if (msg != null && msg.length() > 0)
            getDungeon().announce(ChatColor.RED + getBossType().getName() + "> " + ChatColor.RESET + msg);
    }

    default void createEntity(int level) {
        setArmor();

        if (this instanceof DRWitherSkeleton) {
            DRWitherSkeleton monster = (DRWitherSkeleton) this;
            monster.setSkeletonType(1);
            monster.setSize(0.7F, 2.4F);
        }

        getBukkit().setRemoveWhenFarAway(false);
        EntityAPI.getEntityAttributes().put(this, new AttributeList());
        EntityAPI.registerBoss(this, level, getTier());
        say(getBossType().getGreeting());
    }

    default void setVulnerable(boolean b) {
        if (isVulnerable() == b)
            return;

        if (b) {
            DamageAPI.removeInvulnerable(getBukkit());
        } else {
            DamageAPI.setInvulnerable(getBukkit());
        }
    }

    default boolean isVulnerable() {
        return !DamageAPI.isInvulnerable(getBukkit());
    }

    default List<Block> getNearbyBlocks(Location loc, int maxradius) {
        List<Block> return_list = new ArrayList<>();
        BlockFace[] faces = {BlockFace.UP, BlockFace.NORTH, BlockFace.EAST};
        BlockFace[][] orth = {{BlockFace.NORTH, BlockFace.EAST}, {BlockFace.UP, BlockFace.EAST}, {BlockFace.NORTH, BlockFace.UP}};
        for (int r = 0; r <= maxradius; r++) {
            for (int s = 0; s < 6; s++) {
                BlockFace f = faces[s % 3];
                BlockFace[] o = orth[s % 3];
                if (s >= 3)
                    f = f.getOppositeFace();
                if (!(loc.getBlock().getRelative(f, r) == null)) {
                    Block c = loc.getBlock().getRelative(f, r);

                    for (int x = -r; x <= r; x++) {
                        for (int y = -r; y <= r; y++) {
                            Block a = c.getRelative(o[0], x).getRelative(o[1], y);
                            return_list.add(a);
                        }
                    }
                }
            }
        }
        return return_list;
    }
}
