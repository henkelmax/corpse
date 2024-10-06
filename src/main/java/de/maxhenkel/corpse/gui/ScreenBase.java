package de.maxhenkel.corpse.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ScreenBase extends Screen {

    public static final int FONT_COLOR = 4210752;

    protected ResourceLocation texture;

    protected int guiLeft;
    protected int guiTop;

    protected int xSize;
    protected int ySize;

    protected ScreenBase(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();
        guiLeft = (width - xSize) / 2;
        guiTop = (height - ySize) / 2;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        guiGraphics.blit(texture, guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    protected void drawCenteredString(GuiGraphics guiGraphics, Component text, int x, int y, int color) {
        guiGraphics.drawString(font, text, x - font.width(text) / 2, y, color, false);
    }

}