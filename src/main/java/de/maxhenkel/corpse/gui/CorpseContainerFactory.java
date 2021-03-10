package de.maxhenkel.corpse.gui;

import de.maxhenkel.corelib.death.Death;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.network.IContainerFactory;

import java.util.Optional;
import java.util.UUID;

public abstract class CorpseContainerFactory<T extends CorpseContainerBase> implements IContainerFactory<T> {
    @Override
    public T create(int windowId, PlayerInventory inv, PacketBuffer buffer) {
        boolean isHistory = buffer.readBoolean();
        boolean additionalItemsEmpty = buffer.readBoolean();
        if (isHistory) {
            Death death = Death.fromNBT(buffer.readNbt());
            if (!additionalItemsEmpty) {
                // That the client knows if the additional items slot isn't empty
                death.getAdditionalItems().add(new ItemStack(Items.STONE));
            }
            return create(windowId, inv, CorpseEntity.createFromDeath(inv.player, death), inv.player.abilities.instabuild, isHistory);
        } else {
            UUID uuid = buffer.readUUID();

            AxisAlignedBB aabb = inv.player.getBoundingBox();
            aabb = aabb.inflate(10D);
            Optional<CorpseEntity> entity = inv.player.level.getEntitiesOfClass(CorpseEntity.class, aabb)
                    .stream()
                    .filter(corpse -> corpse.getUUID().equals(uuid) && corpse.distanceTo(inv.player) <= 5)
                    .findFirst();
            return entity.map(corpseEntity -> {
                if (!additionalItemsEmpty) {
                    // That the client knows if the additional items slot isn't empty
                    corpseEntity.getDeath().getAdditionalItems().add(new ItemStack(Items.STONE));
                }
                return create(windowId, inv.player.inventory, corpseEntity, true, isHistory);
            }).orElse(null);
        }
    }

    public abstract T create(int id, PlayerInventory playerInventory, CorpseEntity corpse, boolean editable, boolean history);
}
