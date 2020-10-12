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
        Direction facing = dataManager == null ? Direction.NORTH : Direction.fromAngle(rotationYaw);
        boundingBox = new AxisAlignedBB(
                getPosX() - (facing.getXOffset() != 0 ? 1D : 0.5D),
                getPosY(),
                getPosZ() - (facing.getZOffset() != 0 ? 1D : 0.5D),
                getPosX() + (facing.getXOffset() != 0 ? 1D : 0.5D),
                getPosY() + 0.5D,
                getPosZ() + (facing.getZOffset() != 0 ? 1D : 0.5D)
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
    public void setPosition(double x, double y, double z) {
        super.setPosition(x, y, z);
        recalculateBoundingBox();
    }
}
