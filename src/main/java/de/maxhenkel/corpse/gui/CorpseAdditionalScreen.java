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

        xSize = 176;
        ySize = 248;
    }

    @Override
    protected void init() {
        super.init();

        buttons.clear();
        int left = (width - xSize) / 2;
        int buttonWidth = 50;
        int buttonHeight = 20;
        previous = addButton(new Button(left + PADDING, guiTop + 149 - buttonHeight, buttonWidth, buttonHeight, new TranslationTextComponent("button.corpse.previous"), button -> {
            page--;
            if (page < 0) {
                page = 0;
            }
            Main.SIMPLE_CHANNEL.sendToServer(new MessageSwitchInventoryPage(page));
        }));
        next = addButton(new Button(left + xSize - buttonWidth - PADDING, guiTop + 149 - buttonHeight, buttonWidth, buttonHeight, new TranslationTextComponent("button.corpse.next"), button -> {
            page++;
            if (page >= getPages()) {
                page = getPages() - 1;
            }
            Main.SIMPLE_CHANNEL.sendToServer(new MessageSwitchInventoryPage(page));
        }));

        addButton(new TransferItemsButton(left + xSize - TransferItemsButton.WIDTH - 9, guiTop + 5, button -> {
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

        font.func_243248_b(matrixStack, corpse.getDisplayName(), guiLeft + 7, guiTop + 7, FONT_COLOR);
        font.func_243248_b(matrixStack, playerInventory.getDisplayName(), guiLeft + 7, guiTop + ySize - 96 + 2, FONT_COLOR);

        TranslationTextComponent pageName = new TranslationTextComponent("gui.corpse.page", page + 1, getPages());
        int pageWidth = font.getStringWidth(pageName.getString());
        font.func_243248_b(matrixStack, pageName, guiLeft + xSize / 2 - pageWidth / 2, guiTop + ySize - 113, FONT_COLOR);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);

        if (mouseX >= guiLeft + xSize - TransferItemsButton.WIDTH - 9 && mouseX < guiLeft + xSize - 9 && mouseY >= guiTop + 5 && mouseY < guiTop + 5 + TransferItemsButton.HEIGHT) {
            renderTooltip(matrixStack, Collections.singletonList(new TranslationTextComponent("button.corpse.transfer_items").func_241878_f()), mouseX - guiLeft, mouseY - guiTop);
        }
    }
}
