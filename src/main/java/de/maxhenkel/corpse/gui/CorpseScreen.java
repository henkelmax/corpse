package de.maxhenkel.corpse.gui;

import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.net.MessageSwitchInventoryPage;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class CorpseScreen extends ScreenBase<CorpseContainer> {

    private static final ResourceLocation CORPSE_GUI_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/gui_corpse.png");

    private PlayerInventory playerInventory;
    private CorpseEntity corpse;

    private Button previous;
    private Button next;

    private int page;

    public CorpseScreen(CorpseEntity corpse, PlayerInventory playerInventory, CorpseContainer container, ITextComponent title) {
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
        int padding = 7;
        int buttonWidth = 50;
        int buttonHeight = 20;
        previous = addButton(new Button(left + padding, guiTop + 149 - buttonHeight, buttonWidth, buttonHeight, new TranslationTextComponent("button.previous").getFormattedText(), button -> {
            page--;
            if (page < 0) {
                page = 0;
            }
            Main.SIMPLE_CHANNEL.sendToServer(new MessageSwitchInventoryPage(page));
        }));
        next = addButton(new Button(left + xSize - buttonWidth - padding, guiTop + 149 - buttonHeight, buttonWidth, buttonHeight, new TranslationTextComponent("button.next").getFormattedText(), button -> {
            page++;
            if (page >= getPages()) {
                page = getPages() - 1;
            }
            Main.SIMPLE_CHANNEL.sendToServer(new MessageSwitchInventoryPage(page));
        }));
    }

    @Override
    public void tick() {
        super.tick();
        if (page <= 0) {
            previous.active = false;
        } else {
            previous.active = true;
        }

        if (page >= getPages() - 1) {
            next.active = false;
        } else {
            next.active = true;
        }
    }

    private int getPages() {
        return corpse.getSizeInventory() / 54;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        font.drawString(corpse.getDisplayName().getFormattedText(), 7, 7, FONT_COLOR);
        font.drawString(playerInventory.getDisplayName().getFormattedText(), 7, ySize - 96 + 2, FONT_COLOR);

        String pageName = new TranslationTextComponent("gui.corpse.page", page + 1, getPages()).getFormattedText();
        int pageWidth = font.getStringWidth(pageName);
        font.drawString(pageName, xSize / 2 - pageWidth / 2, ySize - 113, FONT_COLOR);
    }
}
