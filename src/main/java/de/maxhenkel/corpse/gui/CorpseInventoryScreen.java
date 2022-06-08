package de.maxhenkel.corpse.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.corelib.inventory.ScreenBase;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.net.MessageOpenAdditionalItems;
import de.maxhenkel.corpse.net.MessageTransferItems;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CorpseInventoryScreen extends ScreenBase<CorpseInventoryContainer> {

    public static final ResourceLocation CORPSE_GUI_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/inventory_corpse.png");

    public static final MutableComponent TRANSFER_ITEMS = Component.translatable("button.corpse.transfer_items");
    public static final MutableComponent ADDITIONAL_ITEMS = Component.translatable("button.corpse.additional_items");

    public static final Button.OnPress PRESS_TRANSFER_ITEMS = (b) -> Main.SIMPLE_CHANNEL.sendToServer(new MessageTransferItems());
    public static final Button.OnPress PRESS_ADDITIONAL_ITEMS = (b) -> Main.SIMPLE_CHANNEL.sendToServer(new MessageOpenAdditionalItems());

    private static final int PADDING = 7;
    private static final int BUTTON_HEIGHT = 20;

    private Inventory playerInventory;
    private CorpseEntity corpse;

    private Button takeItems;
    private Button additionalItems;

    public CorpseInventoryScreen(CorpseEntity corpse, Inventory playerInventory, CorpseInventoryContainer container, Component title) {
        super(CORPSE_GUI_TEXTURE, container, playerInventory, title);
        this.playerInventory = playerInventory;
        this.corpse = corpse;

        imageWidth = 176;
        imageHeight = 245;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateButtons();
    }

    @Override
    public void init() {
        super.init();
        updateButtons();
    }

    protected void updateButtons() {
        clearWidgets();

        CorpseEntity corpse = menu.getCorpse();
        if (!corpse.isMainInventoryEmpty() && !corpse.isAdditionalInventoryEmpty()) {
            takeItems = addLeftButton(TRANSFER_ITEMS, PRESS_TRANSFER_ITEMS);
            additionalItems = addRightButton(ADDITIONAL_ITEMS, PRESS_ADDITIONAL_ITEMS);
        } else if (!corpse.isMainInventoryEmpty()) {
            takeItems = addCenterButton(TRANSFER_ITEMS, PRESS_TRANSFER_ITEMS);
            additionalItems = null;
        } else if (!corpse.isAdditionalInventoryEmpty()) {
            takeItems = null;
            additionalItems = addCenterButton(ADDITIONAL_ITEMS, PRESS_ADDITIONAL_ITEMS);
        }
        if (takeItems != null) {
            takeItems.active = menu.isEditable();
        }
    }

    private Button addLeftButton(Component text, Button.OnPress pressable) {
        return addRenderableWidget(new Button(leftPos + PADDING, topPos + 120, 80, BUTTON_HEIGHT, text, pressable));
    }

    private Button addRightButton(Component text, Button.OnPress pressable) {
        return addRenderableWidget(new Button(leftPos + imageWidth - 80 - PADDING, topPos + 120, 80, BUTTON_HEIGHT, text, pressable));
    }

    private Button addCenterButton(Component text, Button.OnPress pressable) {
        return addRenderableWidget(new Button(leftPos + imageWidth / 2 - 50, topPos + 120, 100, BUTTON_HEIGHT, text, pressable));
    }

    @Override
    public void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
        super.renderLabels(matrixStack, mouseX, mouseY);

        font.draw(matrixStack, corpse.getDisplayName(), 7, 7, FONT_COLOR);
        font.draw(matrixStack, playerInventory.getDisplayName(), 7, imageHeight - 96 + 2, FONT_COLOR);
    }

}
