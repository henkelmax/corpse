package de.maxhenkel.corpse.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.corpse.Death;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.net.MessageShowCorpseInventory;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.Pose;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DeathHistoryScreen extends ScreenBase {

    private static final ResourceLocation DEATH_HISTORY_GUI_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/gui_death_history.png");

    private Button previous;
    private Button next;

    private List<Death> deaths;
    private int index;
    private SimpleDateFormat dateFormat;
    private int hSplit;

    public DeathHistoryScreen(List<Death> deaths) {
        super(DEATH_HISTORY_GUI_TEXTURE, new DeathHistoryContainer(), null, new TranslationTextComponent("gui.death_history.title"));
        this.deaths = deaths;
        this.dateFormat = new SimpleDateFormat(new TranslationTextComponent("gui.death_history.date_format").getUnformattedComponentText());
        this.index = 0;

        xSize = 248;
        ySize = 166;

        hSplit = xSize / 2;
    }

    @Override
    protected void init() {
        super.init();

        buttons.clear();
        int padding = 7;
        int buttonWidth = 50;
        int buttonHeight = 20;
        previous = addButton(new Button(guiLeft + padding, guiTop + ySize - buttonHeight - padding, buttonWidth, buttonHeight, new TranslationTextComponent("button.previous").getFormattedText(), button -> {
            index--;
            if (index < 0) {
                index = 0;
            }
        }));

        addButton(new Button(guiLeft + (xSize - buttonWidth) / 2, guiTop + ySize - buttonHeight - padding, buttonWidth, buttonHeight, new TranslationTextComponent("button.show_items").getFormattedText(), button -> {
            Main.SIMPLE_CHANNEL.sendToServer(new MessageShowCorpseInventory(getCurrentDeath().getPlayerUUID(), getCurrentDeath().getId()));
        }));

        next = addButton(new Button(guiLeft + xSize - buttonWidth - padding, guiTop + ySize - buttonHeight - padding, buttonWidth, buttonHeight, new TranslationTextComponent("button.next").getFormattedText(), button -> {
            index++;
            if (index >= deaths.size()) {
                index = deaths.size() - 1;
            }

        }));
    }

    @Override
    public boolean mouseClicked(double x, double y, int clickType) {
        if (x >= guiLeft + 7 && x <= guiLeft + hSplit && y >= guiTop + 70 && y <= guiTop + 100 + font.FONT_HEIGHT) {
            BlockPos pos = getCurrentDeath().getBlockPos();
            ITextComponent teleport = TextComponentUtils.wrapInSquareBrackets(new TranslationTextComponent("chat.coordinates", pos.getX(), pos.getY(), pos.getZ())).applyTextStyle((style) -> {
                style.setColor(TextFormatting.GREEN).setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/execute in " + getCurrentDeath().getDimension() + " run tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ())).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("chat.coordinates.tooltip")));
            });
            minecraft.player.sendMessage(new TranslationTextComponent("chat.teleport_death_location", teleport));
            minecraft.getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            minecraft.displayGuiScreen(null);
        }
        return super.mouseClicked(x, y, clickType);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        Death death = getCurrentDeath();

        // Title
        String title = new TranslationTextComponent("gui.death_history.title").getFormattedText();
        int titleWidth = font.getStringWidth(title);
        font.drawString(TextFormatting.BLACK + title, guiLeft + (xSize - titleWidth) / 2, guiTop + 7, 0);

        // Date
        String date = dateFormat.format(new Date(death.getTimestamp()));
        int dateWidth = font.getStringWidth(date);
        font.drawString(TextFormatting.DARK_GRAY + date, guiLeft + (xSize - dateWidth) / 2, guiTop + 20, 0);

        // Name
        String textName = new TranslationTextComponent("gui.death_history.name").getFormattedText() + ":";
        drawLeft(TextFormatting.DARK_GRAY + textName, guiTop + 40);

        String name = death.getPlayerName();
        drawRight(TextFormatting.GRAY + name, guiTop + 40);

        // Dimension
        String textDimension = new TranslationTextComponent("gui.death_history.dimension").getFormattedText() + ":";
        drawLeft(TextFormatting.DARK_GRAY + textDimension, guiTop + 55);

        String dimension = death.getDimension().split(":")[1];
        drawRight(TextFormatting.GRAY + dimension, guiTop + 55);

        // Location
        String textLocation = new TranslationTextComponent("gui.death_history.location").getFormattedText() + ":";
        drawLeft(TextFormatting.DARK_GRAY + textLocation, guiTop + 70);

        drawRight(TextFormatting.GRAY + "" + Math.round(death.getPosX()) + " X", guiTop + 70);
        drawRight(TextFormatting.GRAY + "" + Math.round(death.getPosY()) + " Y", guiTop + 85);
        drawRight(TextFormatting.GRAY + "" + Math.round(death.getPosZ()) + " Z", guiTop + 100);

        // Player
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        RemoteClientPlayerEntity player = new RemoteClientPlayerEntity(minecraft.world, new GameProfile(death.getPlayerUUID(), death.getPlayerName())) {
            @Override
            public EntitySize getSize(Pose pose) {
                return new EntitySize(super.getSize(pose).width, Float.MAX_VALUE, true);
            }
        };
        player.recalculateSize();

        InventoryScreen.drawEntityOnScreen(guiLeft + xSize - (xSize - hSplit) / 2, guiTop + ySize / 2 + 30, 40, (guiLeft + xSize - (xSize - hSplit) / 2) - mouseX, (guiTop + ySize / 2) - mouseY, player);

        if (mouseX >= guiLeft + 7 && mouseX <= guiLeft + hSplit && mouseY >= guiTop + 70 && mouseY <= guiTop + 100 + font.FONT_HEIGHT) {
            renderTooltip(new TranslationTextComponent("tooltip.teleport").getFormattedText(), mouseX, mouseY);
        }
    }


    @Override
    public void tick() {
        super.tick();
        if (index <= 0) {
            previous.active = false;
        } else {
            previous.active = true;
        }

        if (index >= deaths.size() - 1) {
            next.active = false;
        } else {
            next.active = true;
        }
    }

    public void drawLeft(String string, int height) {
        int offset = 7;
        int offsetLeft = guiLeft + offset;
        font.drawString(string, offsetLeft, height, 0);
    }

    public void drawRight(String string, int height) {
        int strWidth = font.getStringWidth(string);
        font.drawString(string, guiLeft + hSplit - strWidth, height, 0);
    }

    public Death getCurrentDeath() {
        return deaths.get(index);
    }
}
