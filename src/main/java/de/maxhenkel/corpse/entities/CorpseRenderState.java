package de.maxhenkel.corpse.entities;

import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;

import javax.annotation.Nullable;

public class CorpseRenderState extends EntityRenderState {

    public float yRot;

    @Nullable
    public PlayerRenderer playerRenderer;
    @Nullable
    public PlayerRenderState playerRenderState;
    @Nullable
    public SkeletonRenderer skeletonRenderer;
    @Nullable
    public SkeletonRenderState skeletonRenderState;

    public boolean skeleton;

}
