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
        super(x, y, WIDTH, HEIGHT, StringTextComponent.field_240750_d_, pressable);
    }

    public void func_230431_b_(MatrixStack matrixStack, int x, int y, float f) {
        RenderSystem.color4f(1F, 1F, 1F, 1F);
        Minecraft.getInstance().getTextureManager().bindTexture(CorpseAdditionalScreen.CORPSE_GUI_TEXTURE);
        if (field_230692_n_) {
            func_238474_b_(matrixStack, field_230690_l_, field_230691_m_, 176, 10, WIDTH, HEIGHT);
        } else {
            func_238474_b_(matrixStack, field_230690_l_, field_230691_m_, 176, 0, WIDTH, HEIGHT);
        }
    }

}
