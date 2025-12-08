package de.maxhenkel.corpse.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ScreenBase extends Screen {

    protected Identifier texture;

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
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, guiLeft, guiTop, 0, 0, xSize, ySize, 256, 256);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    protected void drawCenteredString(GuiGraphics guiGraphics, Component text, int x, int y, int color) {
        guiGraphics.drawString(font, text, x - font.width(text) / 2, y, color, false);
    }

}
