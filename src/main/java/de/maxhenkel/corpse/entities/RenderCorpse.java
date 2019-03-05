package de.maxhenkel.corpse.entities;

import de.maxhenkel.corpse.PlayerSkins;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.model.ModelPlayer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.UUID;

public class RenderCorpse extends Render<EntityCorpse> {

    private Minecraft mc;
    private ModelPlayer modelPlayer;
    private ModelPlayer modelPlayerSlim;

    public RenderCorpse(RenderManager renderManager) {
        super(renderManager);
        mc = Minecraft.getInstance();
        modelPlayer = new ModelPlayer(0F, false);
        modelPlayerSlim = new ModelPlayer(0F, true);
        modelPlayer.isChild = false;
        modelPlayerSlim.isChild = false;
    }

    @Override
    public void doRender(EntityCorpse entity, double x, double y, double z, float entityYaw, float partialTicks) {

        GlStateManager.pushMatrix();
        GlStateManager.translated(x, y, z);
        GlStateManager.rotatef(360F - entity.getCorpseRotation(), 0F, 1F, 0F);
        GlStateManager.rotatef(90, 1F, 0F, 0F);
        GlStateManager.translated(0D, -0.5D, -2D / 16D);

        bindTexture(getEntityTexture(entity));

        if (isSlim(entity.getCorpseUUID())) {
            modelPlayerSlim.render(entity, 0F, 0F, 0F, 0F, 0F, 0.0625F);
        } else {
            modelPlayer.render(entity, 0F, 0F, 0F, 0F, 0F, 0.0625F);
        }
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityCorpse entity) {
        return PlayerSkins.getSkin(entity.getCorpseUUID(), entity.getCorpseName());
    }

    public boolean isSlim(UUID uuid) {
        NetworkPlayerInfo networkplayerinfo = mc.getConnection().getPlayerInfo(uuid);
        return networkplayerinfo == null ? (uuid.hashCode() & 1) == 1 : networkplayerinfo.getSkinType().equals("slim");
    }

}
