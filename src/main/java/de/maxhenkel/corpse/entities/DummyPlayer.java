package de.maxhenkel.corpse.entities;

import com.mojang.authlib.GameProfile;
import de.maxhenkel.corpse.CorpseMod;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForgeMod;

import java.util.EnumMap;

public class DummyPlayer extends RemotePlayer {

    private final byte model;

    public DummyPlayer(ClientLevel world, GameProfile gameProfile, EnumMap<EquipmentSlot, ItemStack> equipment, byte model) {
        super(world, gameProfile);
        this.model = model;
        if (CorpseMod.SERVER_CONFIG.renderEquipment.get()) {
            for (EnumMap.Entry<EquipmentSlot, ItemStack> entry : equipment.entrySet()) {
                setItemSlot(entry.getKey(), entry.getValue());
            }
        }
        AttributeInstance attribute = getAttributes().getInstance(NeoForgeMod.NAMETAG_DISTANCE);
        if (attribute != null) {
            attribute.setBaseValue(0D);
        }

        setPos(0D, 0D, 0D);
        xo = 0D;
        yo = 0D;
        zo = 0D;
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isModelPartShown(PlayerModelPart part) {
        return (model & part.getMask()) == part.getMask();
    }
}
