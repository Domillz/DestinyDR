package net.dungeonrealms.items.armor;

import org.bukkit.ChatColor;

/**
 * Created by Nick on 9/21/2015.
 */
public class Armor {

    public enum EquipmentType {
        HELMET(0, "Helment"),
        CHESTPLATE(1, "Chestplate"),
        LEGGINGS(2, "Leggings"),
        BOOTS(3, "Boots");

        private int id;
        private String name;

        EquipmentType(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public static EquipmentType getById(int id) {
            for (EquipmentType it : values()) {
                if (it.getId() == id) {
                    return it;
                }
            }
            return null;
        }
    }

    public enum ArmorTier {
        TIER_1(0, 1, new Integer[]{1, 10}, 3),
        TIER_2(1, 2, new Integer[]{10, 20}, 4),
        TIER_3(2, 3, new Integer[]{20, 30}, 6),
        TIER_4(3, 4, new Integer[]{30, 40}, 8),
        TIER_5(4, 5, new Integer[]{40, 50}, 13),;

        private int id;
        private int tierId;
        private Integer[] rangeValues;
        private int attributeRange;

        ArmorTier(int id, int tierId, Integer[] rangeValues, int attributeRange) {
            this.id = id;
            this.tierId = tierId;
            this.rangeValues = rangeValues;
            this.attributeRange = attributeRange;
        }

        public int getId() {
            return id;
        }

        public int getTierId() {
            return tierId;
        }

        public Integer[] getRangeValues() {
            return rangeValues;
        }

        public int getAttributeRange() {
            return attributeRange;
        }

        public static ArmorTier getById(int id) {
            for (ArmorTier it : values()) {
                if (it.getId() == id) {
                    return it;
                }
            }
            return null;
        }
    }

    public enum ArmorModifier {
        COMMON(0, ChatColor.GRAY + "Common" + ChatColor.RESET),
        UNCOMMON(1, ChatColor.GREEN + "Uncommon" + ChatColor.RESET),
        RARE(2, ChatColor.AQUA + "Rare" + ChatColor.RESET),
        UNIQUE(3, ChatColor.YELLOW + "Unique" + ChatColor.RESET),
        LEGENDARY(4, ChatColor.GOLD + "Legendary" + ChatColor.RESET),;

        private int id;
        private String name;

        ArmorModifier(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public static ArmorModifier getById(int id) {
            for (ArmorModifier im : values()) {
                if (im.getId() == id) {
                    return im;
                }
            }
            return null;
        }
    }

    public enum ArmorAttributeType {
        ARMOR(0, "Armor", "armor"),
        HEALTH_POINTS(1, "Health Points", "healthPoints"),
        HEALTH_REGEN(2, "Health Regen", "healthRegen"),
        ENERGY(3, "Energy", "energy"),
        ENERGY_REGEN(4, "Energy Regen", "energyRegen"),
        INTELLECT(5, "Intellect", "intellect"),
        FIRE_RESISTANCE(6, "Fire Resistance", "fireResistance"),
        BLOCK(7, "Block", "block"),
        GEM_FIND(8, "Gem Find", "gemFind"),
        THRONES(9, "Thrones", "thornes"),
        STRENGTH(10, "Strength", "strength"),
        VITALITY(11, "Vitality", "vitality"),
        DODGE(12, "Dodge", "dodge"),
        DAMAGE(13, "Damage", "damage"),;

        private int id;
        private String name;
        private String NBTName;

        ArmorAttributeType(int id, String name, String NBTName) {
            this.id = id;
            this.name = name;
            this.NBTName = NBTName;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getNBTName() {
            return NBTName;
        }

        public static ArmorAttributeType getById(int id) {
            for (ArmorAttributeType at : values()) {
                if (at.getId() == id) {
                    return at;
                }
            }
            return null;
        }
    }

}
