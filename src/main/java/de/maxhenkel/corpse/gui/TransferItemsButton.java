package de.maxhenkel.corpse.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class TransferItemsButton extends AbstractButton {

    public static final int WIDTH = 18;
    public static final int HEIGHT = 10;

    private Consumer<TransferItemsButton> onPress;

    public TransferItemsButton(int x, int y, Consumer<TransferItemsButton> onPress) {
        super(x, y, WIDTH, HEIGHT, Component.empty());
        this.onPress = onPress;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        if (isHovered) {
            guiGraphics.blit(CorpseAdditionalScreen.CORPSE_GUI_TEXTURE, getX(), getY(), 176, 10, WIDTH, HEIGHT);
        } else {
            guiGraphics.blit(CorpseAdditionalScreen.CORPSE_GUI_TEXTURE, getX(), getY(), 176, 0, WIDTH, HEIGHT);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }

    @Override
    public void onPress() {
        onPress.accept(this);
    }
}
