package de.maxhenkel.corpse.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public abstract class CorpseBoundingBoxBase extends Entity {

    private static final AxisAlignedBB NULL_AABB = new AxisAlignedBB(0D, 0D, 0D, 0D, 0D, 0D);

    private AxisAlignedBB boundingBox;

    public CorpseBoundingBoxBase(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
        boundingBox = NULL_AABB;
    }

    public void recalculateBoundingBox() {
        Direction facing = entityData == null ? Direction.NORTH : Direction.fromYRot(yRot);
        boundingBox = new AxisAlignedBB(
                getX() - (facing.getStepX() != 0 ? 1D : 0.5D),
                getY(),
                getZ() - (facing.getStepZ() != 0 ? 1D : 0.5D),
                getX() + (facing.getStepX() != 0 ? 1D : 0.5D),
                getY() + 0.5D,
                getZ() + (facing.getStepZ() != 0 ? 1D : 0.5D)
        );
    }

    @Override
    public void tick() {
        super.tick();
        recalculateBoundingBox();
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return boundingBox;
    }

    @Override
    public void setBoundingBox(AxisAlignedBB boundingBox) {
        this.boundingBox = boundingBox;
    }

    @Override
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);
        recalculateBoundingBox();
    }
}
