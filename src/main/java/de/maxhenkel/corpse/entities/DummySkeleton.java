package de.maxhenkel.corpse.entities;

import de.maxhenkel.corpse.Main;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

public class DummySkeleton extends SkeletonEntity {

    public DummySkeleton(World world, NonNullList<ItemStack> equipment) {
        super(EntityType.SKELETON, world);
        if(Main.SERVER_CONFIG.renderEquipment.get()){
            for (EquipmentSlotType type : EquipmentSlotType.values()) {
                setItemSlot(type, equipment.get(type.ordinal()));
            }
        }
    }

}
