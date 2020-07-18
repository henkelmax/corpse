package de.maxhenkel.corpse.gui;

import de.maxhenkel.corpse.Death;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.network.IContainerFactory;

import java.util.Optional;
import java.util.UUID;

public class CorpseContainerFactory implements IContainerFactory<CorpseContainer> {
    @Override
    public CorpseContainer create(int windowId, PlayerInventory inv, PacketBuffer buffer) {
        boolean isHistory = buffer.readBoolean();
        if (isHistory) {
            Death death = Death.fromNBT(buffer.readCompoundTag());
            return new CorpseContainer(windowId, inv, CorpseEntity.createFromDeath(inv.player, death), inv.player.abilities.isCreativeMode, isHistory);
        } else {
            UUID uuid = new UUID(buffer.readLong(), buffer.readLong());

            AxisAlignedBB aabb = inv.player.getBoundingBox();
            if (aabb == null) {
                return null;
            }
            aabb = aabb.grow(10D);
            Optional<CorpseEntity> entity = inv.player.world.getEntitiesWithinAABB(CorpseEntity.class, aabb)
                    .stream()
                    .filter(corpse -> corpse.getUniqueID().equals(uuid) && corpse.getDistance(inv.player) <= 5)
                    .findFirst();
            if (!entity.isPresent()) {
                return null;
            }
            return new CorpseContainer(windowId, inv.player.inventory, entity.get(), true, isHistory);
        }
    }
}
