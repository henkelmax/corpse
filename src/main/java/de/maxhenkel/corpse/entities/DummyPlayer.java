package de.maxhenkel.corpse.entities;

import com.mojang.authlib.GameProfile;
import de.maxhenkel.corpse.Main;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.Pose;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class DummyPlayer extends RemoteClientPlayerEntity {

    public DummyPlayer(ClientWorld world, GameProfile gameProfile, NonNullList<ItemStack> equipment) {
        super(world, gameProfile);
        if (Main.SERVER_CONFIG.renderEquipment.get()) {
            for (EquipmentSlotType type : EquipmentSlotType.values()) {
                setItemStackToSlot(type, equipment.get(type.ordinal()));
            }
        }
        recalculateSize();
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public EntitySize getSize(Pose pose) {
        return new EntitySize(super.getSize(pose).width, Float.MAX_VALUE, true);
    }

}
