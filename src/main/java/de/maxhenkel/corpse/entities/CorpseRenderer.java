package de.maxhenkel.corpse.entities;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.maxhenkel.corelib.CachedMap;
import de.maxhenkel.corpse.CorpseMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;

import java.util.UUID;

public class CorpseRenderer extends EntityRenderer<CorpseEntity, CorpseRenderState> {

    private final CachedMap<UUID, DummyPlayer> players;
    private final CachedMap<UUID, DummySkeleton> skeletons;
    private static final Minecraft MC = Minecraft.getInstance();

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
    public void render(CorpseRenderState state, PoseStack stack, MultiBufferSource source, int packedLight) {
        super.render(state, stack, source, packedLight);

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
            if (state.skeletonRenderState != null && state.skeletonRenderer != null) {
                state.skeletonRenderer.render(state.skeletonRenderState, stack, source, packedLight);
            }
        } else {
            if (state.playerRenderState != null && state.playerRenderer != null) {
                state.playerRenderer.render(state.playerRenderState, stack, source, packedLight);
            }
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
            if (state.skeletonRenderer == null) {
                state.skeletonRenderer = (SkeletonRenderer) MC.getEntityRenderDispatcher().getRenderer(skeleton);
            }
            if (state.skeletonRenderState == null) {
                state.skeletonRenderState = state.skeletonRenderer.createRenderState();
            }
            state.skeletonRenderer.extractRenderState(skeleton, state.skeletonRenderState, 0F);
        } else {
            DummyPlayer dummyPlayer = players.get(corpse.getUUID(), () -> new DummyPlayer((ClientLevel) corpse.level(), new GameProfile(corpse.getPlayerUuid(), corpse.getCorpseName()), corpse.getEquipment(), corpse.getCorpseModel()));//;
            if (state.playerRenderer == null) {
                state.playerRenderer = (PlayerRenderer) MC.getEntityRenderDispatcher().getRenderer(dummyPlayer);
            }
            if (state.playerRenderState == null) {
                state.playerRenderState = state.playerRenderer.createRenderState();
            }
            state.playerRenderer.extractRenderState(dummyPlayer, state.playerRenderState, 0F);
        }
    }

}
