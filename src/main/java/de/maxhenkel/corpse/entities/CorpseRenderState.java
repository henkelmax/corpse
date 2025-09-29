package de.maxhenkel.corpse.entities;

import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;

public class CorpseRenderState extends EntityRenderState {

    public float yRot;
    public AvatarRenderState playerRenderState = new AvatarRenderState();
    public SkeletonRenderState skeletonRenderState = new SkeletonRenderState();
    public boolean skeleton;

}
