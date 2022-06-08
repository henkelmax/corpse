package de.maxhenkel.corpse.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.corelib.inventory.ScreenBase;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.net.MessageTransferItems;
import de.maxhenkel.corpse.net.MessageSwitchInventoryPage;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.Collections;

public class CorpseAdditionalScreen extends ScreenBase<CorpseAdditionalContainer> {

    public static final ResourceLocation CORPSE_GUI_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/gui_corpse.png");

    private static final int PADDING = 7;

    private Inventory playerInventory;
    private CorpseEntity corpse;

    private Button previous;
    private Button next;

    private int page;

    public CorpseAdditionalScreen(CorpseEntity corpse, Inventory playerInventory, CorpseAdditionalContainer container, Component title) {
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

        int left = (width - imageWidth) / 2;
        int buttonWidth = 50;
        int buttonHeight = 20;
        previous = addRenderableWidget(new Button(left + PADDING, topPos + 149 - buttonHeight, buttonWidth, buttonHeight, Component.translatable("button.corpse.previous"), button -> {
            page--;
            if (page < 0) {
                page = 0;
            }
            Main.SIMPLE_CHANNEL.sendToServer(new MessageSwitchInventoryPage(page));
        }));
        next = addRenderableWidget(new Button(left + imageWidth - buttonWidth - PADDING, topPos + 149 - buttonHeight, buttonWidth, buttonHeight, Component.translatable("button.corpse.next"), button -> {
            page++;
            if (page >= getPages()) {
                page = getPages() - 1;
            }
            Main.SIMPLE_CHANNEL.sendToServer(new MessageSwitchInventoryPage(page));
        }));

        addRenderableWidget(new TransferItemsButton(left + imageWidth - TransferItemsButton.WIDTH - 9, topPos + 5, button -> {
            Main.SIMPLE_CHANNEL.sendToServer(new MessageTransferItems());
        }));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        previous.active = page > 0;
        next.active = page < getPages() - 1;
    }

    private int getPages() {
        int size = corpse.getDeath().getAdditionalItems().size();
        return Math.max(1, (size / 54) + ((size % 54 == 0) ? 0 : 1));
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        font.draw(matrixStack, corpse.getDisplayName(), leftPos + 7, topPos + 7, FONT_COLOR);
        font.draw(matrixStack, playerInventory.getDisplayName(), leftPos + 7, topPos + imageHeight - 96 + 2, FONT_COLOR);

        MutableComponent pageName = Component.translatable("gui.corpse.page", page + 1, getPages());
        int pageWidth = font.width(pageName);
        font.draw(matrixStack, pageName, leftPos + imageWidth / 2 - pageWidth / 2, topPos + imageHeight - 113, FONT_COLOR);
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
        super.renderLabels(matrixStack, mouseX, mouseY);

        if (mouseX >= leftPos + imageWidth - TransferItemsButton.WIDTH - 9 && mouseX < leftPos + imageWidth - 9 && mouseY >= topPos + 5 && mouseY < topPos + 5 + TransferItemsButton.HEIGHT) {
            renderTooltip(matrixStack, Collections.singletonList(Component.translatable("button.corpse.transfer_items").getVisualOrderText()), mouseX - leftPos, mouseY - topPos);
        }
    }
}
