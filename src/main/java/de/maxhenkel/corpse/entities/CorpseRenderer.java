package de.maxhenkel.corpse.entities;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.corelib.CachedMap;
import de.maxhenkel.corelib.client.PlayerSkins;
import de.maxhenkel.corpse.Main;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

import java.util.UUID;

public class CorpseRenderer extends EntityRenderer<CorpseEntity> {

    private final CachedMap<CorpseEntity, DummyPlayer> players;
    private final CachedMap<CorpseEntity, DummySkeleton> skeletons;
    private static final Minecraft MC = Minecraft.getInstance();

    public CorpseRenderer(EntityRendererManager renderManager) {
        super(renderManager);
        players = new CachedMap<>(10_000L);
        skeletons = new CachedMap<>(10_000L);
    }

    @Override
    public ResourceLocation getTextureLocation(CorpseEntity entity) {
        return null;
    }

    @Override
    public void render(CorpseEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLightIn) {
        super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLightIn);
        matrixStack.pushPose();

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(-entity.yRot));

        if (Main.SERVER_CONFIG.spawnCorpseOnFace.get()) {
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(90F));
            matrixStack.translate(0D, -1D, -2.01D / 16D);
        } else {
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(-90F));
            matrixStack.translate(0D, -1D, 2.01D / 16D);
        }

        if (entity.isSkeleton()) {
            DummySkeleton skeleton = skeletons.get(entity, () -> new DummySkeleton(entity.level, entity.getEquipment()));
            getRenderer(skeleton).render(skeleton, entityYaw, 1F, matrixStack, buffer, packedLightIn);
        } else {
            AbstractClientPlayer abstractClientPlayerEntity = players.get(entity, () -> new DummyPlayer((ClientLevel) entity.level, new GameProfile(entity.getCorpseUUID().orElse(new UUID(0L, 0L)), entity.getCorpseName()), entity.getEquipment(), entity.getCorpseModel()));
            getRenderer(abstractClientPlayerEntity).render(abstractClientPlayerEntity, 0F, 1F, matrixStack, buffer, packedLightIn);
        }
        matrixStack.popPose();
    }

    private <T extends Entity> EntityRenderer<? super T> getRenderer(T entity) {
        return MC.getEntityRenderDispatcher().getRenderer(entity);
    }

}
