package de.maxhenkel.corpse.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.corelib.client.PlayerSkins;
import de.maxhenkel.corpse.Main;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.entity.model.SkeletonModel;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class CorpseRenderer extends EntityRenderer<CorpseEntity> {

    private static final ResourceLocation SKELETON_TEXTURE = new ResourceLocation("textures/entity/skeleton/skeleton.png");

    private PlayerModel modelPlayer;
    private PlayerModel modelPlayerSlim;
    private SkeletonModel modelSkeleton;

    public CorpseRenderer(EntityRendererManager renderManager) {
        super(renderManager);
        modelPlayer = new PlayerModel(0F, false);
        modelPlayerSlim = new PlayerModel(0F, true);
        modelSkeleton = new SkeletonModel() {
            @Override
            public void setRotationAngles(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

            }
        };
        modelPlayer.isChild = false;
        modelPlayerSlim.isChild = false;
        modelSkeleton.isChild = false;
    }

    @Override
    public ResourceLocation getEntityTexture(CorpseEntity entity) {
        if (entity.isSkeleton()) {
            return SKELETON_TEXTURE;
        } else {
            return PlayerSkins.getSkin(entity.getCorpseUUID(), entity.getCorpseName());
        }
    }

    @Override
    public void render(CorpseEntity entity, float f1, float f2, MatrixStack matrixStack, IRenderTypeBuffer buffer, int i) {
        super.render(entity, f1, f2, matrixStack, buffer, i);
        matrixStack.push();

        matrixStack.rotate(Vector3f.YP.rotationDegrees(360F - entity.getCorpseRotation()));

        if (Main.SERVER_CONFIG.spawnCorpseOnFace.get()) {
            matrixStack.rotate(Vector3f.XP.rotationDegrees(-90F));
            matrixStack.translate(0D, -0.5D, 2.01D / 16D);
        } else {
            matrixStack.rotate(Vector3f.XP.rotationDegrees(90F));
            matrixStack.translate(0D, -0.5D, -2.01D / 16D);
        }

        if (entity.isSkeleton()) {
            modelSkeleton.render(matrixStack, buffer.getBuffer(modelSkeleton.getRenderType(getEntityTexture(entity))), i, 0xFFFFFF, 1F, 1F, 1F, 1F);
        } else {
            if (PlayerSkins.isSlim(entity.getCorpseUUID())) {
                modelPlayerSlim.render(matrixStack, buffer.getBuffer(modelPlayerSlim.getRenderType(getEntityTexture(entity))), i, 0xFFFFFF, 1F, 1F, 1F, 1F);
            } else {
                modelPlayer.render(matrixStack, buffer.getBuffer(modelPlayer.getRenderType(getEntityTexture(entity))), i, 0xFFFFFF, 1F, 1F, 1F, 1F);
            }
        }
        matrixStack.pop();
    }

}
