package de.maxhenkel.corpse.entities;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.maxhenkel.corelib.CachedMap;
import de.maxhenkel.corpse.CorpseMod;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;

import java.util.UUID;

public class CorpseRenderer extends EntityRenderer<CorpseEntity, CorpseRenderState> {

    private final CachedMap<UUID, DummyPlayer> players;
    private final CachedMap<UUID, DummySkeleton> skeletons;

    public CorpseRenderer(EntityRendererProvider.Context renderer) {
        super(renderer);
        players = new CachedMap<>(10_000L);
        skeletons = new CachedMap<>(10_000L);
    }

    @Override
    public CorpseRenderState createRenderState() {
        return new CorpseRenderState();
    }

    @Override
    public void submit(CorpseRenderState state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        super.submit(state, stack, collector, cameraRenderState);

        stack.pushPose();

        stack.mulPose(Axis.YP.rotationDegrees(-state.yRot));

        if (CorpseMod.SERVER_CONFIG.spawnCorpseOnFace.get()) {
            stack.mulPose(Axis.XP.rotationDegrees(90F));
            stack.translate(0D, -1D, -2.01D / 16D);
        } else {
            stack.mulPose(Axis.XP.rotationDegrees(-90F));
            stack.translate(0D, -1D, 2.01D / 16D);
        }

        if (state.skeleton) {
            entityRenderDispatcher.getRenderer(state.skeletonRenderState).submit(state.skeletonRenderState, stack, collector, cameraRenderState);
        } else {
            entityRenderDispatcher.getRenderer(state.playerRenderState).submit(state.playerRenderState, stack, collector, cameraRenderState);
        }

        stack.popPose();
    }

    @Override
    public void extractRenderState(CorpseEntity corpse, CorpseRenderState state, float partialTicks) {
        super.extractRenderState(corpse, state, partialTicks);

        state.yRot = corpse.getYRot();
        state.skeleton = corpse.isSkeleton();
        if (corpse.isSkeleton()) {
            DummySkeleton skeleton = skeletons.get(corpse.getUUID(), () -> new DummySkeleton(corpse.level(), corpse.getEquipment()));
            ((SkeletonRenderer) entityRenderDispatcher.getRenderer(state.skeletonRenderState)).extractRenderState(skeleton, state.skeletonRenderState, 0F);
            state.skeletonRenderState.lightCoords = state.lightCoords;
        } else {
            DummyPlayer dummyPlayer = players.get(corpse.getUUID(), () -> new DummyPlayer((ClientLevel) corpse.level(), new GameProfile(corpse.getPlayerUuid(), corpse.getCorpseName()), corpse.getEquipment(), corpse.getCorpseModel()));
            ((AvatarRenderer) entityRenderDispatcher.getRenderer(state.playerRenderState)).extractRenderState(dummyPlayer, state.playerRenderState, 0F);
            state.playerRenderState.lightCoords = state.lightCoords;
        }
    }

}
