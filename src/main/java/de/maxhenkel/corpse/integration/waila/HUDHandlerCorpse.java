package de.maxhenkel.corpse.integration.waila;

import de.maxhenkel.corelib.death.Death;
import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.gui.DeathHistoryScreen;
import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.*;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class HUDHandlerCorpse implements IEntityComponentProvider, IServerDataProvider<Entity> {

    public static final ResourceLocation OBJECT_NAME_TAG = new ResourceLocation("waila", "object_name");

    public static final HUDHandlerCorpse INSTANCE = new HUDHandlerCorpse();

    @Override
    public void appendTooltip(ITooltip iTooltip, EntityAccessor entityAccessor, IPluginConfig iPluginConfig) {
        if (entityAccessor.getEntity() instanceof CorpseEntity corpse) {
            if (entityAccessor.getTooltipPosition().equals(TooltipPosition.BODY)) {
                CompoundTag data = entityAccessor.getServerData();

                if (data.contains("Death")) {
                    Death death = Death.fromNBT(data.getCompound("Death"));
                    long timestamp = death.getTimestamp();
                    if (timestamp > 0L) {
                        iTooltip.add(new TranslatableComponent("tooltip.corpse.death_date", DeathHistoryScreen.getDate(timestamp)));
                    }
                }

                if (data.contains("ItemCount")) {
                    iTooltip.add(new TranslatableComponent("tooltip.corpse.item_count", data.getInt("ItemCount")));
                }
            } else if (entityAccessor.getTooltipPosition().equals(TooltipPosition.HEAD)) {
                iTooltip.remove(OBJECT_NAME_TAG);
                iTooltip.add(new TextComponent(String.format(Waila.CONFIG.get().getFormatting().getBlockName(), corpse.getDisplayName().getString())).withStyle(ChatFormatting.WHITE));
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, ServerPlayer serverPlayer, Level level, Entity entity, boolean b) {
        if (entity instanceof CorpseEntity corpse) {
            Death death = corpse.getDeath();
            compoundTag.put("Death", death.toNBT(false));
            compoundTag.putInt("ItemCount", (int) death.getAllItems().stream().filter(itemStack -> !itemStack.isEmpty()).count());
        }
    }

}