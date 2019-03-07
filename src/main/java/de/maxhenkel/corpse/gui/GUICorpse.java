package de.maxhenkel.corpse.gui;

import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.entities.EntityCorpse;
import de.maxhenkel.corpse.net.MessageSwitchInventoryPage;
import de.maxhenkel.corpse.proxy.CommonProxy;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

import java.io.IOException;

public class GUICorpse extends GUIBase {

    private static final ResourceLocation CORPSE_GUI_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/gui_corpse.png");

    private IInventory playerInventory;
    private EntityCorpse corpse;

    private GuiButton previous;
    private GuiButton next;

    private int page;

    public GUICorpse(IInventory playerInventory, EntityCorpse corpse, boolean editable) {
        super(CORPSE_GUI_TEXTURE, new ContainerCorpse(playerInventory, corpse, editable));
        this.playerInventory = playerInventory;
        this.corpse = corpse;
        this.page = 0;

        xSize = 176;
        ySize = 248;
    }

    @Override
    public void initGui() {
        super.initGui();

        buttonList.clear();
        int left = (width - xSize) / 2;
        int padding = 7;
        int buttonWidth = 50;
        int buttonHeight = 20;
        previous = addButton(new GuiButton(0, left + padding, guiTop + 149 - buttonHeight, buttonWidth, buttonHeight, new TextComponentTranslation("button.previous").getFormattedText()));
        next = addButton(new GuiButton(1, left + xSize - buttonWidth - padding, guiTop + 149 - buttonHeight, buttonWidth, buttonHeight, new TextComponentTranslation("button.next").getFormattedText()));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        if (button.id == 0) {
            page--;
            if (page < 0) {
                page = 0;
            }
            CommonProxy.simpleNetworkWrapper.sendToServer(new MessageSwitchInventoryPage(page));
        } else if (button.id == 1) {
            page++;
            if (page >= getPages()) {
                page = getPages() - 1;
            }
            CommonProxy.simpleNetworkWrapper.sendToServer(new MessageSwitchInventoryPage(page));
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (page <= 0) {
            previous.enabled = false;
        } else {
            previous.enabled = true;
        }

        if (page >= getPages() - 1) {
            next.enabled = false;
        } else {
            next.enabled = true;
        }
    }

    private int getPages() {
        return corpse.getSizeInventory() / 54;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        fontRenderer.drawString(corpse.getDisplayName().getFormattedText(), 7, 7, FONT_COLOR);
        fontRenderer.drawString(playerInventory.getDisplayName().getFormattedText(), 7, ySize - 96 + 2, FONT_COLOR);

        String pageName = new TextComponentTranslation("gui.corpse.page", page + 1, getPages()).getFormattedText();
        int pageWidth = fontRenderer.getStringWidth(pageName);
        fontRenderer.drawString(pageName, xSize / 2 - pageWidth / 2, ySize - 113, FONT_COLOR);
    }


}
