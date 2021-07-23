package de.maxhenkel.corpse.entities;


import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;

public class DummyPlayerRenderer extends PlayerRenderer {

    public DummyPlayerRenderer(EntityRendererProvider.Context renderManager, boolean useSmallArms) {
        super(renderManager, useSmallArms);
    }

    @Override
    protected void renderNameTag(AbstractClientPlayer player, Component component, PoseStack stack, MultiBufferSource bufferSource, int light) {

    }

}
