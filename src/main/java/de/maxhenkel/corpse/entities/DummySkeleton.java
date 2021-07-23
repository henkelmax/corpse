package de.maxhenkel.corpse.entities;

import de.maxhenkel.corpse.Main;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DummySkeleton extends Skeleton {

    public DummySkeleton(Level world, NonNullList<ItemStack> equipment) {
        super(EntityType.SKELETON, world);
        if (Main.SERVER_CONFIG.renderEquipment.get()) {
            for (EquipmentSlot type : EquipmentSlot.values()) {
                setItemSlot(type, equipment.get(type.ordinal()));
            }
        }
    }

}
