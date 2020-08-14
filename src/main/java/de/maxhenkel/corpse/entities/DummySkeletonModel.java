package de.maxhenkel.corpse.entities;

import net.minecraft.client.renderer.entity.model.SkeletonModel;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.MobEntity;

public class DummySkeletonModel<T extends MobEntity & IRangedAttackMob> extends SkeletonModel<T> {

    @Override
    public void setRotationAngles(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

}
