package de.maxhenkel.corpse.gui;


import com.mojang.authlib.GameProfile;
import de.maxhenkel.corpse.Death;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.net.MessageShowCorpseInventory;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

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
    protected void initGui() {
        super.initGui();

        buttons.clear();
        int padding = 7;
        int buttonWidth = 50;
        int buttonHeight = 20;
        previous = addButton(new GuiButton(0, guiLeft + padding, guiTop + ySize - buttonHeight - padding, buttonWidth, buttonHeight, new TextComponentTranslation("button.previous").getFormattedText()) {
            @Override
            public void onClick(double x, double y) {
                super.onClick(x, y);
                index--;
                if (index < 0) {
                    index = 0;
                }
            }
        });

        addButton(new GuiButton(0, guiLeft + (xSize - buttonWidth) / 2, guiTop + ySize - buttonHeight - padding, buttonWidth, buttonHeight, new TextComponentTranslation("button.show_items").getFormattedText()) {
            @Override
            public void onClick(double x, double y) {
                super.onClick(x, y);
                Main.SIMPLE_CHANNEL.sendToServer(new MessageShowCorpseInventory(getCurrentDeath().getId()));
            }
        });

        next = addButton(new GuiButton(1, guiLeft + xSize - buttonWidth - padding, guiTop + ySize - buttonHeight - padding, buttonWidth, buttonHeight, new TextComponentTranslation("button.next").getFormattedText()) {
            @Override
            public void onClick(double x, double y) {
                super.onClick(x, y);
                index++;
                if (index >= deaths.size()) {
                    index = deaths.size() - 1;
                }
            }
        });
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
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

        String dimension = death.getDimension().split(":")[1];
        drawRight(TextFormatting.GRAY + dimension, guiTop + 55);

        // Location
        String textLocation = new TextComponentTranslation("gui.death_history.location").getFormattedText() + ":";
        drawLeft(TextFormatting.DARK_GRAY + textLocation, guiTop + 70);

        drawRight(TextFormatting.GRAY + "" + Math.round(death.getPosX()) + " X", guiTop + 70);
        drawRight(TextFormatting.GRAY + "" + Math.round(death.getPosY()) + " Y", guiTop + 85);
        drawRight(TextFormatting.GRAY + "" + Math.round(death.getPosZ()) + " Z", guiTop + 100);

        // Player
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        EntityPlayer player = new EntityOtherPlayerMP(mc.world, new GameProfile(death.getPlayerUUID(), death.getPlayerName()));
        player.height = Float.MAX_VALUE;
        GuiInventory.drawEntityOnScreen(guiLeft + xSize - (xSize - hSplit) / 2, guiTop + ySize / 2 + 30, 40, (guiLeft + xSize - (xSize - hSplit) / 2) - mouseX, (guiTop + ySize / 2) - mouseY, player);
    }


    @Override
    public void tick() {
        super.tick();
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
