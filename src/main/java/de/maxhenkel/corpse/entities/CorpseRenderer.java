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

public class CorpseRenderer extends EntityRenderer<CorpseEntity> {

    private final CachedMap<CorpseEntity, DummyPlayer> players;
    private final CachedMap<CorpseEntity, DummySkeleton> skeletons;
    private final DummyPlayerRenderer playerRenderer;
    private final DummyPlayerRenderer playerRendererSmallArms;
    private final SkeletonRenderer skeletonRenderer;

    public CorpseRenderer(EntityRendererManager renderManager) {
        super(renderManager);
        players = new CachedMap<>(10_000L);
        skeletons = new CachedMap<>(10_000L);
        playerRenderer = new DummyPlayerRenderer(renderManager, false);
        playerRendererSmallArms = new DummyPlayerRenderer(renderManager, true);
        skeletonRenderer = new SkeletonRenderer(renderManager);
    }

    @Override
    public ResourceLocation getEntityTexture(CorpseEntity entity) {
        return null;
    }

    @Override
    public void render(CorpseEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLightIn) {
        super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLightIn);
        matrixStack.push();

        matrixStack.rotate(Vector3f.YP.rotationDegrees(-entity.getCorpseRotation()));

        if (Main.SERVER_CONFIG.spawnCorpseOnFace.get()) {
            matrixStack.rotate(Vector3f.XP.rotationDegrees(90F));
            matrixStack.translate(0D, -1D, -2.01D / 16D);
        } else {
            matrixStack.rotate(Vector3f.XP.rotationDegrees(-90F));
            matrixStack.translate(0D, -1D, 2.01D / 16D);
        }

        if (entity.isSkeleton()) {
            DummySkeleton skeleton = skeletons.get(entity, () -> new DummySkeleton(entity.world, entity.getEquipment()));
            skeletonRenderer.render(skeleton, entityYaw, 1F, matrixStack, buffer, packedLightIn);
        } else {
            AbstractClientPlayerEntity abstractClientPlayerEntity = players.get(entity, () -> new DummyPlayer((ClientWorld) entity.world, new GameProfile(entity.getCorpseUUID(), entity.getCorpseName()), entity.getEquipment(), entity.getCorpseModel()));
            if (PlayerSkins.isSlim(entity.getCorpseUUID())) {
                playerRendererSmallArms.render(abstractClientPlayerEntity, 0F, 1F, matrixStack, buffer, packedLightIn);
            } else {
                playerRenderer.render(abstractClientPlayerEntity, 0F, 1F, matrixStack, buffer, packedLightIn);
            }
        }
        matrixStack.pop();
    }

}
