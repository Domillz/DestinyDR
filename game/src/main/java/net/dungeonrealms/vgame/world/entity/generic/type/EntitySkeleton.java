package net.dungeonrealms.vgame.world.entity.generic.type;

import lombok.Getter;
import net.dungeonrealms.api.creature.lib.craft.CreatureCow;
import net.dungeonrealms.api.creature.lib.craft.CreaturePig;
import net.dungeonrealms.api.creature.lib.craft.CreatureSkeleton;
import net.dungeonrealms.api.creature.lib.craft.skeleton.EnumSkeletonType;
import net.dungeonrealms.api.creature.lib.intelligence.EnumIntelligenceType;
import net.dungeonrealms.api.exception.InvalidEntityDataException;
import net.dungeonrealms.vgame.world.entity.generic.EnumGameEntity;
import net.dungeonrealms.vgame.world.entity.generic.IGameEntity;
import net.dungeonrealms.vgame.world.entity.generic.construct.EntityData;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.EntityHuman;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.Random;

/**
 * Created by Giovanni on 26-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class EntitySkeleton extends CreatureSkeleton implements IGameEntity {

    @Getter
    private EntityData entityData;

    @Getter
    private Entity entity;

    public EntitySkeleton(World world, EnumSkeletonType skeletonType, EntityData entityData) throws InvalidEntityDataException {
        super(world, EnumIntelligenceType.HOSTILE, skeletonType);

        this.entityData = entityData;
        this.entity = this;

        if (!this.entityData.isNull()) {
            String elementName = null;
            if (this.isElemental()) {
                elementName = this.entityData.getEntityElement().getColor() +
                        this.entityData.getEntityElement().getSimpleNames()
                                .get(new Random().nextInt(this.getEntityData().getEntityElement().getSimpleNames().size()));
            }
            String simpleName;
            if (elementName != null) {
                simpleName = EnumGameEntity.SKELETON.getSimpleNames().get(new Random().nextInt(EnumGameEntity.SKELETON.getSimpleNames().size())) + elementName;
            } else
                simpleName = EnumGameEntity.SKELETON.getSimpleNames().get(new Random().nextInt(EnumGameEntity.SKELETON.getSimpleNames().size()));

            this.setCustomName(ChatColor.AQUA + "[Lvl. " + this.entityData.getLevel() + "]"
                    + this.entityData.getEntityTier().getColor() + simpleName + " Bandit");
            this.entityData.setName(this.getCustomName());

            this.addTargets(Arrays.asList(EntityHuman.class, CreatureCow.class, CreaturePig.class, EntityBandit.class));
        } else
            throw new InvalidEntityDataException(this.entityData);
    }
}
