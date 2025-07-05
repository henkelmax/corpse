package de.maxhenkel.corpse.integration.waila;

import de.maxhenkel.corelib.death.Death;
import de.maxhenkel.corpse.CorpseMod;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IServerDataProvider;

public class DataProviderCorpse implements IServerDataProvider<EntityAccessor> {

    public static final DataProviderCorpse INSTANCE = new DataProviderCorpse();

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(CorpseMod.MODID, "corpse_data");

    @Override
    public void appendServerData(CompoundTag compoundTag, EntityAccessor entityAccessor) {
        if (entityAccessor.getEntity() instanceof CorpseEntity corpse) {
            Death death = corpse.getDeath();
            compoundTag.put("Death", death.write(corpse.registryAccess(), false));
            compoundTag.putInt("ItemCount", (int) death.getAllItems().stream().filter(itemStack -> !itemStack.isEmpty()).count());
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}