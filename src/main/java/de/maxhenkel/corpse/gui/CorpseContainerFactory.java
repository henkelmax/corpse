package de.maxhenkel.corpse.gui;

import de.maxhenkel.corelib.death.Death;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.IContainerFactory;

import javax.annotation.Nullable;
import java.util.UUID;

public abstract class CorpseContainerFactory<T extends CorpseContainerBase> implements IContainerFactory<T> {

    @Override
    public T create(int windowId, Inventory inv, RegistryFriendlyByteBuf buffer) {
        boolean isHistory = buffer.readBoolean();
        boolean additionalItemsEmpty = buffer.readBoolean();
        if (isHistory) {
            Death death = Death.read(buffer.registryAccess(), buffer.readNbt());
            if (!additionalItemsEmpty) {
                // That the client knows if the additional items slot isn't empty
                death.getAdditionalItems().add(new ItemStack(Items.STONE));
            }
            return create(windowId, inv, CorpseEntity.createFromDeath(inv.player, death), inv.player.getAbilities().instabuild, isHistory);
        } else {
            UUID uuid = buffer.readUUID();

            double maxDistance = inv.player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE) + 2D;
            AABB aabb = inv.player.getBoundingBox().inflate(maxDistance);
            @Nullable CorpseEntity corpseEntity = inv.player.level().getEntitiesOfClass(CorpseEntity.class, aabb)
                    .stream()
                    .filter(corpse -> corpse.getUUID().equals(uuid) && corpse.distanceTo(inv.player) <= maxDistance)
                    .findFirst().orElse(null);

            if (corpseEntity != null && !additionalItemsEmpty) {
                // That the client knows if the additional items slot isn't empty
                corpseEntity.getDeath().getAdditionalItems().add(new ItemStack(Items.STONE));
            }
            return create(windowId, inv.player.getInventory(), corpseEntity, true, isHistory);

        }
    }

    public abstract T create(int id, Inventory playerInventory, @Nullable CorpseEntity corpse, boolean editable, boolean history);
}
