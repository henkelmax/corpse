package de.maxhenkel.corpse.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
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
            public void func_225597_a_(Entity entity, float f1, float f2, float f3, float f4, float f5) {

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
    public void func_225623_a_(CorpseEntity entity, float f1, float f2, MatrixStack matrixStack, IRenderTypeBuffer buffer, int i) {
        super.func_225623_a_(entity, f1, f2, matrixStack, buffer, i);
        matrixStack.func_227860_a_();

        matrixStack.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(360F - entity.getCorpseRotation()));

        if (Config.SERVER.spawnCorpseOnFace.get()) {
            matrixStack.func_227863_a_(Vector3f.field_229179_b_.func_229187_a_(-90));
            matrixStack.func_227861_a_(0D, -0.5D, 2.01D / 16D);
        } else {
            matrixStack.func_227863_a_(Vector3f.field_229179_b_.func_229187_a_(90));
            matrixStack.func_227861_a_(0D, -0.5D, -2.01D / 16D);
        }

        if (entity.isSkeleton()) {
            modelSkeleton.func_225598_a_(matrixStack, buffer.getBuffer(modelSkeleton.func_228282_a_(getEntityTexture(entity))), i, 0xFFFFFF, 1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            if (isSlim(entity.getCorpseUUID())) {
                modelPlayerSlim.func_225598_a_(matrixStack, buffer.getBuffer(modelPlayerSlim.func_228282_a_(getEntityTexture(entity))), i, 0xFFFFFF, 1.0F, 1.0F, 1.0F, 1.0F);
            } else {
                modelPlayer.func_225598_a_(matrixStack, buffer.getBuffer(modelPlayer.func_228282_a_(getEntityTexture(entity))), i, 0xFFFFFF, 1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
        matrixStack.func_227865_b_();

    }

    public boolean isSlim(UUID uuid) {
        NetworkPlayerInfo networkplayerinfo = mc.getConnection().getPlayerInfo(uuid);
        return networkplayerinfo == null ? (uuid.hashCode() & 1) == 1 : networkplayerinfo.getSkinType().equals("slim");
    }

}
