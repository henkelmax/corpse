package de.maxhenkel.corpse.entities;

import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.RangedAttackMob;

public class DummySkeletonModel<T extends Mob & RangedAttackMob> extends SkeletonModel<T> {

    public DummySkeletonModel(ModelPart modelPart) {
        super(modelPart);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

}
