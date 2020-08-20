package de.maxhenkel.corpse.integration.waila;

import de.maxhenkel.corpse.Death;
import de.maxhenkel.corpse.DeathManager;
import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.gui.DeathHistoryScreen;
import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.*;
import mcp.mobius.waila.utils.ModIdentification;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

public class HUDHandlerCorpse implements IEntityComponentProvider, IServerDataProvider<Entity> {

    static final ResourceLocation OBJECT_NAME_TAG = new ResourceLocation("waila", "object_name");
    static final ResourceLocation CONFIG_SHOW_REGISTRY = new ResourceLocation("waila", "show_registry");
    static final ResourceLocation REGISTRY_NAME_TAG = new ResourceLocation("waila", "registry_name");

    static final HUDHandlerCorpse INSTANCE = new HUDHandlerCorpse();

    @Override
    public void appendHead(List<ITextComponent> t, IEntityAccessor accessor, IPluginConfig config) {
        ITaggableList<ResourceLocation, ITextComponent> tooltip = (ITaggableList<ResourceLocation, ITextComponent>) t;
        tooltip.setTag(OBJECT_NAME_TAG, new StringTextComponent(String.format(Waila.CONFIG.get().getFormatting().getEntityName(), accessor.getEntity().getDisplayName().getString())));
        if (config.get(CONFIG_SHOW_REGISTRY)) {
            tooltip.setTag(REGISTRY_NAME_TAG, new StringTextComponent(accessor.getEntity().getType().getRegistryName().toString()).func_240699_a_(TextFormatting.GRAY));
        }
    }

    @Override
    public void appendBody(List<ITextComponent> tooltip, IEntityAccessor accessor, IPluginConfig config) {
        if (!(accessor.getEntity() instanceof CorpseEntity)) {
            return;
        }

        CompoundNBT data = accessor.getServerData();

        if (data.contains("death")) {
            Death death = Death.fromNBT(data.getCompound("death"));
            tooltip.add(new TranslationTextComponent("tooltip.corpse.death_date", DeathHistoryScreen.getDate(death.getTimestamp())));
        }

        if (data.contains("item_count")) {
            tooltip.add(new TranslationTextComponent("tooltip.corpse.item_count", data.getInt("item_count")));
        }
    }

    @Override
    public void appendTail(List<ITextComponent> tooltip, IEntityAccessor accessor, IPluginConfig config) {
        tooltip.add(new StringTextComponent(String.format(Waila.CONFIG.get().getFormatting().getModName(), ModIdentification.getModInfo(accessor.getEntity()).getName())));
    }

    @Override
    public void appendServerData(CompoundNBT compoundNBT, ServerPlayerEntity serverPlayerEntity, World world, Entity entity) {
        CorpseEntity corpse = (CorpseEntity) entity;
        UUID uuid = corpse.getDeathUUID();
        if (uuid == null) {
            return;
        }

        Death death = DeathManager.getDeath(serverPlayerEntity, uuid);
        if (death != null) {
            compoundNBT.put("death", death.toNBT(false));
        }

        compoundNBT.putInt("item_count", getStackCount(corpse));
    }

    public static int getStackCount(IInventory inventory) {
        int count = 0;

        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stackInSlot = inventory.getStackInSlot(i);
            if (!stackInSlot.isEmpty()) {
                count++;
            }
        }
        return count;
    }

}