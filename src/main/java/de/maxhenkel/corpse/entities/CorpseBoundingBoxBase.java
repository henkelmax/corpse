package de.maxhenkel.corpse.entities;


import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public abstract class CorpseBoundingBoxBase extends Entity {

    public CorpseBoundingBoxBase(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    public void recalculateBoundingBox() {
        Direction facing = entityData == null ? Direction.NORTH : Direction.fromYRot(getYRot());
        setBoundingBox(new AABB(
                getX() - (facing.getStepX() != 0 ? 1D : 0.5D),
                getY(),
                getZ() - (facing.getStepZ() != 0 ? 1D : 0.5D),
                getX() + (facing.getStepX() != 0 ? 1D : 0.5D),
                getY() + 0.5D,
                getZ() + (facing.getStepZ() != 0 ? 1D : 0.5D)
        ));
    }

    @Override
    public void tick() {
        super.tick();
        recalculateBoundingBox();
    }

    @Override
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);
        recalculateBoundingBox();
    }
}
