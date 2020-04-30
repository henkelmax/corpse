package de.maxhenkel.corpse.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import de.maxhenkel.corpse.Config;
import de.maxhenkel.corpse.PlayerSkins;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.entity.model.SkeletonModel;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public class CorpseRenderer extends EntityRenderer<CorpseEntity> {

    private static final ResourceLocation SKELETON_TEXTURE = new ResourceLocation("textures/entity/skeleton/skeleton.png");

    private Minecraft mc;
    private PlayerModel modelPlayer;
    private PlayerModel modelPlayerSlim;
    private SkeletonModel modelSkeleton;

    public CorpseRenderer(EntityRendererManager renderManager) {
        super(renderManager);
        mc = Minecraft.getInstance();
        modelPlayer = new PlayerModel(0F, false);
        modelPlayerSlim = new PlayerModel(0F, true);
        modelSkeleton = new SkeletonModel() {
            // setRotationAngles
            @Override
            public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {

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

        if (Config.SERVER.spawnCorpseOnFace.get()) {
            matrixStack.rotate(Vector3f.XP.rotationDegrees(-90F));
            matrixStack.translate(0D, -0.5D, 2.01D / 16D);
        } else {
            matrixStack.rotate(Vector3f.XP.rotationDegrees(90F));
            matrixStack.translate(0D, -0.5D, -2.01D / 16D);
        }

        if (entity.isSkeleton()) {
            modelSkeleton.render(matrixStack, buffer.getBuffer(modelSkeleton.getRenderType(getEntityTexture(entity))), i, 0xFFFFFF, 1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            if (isSlim(entity.getCorpseUUID())) {
                modelPlayerSlim.render(matrixStack, buffer.getBuffer(modelPlayerSlim.getRenderType(getEntityTexture(entity))), i, 0xFFFFFF, 1.0F, 1.0F, 1.0F, 1.0F);
            } else {
                modelPlayer.render(matrixStack, buffer.getBuffer(modelPlayer.getRenderType(getEntityTexture(entity))), i, 0xFFFFFF, 1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
        matrixStack.pop();

    }

    public boolean isSlim(UUID uuid) {
        NetworkPlayerInfo networkplayerinfo = mc.getConnection().getPlayerInfo(uuid);
        return networkplayerinfo == null ? (uuid.hashCode() & 1) == 1 : networkplayerinfo.getSkinType().equals("slim");
    }

}
