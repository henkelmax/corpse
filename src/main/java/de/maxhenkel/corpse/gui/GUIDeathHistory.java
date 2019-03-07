package de.maxhenkel.corpse.gui;

import com.mojang.authlib.GameProfile;
import de.maxhenkel.corpse.Death;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.net.MessageShowCorpseInventory;
import de.maxhenkel.corpse.net.MessageTeleport;
import de.maxhenkel.corpse.proxy.CommonProxy;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GUIDeathHistory extends GUIBase {

    private static final ResourceLocation DEATH_HISTORY_GUI_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/gui_death_history.png");

    private GuiButton previous;
    private GuiButton next;

    private List<Death> deaths;
    private int index;
    private SimpleDateFormat dateFormat;
    private int hSplit;

    public GUIDeathHistory(List<Death> deaths) {
        super(DEATH_HISTORY_GUI_TEXTURE, new ContainerDeathHistory());
        this.deaths = deaths;
        this.dateFormat = new SimpleDateFormat(new TextComponentTranslation("gui.death_history.date_format").getUnformattedComponentText());
        this.index = 0;

        xSize = 248;
        ySize = 166;

        hSplit = xSize / 2;
    }

    @Override
    public void initGui() {
        super.initGui();

        buttonList.clear();
        int padding = 7;
        int buttonWidth = 50;
        int buttonHeight = 20;
        previous = addButton(new GuiButton(0, guiLeft + padding, guiTop + ySize - buttonHeight - padding, buttonWidth, buttonHeight, new TextComponentTranslation("button.previous").getFormattedText()));

        addButton(new GuiButton(2, guiLeft + (xSize - buttonWidth) / 2, guiTop + ySize - buttonHeight - padding, buttonWidth, buttonHeight, new TextComponentTranslation("button.show_items").getFormattedText()));

        next = addButton(new GuiButton(1, guiLeft + xSize - buttonWidth - padding, guiTop + ySize - buttonHeight - padding, buttonWidth, buttonHeight, new TextComponentTranslation("button.next").getFormattedText()));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button.id == 0) {
            index--;
            if (index < 0) {
                index = 0;
            }
        } else if (button.id == 1) {
            index++;
            if (index >= deaths.size()) {
                index = deaths.size() - 1;
            }
        } else if (button.id == 2) {
            CommonProxy.simpleNetworkWrapper.sendToServer(new MessageShowCorpseInventory(getCurrentDeath().getPlayerUUID(), getCurrentDeath().getId()));
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseX >= guiLeft + 7 && mouseX <= guiLeft + hSplit && mouseY >= guiTop + 70 && mouseY <= guiTop + 100 + fontRenderer.FONT_HEIGHT) {
            /*BlockPos pos = getCurrentDeath().getBlockPos();
            ITextComponent teleport = TextComponentUtils.wrapInSquareBrackets(new TextComponentTranslation("chat.coordinates", pos.getX(), pos.getY(), pos.getZ())).applyTextStyle((style) -> {
                style.setColor(TextFormatting.GREEN).setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/execute in " + getCurrentDeath().getDimension() + " run tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ())).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("chat.coordinates.tooltip")));
            });
            mc.player.sendMessage(new TextComponentTranslation("chat.teleport_death_location", teleport));
            mc.getSoundHandler().play(SimpleSound.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            mc.displayGuiScreen(null);*/

            CommonProxy.simpleNetworkWrapper.sendToServer(new MessageTeleport(getCurrentDeath()));
        }
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        Death death = getCurrentDeath();

        // Title
        String title = new TextComponentTranslation("gui.death_history.title").getFormattedText();
        int titleWidth = fontRenderer.getStringWidth(title);
        fontRenderer.drawString(TextFormatting.BLACK + title, guiLeft + (xSize - titleWidth) / 2, guiTop + 7, 0);

        // Date
        String date = dateFormat.format(new Date(death.getTimestamp()));
        int dateWidth = fontRenderer.getStringWidth(date);
        fontRenderer.drawString(TextFormatting.DARK_GRAY + date, guiLeft + (xSize - dateWidth) / 2, guiTop + 20, 0);

        // Name
        String textName = new TextComponentTranslation("gui.death_history.name").getFormattedText() + ":";
        drawLeft(TextFormatting.DARK_GRAY + textName, guiTop + 40);

        String name = death.getPlayerName();
        drawRight(TextFormatting.GRAY + name, guiTop + 40);

        // Dimension
        String textDimension = new TextComponentTranslation("gui.death_history.dimension").getFormattedText() + ":";
        drawLeft(TextFormatting.DARK_GRAY + textDimension, guiTop + 55);

        String dimension = "Dim " + death.getDimension();
        drawRight(TextFormatting.GRAY + dimension, guiTop + 55);

        // Location
        String textLocation = new TextComponentTranslation("gui.death_history.location").getFormattedText() + ":";
        drawLeft(TextFormatting.DARK_GRAY + textLocation, guiTop + 70);

        drawRight(TextFormatting.GRAY + "" + Math.round(death.getPosX()) + " X", guiTop + 70);
        drawRight(TextFormatting.GRAY + "" + Math.round(death.getPosY()) + " Y", guiTop + 85);
        drawRight(TextFormatting.GRAY + "" + Math.round(death.getPosZ()) + " Z", guiTop + 100);

        // Player
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        EntityPlayer player = new EntityOtherPlayerMP(mc.world, new GameProfile(death.getPlayerUUID(), death.getPlayerName()));
        player.height = Float.MAX_VALUE;
        GuiInventory.drawEntityOnScreen(guiLeft + xSize - (xSize - hSplit) / 2, guiTop + ySize / 2 + 30, 40, (guiLeft + xSize - (xSize - hSplit) / 2) - mouseX, (guiTop + ySize / 2) - mouseY, player);

        if (mouseX >= guiLeft + 7 && mouseX <= guiLeft + hSplit && mouseY >= guiTop + 70 && mouseY <= guiTop + 100 + fontRenderer.FONT_HEIGHT) {
            drawHoveringText(new TextComponentTranslation("tooltip.teleport").getFormattedText(), mouseX, mouseY);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (index <= 0) {
            previous.enabled = false;
        } else {
            previous.enabled = true;
        }

        if (index >= deaths.size() - 1) {
            next.enabled = false;
        } else {
            next.enabled = true;
        }
    }

    public void drawLeft(String string, int height) {
        int offset = 7;
        int offsetLeft = guiLeft + offset;
        fontRenderer.drawString(string, offsetLeft, height, 0);
    }

    public void drawRight(String string, int height) {
        int strWidth = fontRenderer.getStringWidth(string);
        fontRenderer.drawString(string, guiLeft + hSplit - strWidth, height, 0);
    }

    public Death getCurrentDeath() {
        return deaths.get(index);
    }
}
