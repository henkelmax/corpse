package de.maxhenkel.corpse.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

public class TransferItemsButton extends Button {

    public static final int WIDTH = 18;
    public static final int HEIGHT = 10;

    public TransferItemsButton(int x, int y, Button.IPressable pressable) {
        super(x, y, WIDTH, HEIGHT, StringTextComponent.EMPTY, pressable);
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.color4f(1F, 1F, 1F, 1F);
        Minecraft.getInstance().getTextureManager().bind(CorpseAdditionalScreen.CORPSE_GUI_TEXTURE);
        if (isHovered) {
            blit(matrixStack, x, y, 176, 10, WIDTH, HEIGHT);
        } else {
            blit(matrixStack, x, y, 176, 0, WIDTH, HEIGHT);
        }
    }
}
