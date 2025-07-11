package de.maxhenkel.corpse.integration.waila;

import de.maxhenkel.corelib.death.Death;
import de.maxhenkel.corpse.CorpseMod;
import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.gui.DeathHistoryScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class HUDHandlerCorpse implements IEntityComponentProvider {

    public static final ResourceLocation OBJECT_NAME_TAG = ResourceLocation.fromNamespaceAndPath("jade", "object_name");

    public static final HUDHandlerCorpse INSTANCE = new HUDHandlerCorpse();

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(CorpseMod.MODID, "corpse");

    @Override
    public void appendTooltip(ITooltip iTooltip, EntityAccessor entityAccessor, IPluginConfig iPluginConfig) {
        if (entityAccessor.getEntity() instanceof CorpseEntity corpse) {
            iTooltip.remove(OBJECT_NAME_TAG);
            iTooltip.add(corpse.getDisplayName().copy().withStyle(ChatFormatting.WHITE));

            CompoundTag data = entityAccessor.getServerData();
            data.getCompound("Death").ifPresent(deathTag -> {
                Death death = Death.read(corpse.registryAccess(), deathTag);
                long timestamp = death.getTimestamp();
                if (timestamp > 0L) {
                    iTooltip.add(Component.translatable("tooltip.corpse.death_date", DeathHistoryScreen.getDate(timestamp)));
                }
            });

            if (data.contains("ItemCount")) {
                iTooltip.add(Component.translatable("tooltip.corpse.item_count", data.getIntOr("ItemCount", 0)));
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}