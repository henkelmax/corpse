package de.maxhenkel.corpse.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.corelib.inventory.ScreenBase;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.net.MessageOpenAdditionalItems;
import de.maxhenkel.corpse.net.MessageTransferItems;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class CorpseInventoryScreen extends ScreenBase<CorpseInventoryContainer> {

    public static final ResourceLocation CORPSE_GUI_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/inventory_corpse.png");

    public static final TranslationTextComponent TRANSFER_ITEMS = new TranslationTextComponent("button.corpse.transfer_items");
    public static final TranslationTextComponent ADDITIONAL_ITEMS = new TranslationTextComponent("button.corpse.additional_items");

    public static final Button.IPressable PRESS_TRANSFER_ITEMS = (b) -> Main.SIMPLE_CHANNEL.sendToServer(new MessageTransferItems());
    public static final Button.IPressable PRESS_ADDITIONAL_ITEMS = (b) -> Main.SIMPLE_CHANNEL.sendToServer(new MessageOpenAdditionalItems());

    private static final int PADDING = 7;
    private static final int BUTTON_HEIGHT = 20;

    private PlayerInventory playerInventory;
    private CorpseEntity corpse;

    private Button takeItems;
    private Button additionalItems;

    public CorpseInventoryScreen(CorpseEntity corpse, PlayerInventory playerInventory, CorpseInventoryContainer container, ITextComponent title) {
        super(CORPSE_GUI_TEXTURE, container, playerInventory, title);
        this.playerInventory = playerInventory;
        this.corpse = corpse;

        xSize = 176;
        ySize = 245;
    }

    @Override
    protected void func_231160_c_() {
        super.func_231160_c_();

        updateButtons();
    }

    protected void updateButtons() {
        field_230710_m_.clear();

        takeItems = addLeftButton(TRANSFER_ITEMS, PRESS_TRANSFER_ITEMS);
        additionalItems = addRightButton(ADDITIONAL_ITEMS, PRESS_ADDITIONAL_ITEMS);
        takeItems.field_230693_o_ = container.isEditable();

        /*CorpseEntity corpse = container.getCorpse();
        if (!corpse.isMainInventoryEmpty() && !corpse.isAdditionalInventoryEmpty()) {
            takeItems = addLeftButton(TRANSFER_ITEMS, PRESS_TRANSFER_ITEMS);
            additionalItems = addRightButton(ADDITIONAL_ITEMS, PRESS_ADDITIONAL_ITEMS);
        } else if (!corpse.isMainInventoryEmpty()) {
            takeItems = addCenterButton(TRANSFER_ITEMS, PRESS_TRANSFER_ITEMS);
        } else if (!corpse.isAdditionalInventoryEmpty()) {
            additionalItems = addCenterButton(ADDITIONAL_ITEMS, PRESS_ADDITIONAL_ITEMS);
        }*/
    }

    /*@Override
    public void func_231023_e_() {
        super.func_231023_e_();
        takeItems.field_230693_o_ = container.isEditable() && !corpse.isMainInventoryEmpty();
        additionalItems.field_230693_o_ = !corpse.isAdditionalInventoryEmpty();
    }*/

    private Button addLeftButton(ITextComponent text, Button.IPressable pressable) {
        return func_230480_a_(new Button(guiLeft + PADDING, guiTop + 120, 80, BUTTON_HEIGHT, text, pressable));
    }

    private Button addRightButton(ITextComponent text, Button.IPressable pressable) {
        return func_230480_a_(new Button(guiLeft + xSize - 80 - PADDING, guiTop + 120, 80, BUTTON_HEIGHT, text, pressable));
    }

    private Button addCenterButton(ITextComponent text, Button.IPressable pressable) {
        return func_230480_a_(new Button(guiLeft + xSize / 2 - 50, guiTop + 120, 100, BUTTON_HEIGHT, text, pressable));
    }

    @Override
    public void func_230430_a_(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);

        field_230712_o_.func_238421_b_(matrixStack, corpse.getDisplayName().getString(), guiLeft + 7, guiTop + 7, FONT_COLOR);
        field_230712_o_.func_238421_b_(matrixStack, playerInventory.getDisplayName().getString(), guiLeft + 7, guiTop + ySize - 96 + 2, FONT_COLOR);
    }

}
