package de.maxhenkel.corpse.entities;

import com.mojang.blaze3d.platform.GlStateManager;
import de.maxhenkel.corpse.Config;
import de.maxhenkel.corpse.PlayerSkins;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.entity.model.SkeletonModel;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.UUID;

public class CorpseRenderer extends EntityRenderer<CorpseEntity> {

    private static final ResourceLocation SKELETON_TEXTURE = new ResourceLocation("textures/entity/skeleton/skeleton.png");

    private Minecraft mc;
    private PlayerModel modelPlayer;
    private PlayerModel modelPlayerSlim;
    private SkeletonModel modelSkeleton;
    private FakeMobEntity fakeMobEntity;

    public CorpseRenderer(EntityRendererManager renderManager) {
        super(renderManager);
        mc = Minecraft.getInstance();
        modelPlayer = new PlayerModel(0F, false);
        modelPlayerSlim = new PlayerModel(0F, true);
        modelSkeleton = new SkeletonModel() {
            @Override
            public void setRotationAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {

            }
        };
        fakeMobEntity = new FakeMobEntity();
        modelPlayer.isChild = false;
        modelPlayerSlim.isChild = false;
        modelSkeleton.isChild = false;
    }

    @Override
    public void doRender(CorpseEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {

        GlStateManager.pushMatrix();
        GlStateManager.translated(x, y, z);
        GlStateManager.rotatef(360F - entity.getCorpseRotation(), 0F, 1F, 0F);
        if (Config.SERVER.spawnCorpseOnFace.get()) {
            GlStateManager.rotatef(-90, 1F, 0F, 0F);
            GlStateManager.translated(0D, -0.5D, 2D / 16D);
        } else {
            GlStateManager.rotatef(90, 1F, 0F, 0F);
            GlStateManager.translated(0D, -0.5D, -2D / 16D);
        }

        fakeMobEntity.setPosition(x, y, z);
        if (entity.getCorpseAge() >= Config.SERVER.corpseSkeletonTime.get()) {
            bindTexture(SKELETON_TEXTURE);
            modelSkeleton.render(fakeMobEntity, 0F, 0F, 0F, 0F, 0F, 0.0625F);
        } else {
            bindTexture(getEntityTexture(entity));
            if (isSlim(entity.getCorpseUUID())) {
                modelPlayerSlim.render(fakeMobEntity, 0F, 0F, 0F, 0F, 0F, 0.0625F);
            } else {
                modelPlayer.render(fakeMobEntity, 0F, 0F, 0F, 0F, 0F, 0.0625F);
            }
        }
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(CorpseEntity entity) {
        return PlayerSkins.getSkin(entity.getCorpseUUID(), entity.getCorpseName());
    }

    public boolean isSlim(UUID uuid) {
        NetworkPlayerInfo networkplayerinfo = mc.getConnection().getPlayerInfo(uuid);
        return networkplayerinfo == null ? (uuid.hashCode() & 1) == 1 : networkplayerinfo.getSkinType().equals("slim");
    }

}
