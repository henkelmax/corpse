package de.maxhenkel.corpse.integration.waila;

import de.maxhenkel.corelib.death.Death;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.gui.DeathHistoryScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class HUDHandlerCorpse implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {

    public static final ResourceLocation OBJECT_NAME_TAG = new ResourceLocation("jade", "object_name");

    public static final HUDHandlerCorpse INSTANCE = new HUDHandlerCorpse();

    private static final ResourceLocation UID = new ResourceLocation(Main.MODID, "corpse");

    @Override
    public void appendTooltip(ITooltip iTooltip, EntityAccessor entityAccessor, IPluginConfig iPluginConfig) {
        if (entityAccessor.getEntity() instanceof CorpseEntity corpse) {
            iTooltip.remove(OBJECT_NAME_TAG);
            iTooltip.add(corpse.getDisplayName().copy().withStyle(ChatFormatting.WHITE));

            CompoundTag data = entityAccessor.getServerData();
            if (data.contains("Death")) {
                Death death = Death.fromNBT(corpse.registryAccess(), data.getCompound("Death"));
                long timestamp = death.getTimestamp();
                if (timestamp > 0L) {
                    iTooltip.add(Component.translatable("tooltip.corpse.death_date", DeathHistoryScreen.getDate(timestamp)));
                }
            }

            if (data.contains("ItemCount")) {
                iTooltip.add(Component.translatable("tooltip.corpse.item_count", data.getInt("ItemCount")));
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, EntityAccessor entityAccessor) {
        if (entityAccessor.getEntity() instanceof CorpseEntity corpse) {
            Death death = corpse.getDeath();
            compoundTag.put("Death", death.toNBT(corpse.registryAccess(), false));
            compoundTag.putInt("ItemCount", (int) death.getAllItems().stream().filter(itemStack -> !itemStack.isEmpty()).count());
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}