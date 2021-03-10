package de.maxhenkel.corpse.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.corelib.inventory.ScreenBase;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.net.MessageTransferItems;
import de.maxhenkel.corpse.net.MessageSwitchInventoryPage;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collections;

public class CorpseAdditionalScreen extends ScreenBase<CorpseAdditionalContainer> {

    public static final ResourceLocation CORPSE_GUI_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/gui_corpse.png");

    private static final int PADDING = 7;

    private PlayerInventory playerInventory;
    private CorpseEntity corpse;

    private Button previous;
    private Button next;

    private int page;

    public CorpseAdditionalScreen(CorpseEntity corpse, PlayerInventory playerInventory, CorpseAdditionalContainer container, ITextComponent title) {
        super(CORPSE_GUI_TEXTURE, container, playerInventory, title);
        this.playerInventory = playerInventory;
        this.corpse = corpse;
        this.page = 0;

        imageWidth = 176;
        imageHeight = 248;
    }

    @Override
    protected void init() {
        super.init();

        buttons.clear();
        int left = (width - imageWidth) / 2;
        int buttonWidth = 50;
        int buttonHeight = 20;
        previous = addButton(new Button(left + PADDING, topPos + 149 - buttonHeight, buttonWidth, buttonHeight, new TranslationTextComponent("button.corpse.previous"), button -> {
            page--;
            if (page < 0) {
                page = 0;
            }
            Main.SIMPLE_CHANNEL.sendToServer(new MessageSwitchInventoryPage(page));
        }));
        next = addButton(new Button(left + imageWidth - buttonWidth - PADDING, topPos + 149 - buttonHeight, buttonWidth, buttonHeight, new TranslationTextComponent("button.corpse.next"), button -> {
            page++;
            if (page >= getPages()) {
                page = getPages() - 1;
            }
            Main.SIMPLE_CHANNEL.sendToServer(new MessageSwitchInventoryPage(page));
        }));

        addButton(new TransferItemsButton(left + imageWidth - TransferItemsButton.WIDTH - 9, topPos + 5, button -> {
            Main.SIMPLE_CHANNEL.sendToServer(new MessageTransferItems());
        }));
    }

    @Override
    public void tick() {
        super.tick();
        previous.active = page > 0;

        next.active = page < getPages() - 1;
    }

    private int getPages() {
        int size = corpse.getDeath().getAdditionalItems().size();
        return Math.max(1, (size / 54) + ((size % 54 == 0) ? 0 : 1));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        font.draw(matrixStack, corpse.getDisplayName(), leftPos + 7, topPos + 7, FONT_COLOR);
        font.draw(matrixStack, playerInventory.getDisplayName(), leftPos + 7, topPos + imageHeight - 96 + 2, FONT_COLOR);

        TranslationTextComponent pageName = new TranslationTextComponent("gui.corpse.page", page + 1, getPages());
        int pageWidth = font.width(pageName);
        font.draw(matrixStack, pageName, leftPos + imageWidth / 2 - pageWidth / 2, topPos + imageHeight - 113, FONT_COLOR);
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
        super.renderLabels(matrixStack, mouseX, mouseY);

        if (mouseX >= leftPos + imageWidth - TransferItemsButton.WIDTH - 9 && mouseX < leftPos + imageWidth - 9 && mouseY >= topPos + 5 && mouseY < topPos + 5 + TransferItemsButton.HEIGHT) {
            renderTooltip(matrixStack, Collections.singletonList(new TranslationTextComponent("button.corpse.transfer_items").getVisualOrderText()), mouseX - leftPos, mouseY - topPos);
        }
    }
}
