package de.maxhenkel.corpse.gui;

import de.maxhenkel.corpse.entities.EntityCorpse;
import de.maxhenkel.corpse.proxy.CommonProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

    public static final int GUI_CORPSE = 0;
    public static final int GUI_DEATH_HISTORY = 1;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {

        if (id == GUI_CORPSE) {
            EntityCorpse corpse = getCorpse(world, x, y, z);
            if (corpse != null) {
                return new ContainerCorpse(player.inventory, corpse, true);
            }
        } else if (id == GUI_DEATH_HISTORY) {
            return new ContainerCorpse(player.inventory, EntityCorpse.createFromDeath(player, CommonProxy.getDeathToShow(player)), player.capabilities.isCreativeMode);
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {

        if (id == GUI_CORPSE) {
            EntityCorpse corpse = getCorpse(world, x, y, z);
            if (corpse != null) {
                return new GUICorpse(player.inventory, corpse, true);
            }
        } else if (id == GUI_DEATH_HISTORY) {
            return new GUICorpse(player.inventory, EntityCorpse.createFromDeath(player, CommonProxy.getDeathToShow(player)), player.capabilities.isCreativeMode);
        }

        return null;
    }

    public static EntityCorpse getCorpse(World world, int posX, int posY, int posZ) {
        return world.getEntitiesWithinAABB(
                EntityCorpse.class,
                new AxisAlignedBB(posX - 0.5D, posY - 0.5D, posZ - 0.5D, posX + 0.5D, posY + 0.5D, posZ + 0.5D),
                input -> input.getPosition().equals(new BlockPos(posX, posY, posZ))
        )
                .stream()
                .findFirst()
                .orElse(null);
    }
}
