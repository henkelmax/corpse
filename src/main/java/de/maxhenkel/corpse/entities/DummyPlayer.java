package de.maxhenkel.corpse.entities;

import com.mojang.authlib.GameProfile;
import de.maxhenkel.corpse.Main;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class DummyPlayer extends RemoteClientPlayerEntity {

    private final byte model;

    public DummyPlayer(ClientWorld world, GameProfile gameProfile, NonNullList<ItemStack> equipment, byte model) {
        super(world, gameProfile);
        this.model = model;
        if (Main.SERVER_CONFIG.renderEquipment.get()) {
            for (EquipmentSlotType type : EquipmentSlotType.values()) {
                setItemSlot(type, equipment.get(type.ordinal()));
            }
        }
        refreshDimensions();
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public EntitySize getDimensions(Pose pose) {
        return new EntitySize(super.getDimensions(pose).width, Float.MAX_VALUE, true);
    }

    @Override
    public boolean isModelPartShown(PlayerModelPart part) {
        return (model & part.getMask()) == part.getMask();
    }
}
