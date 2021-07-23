package de.maxhenkel.corpse.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;

public class TransferItemsButton extends Button {

    public static final int WIDTH = 18;
    public static final int HEIGHT = 10;

    public TransferItemsButton(int x, int y, Button.OnPress pressable) {
        super(x, y, WIDTH, HEIGHT, TextComponent.EMPTY, pressable);
    }

    @Override
    public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, CorpseAdditionalScreen.CORPSE_GUI_TEXTURE);
        if (isHovered) {
            blit(matrixStack, x, y, 176, 10, WIDTH, HEIGHT);
        } else {
            blit(matrixStack, x, y, 176, 0, WIDTH, HEIGHT);
        }
    }
}
