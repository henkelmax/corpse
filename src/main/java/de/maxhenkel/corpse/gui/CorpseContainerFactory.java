package de.maxhenkel.corpse.gui;

import de.maxhenkel.corelib.death.Death;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.network.IContainerFactory;

import java.util.Optional;
import java.util.UUID;

public abstract class CorpseContainerFactory<T extends CorpseContainerBase> implements IContainerFactory<T> {
    @Override
    public T create(int windowId, PlayerInventory inv, PacketBuffer buffer) {
        boolean isHistory = buffer.readBoolean();
        Death death = Death.fromNBT(buffer.readCompoundTag());
        if (isHistory) {
            return create(windowId, inv, CorpseEntity.createFromDeath(inv.player, death), inv.player.abilities.isCreativeMode, isHistory);
        } else {
            UUID uuid = buffer.readUniqueId();

            AxisAlignedBB aabb = inv.player.getBoundingBox();
            aabb = aabb.grow(10D);
            Optional<CorpseEntity> entity = inv.player.world.getEntitiesWithinAABB(CorpseEntity.class, aabb)
                    .stream()
                    .filter(corpse -> corpse.getUniqueID().equals(uuid) && corpse.getDistance(inv.player) <= 5)
                    .findFirst();
            return entity.map(corpseEntity -> {
                corpseEntity.setDeath(death);
                return create(windowId, inv.player.inventory, corpseEntity, true, isHistory);
            }).orElse(null);
        }
    }

    public abstract T create(int id, PlayerInventory playerInventory, CorpseEntity corpse, boolean editable, boolean history);
}
