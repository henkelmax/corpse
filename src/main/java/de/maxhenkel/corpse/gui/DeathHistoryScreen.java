package de.maxhenkel.corpse.gui;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.corelib.CachedMap;
import de.maxhenkel.corelib.death.Death;
import de.maxhenkel.corelib.inventory.ScreenBase;
import de.maxhenkel.corelib.net.NetUtils;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.entities.DummyPlayer;
import de.maxhenkel.corpse.net.MessageShowCorpseInventory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DeathHistoryScreen extends ScreenBase<AbstractContainerMenu> {

    private static final ResourceLocation DEATH_HISTORY_GUI_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/gui_death_history.png");

    private final CachedMap<Death, DummyPlayer> players;

    private Button previous;
    private Button next;

    private List<Death> deaths;
    private int index;
    private int hSplit;

    public DeathHistoryScreen(List<Death> deaths) {
        super(DEATH_HISTORY_GUI_TEXTURE, new DeathHistoryContainer(), Minecraft.getInstance().player.getInventory(), Component.translatable("gui.death_history.corpse.title"));
        this.players = new CachedMap<>(10_000L);
        this.deaths = deaths;
        this.index = 0;

        imageWidth = 248;
        imageHeight = 166;

        hSplit = imageWidth / 2;
    }

    @Override
    protected void init() {
        super.init();

        int padding = 7;
        int buttonWidth = 50;
        int buttonHeight = 20;
        previous = addRenderableWidget(Button.builder(Component.translatable("button.corpse.previous"), button -> {
            index--;
            if (index < 0) {
                index = 0;
            }
        }).bounds(leftPos + padding, topPos + imageHeight - buttonHeight - padding, buttonWidth, buttonHeight).build());

        addRenderableWidget(Button.builder(Component.translatable("button.corpse.show_items"), button -> {
            NetUtils.sendToServer(Main.SIMPLE_CHANNEL, new MessageShowCorpseInventory(getCurrentDeath().getPlayerUUID(), getCurrentDeath().getId()));
        }).bounds(leftPos + (imageWidth - buttonWidth) / 2, topPos + imageHeight - buttonHeight - padding, buttonWidth, buttonHeight).build());

        next = addRenderableWidget(Button.builder(Component.translatable("button.corpse.next"), button -> {
            index++;
            if (index >= deaths.size()) {
                index = deaths.size() - 1;
            }

        }).bounds(leftPos + imageWidth - buttonWidth - padding, topPos + imageHeight - buttonHeight - padding, buttonWidth, buttonHeight).build());
    }

    @Override
    public boolean mouseClicked(double x, double y, int clickType) {
        if (x >= leftPos + 7 && x <= leftPos + hSplit && y >= topPos + 70 && y <= topPos + 100 + font.lineHeight) {
            BlockPos pos = getCurrentDeath().getBlockPos();
            Component teleport = ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", pos.getX(), pos.getY(), pos.getZ()))
                    .withStyle((style) -> style
                            .applyFormat(ChatFormatting.GREEN)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/execute in " + getCurrentDeath().getDimension() + " run tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ()))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.coordinates.tooltip")))
                    );
            minecraft.player.sendSystemMessage(Component.translatable("chat.corpse.teleport_death_location", teleport));
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
            minecraft.setScreen(null);
        }
        return super.mouseClicked(x, y, clickType);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        Death death = getCurrentDeath();

        // Title
        MutableComponent title = Component.translatable("gui.corpse.death_history.title").withStyle(ChatFormatting.BLACK);
        int titleWidth = font.width(title.getString());
        guiGraphics.drawString(font, title.getVisualOrderText(), (imageWidth - titleWidth) / 2, 7, 0, false);

        // Date
        MutableComponent date = Component.literal(getDate(death.getTimestamp()).getString()).withStyle(ChatFormatting.DARK_GRAY);
        int dateWidth = font.width(date);
        guiGraphics.drawString(font, date.getVisualOrderText(), (imageWidth - dateWidth) / 2, 20, 0, false);

        // Name
        drawLeft(guiGraphics,
                Component.translatable("gui.corpse.death_history.name")
                        .append(Component.literal(":"))
                        .withStyle(ChatFormatting.DARK_GRAY),
                40);

        drawRight(guiGraphics, Component.literal(death.getPlayerName()).withStyle(ChatFormatting.GRAY), 40);

        // Dimension
        MutableComponent dimension = Component.translatable("gui.corpse.death_history.dimension")
                .append(Component.literal(":"))
                .withStyle(ChatFormatting.DARK_GRAY);

        drawLeft(guiGraphics, dimension, 55);

        String dimensionName = death.getDimension().split(":")[1];
        boolean shortened = false;

        int dimWidth = font.width(dimension);

        while (dimWidth + font.width(dimensionName + (shortened ? "..." : "")) >= hSplit - 7) {
            dimensionName = dimensionName.substring(0, dimensionName.length() - 1);
            shortened = true;
        }

        drawRight(guiGraphics, Component.translatable(dimensionName + (shortened ? "..." : "")).withStyle(ChatFormatting.GRAY), 55);

        // Location
        drawLeft(guiGraphics,
                Component.translatable("gui.corpse.death_history.location")
                        .append(Component.literal(":"))
                        .withStyle(ChatFormatting.DARK_GRAY)
                , 70);


        drawRight(guiGraphics, Component.literal(Math.round(death.getPosX()) + " X").withStyle(ChatFormatting.GRAY), 70);
        drawRight(guiGraphics, Component.literal(Math.round(death.getPosY()) + " Y").withStyle(ChatFormatting.GRAY), 85);
        drawRight(guiGraphics, Component.literal(Math.round(death.getPosZ()) + " Z").withStyle(ChatFormatting.GRAY), 100);

        // Player
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        DummyPlayer dummyPlayer = players.get(death, () -> new DummyPlayer(minecraft.level, new GameProfile(death.getPlayerUUID(), death.getPlayerName()), death.getEquipment(), death.getModel()));

        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, (int) (leftPos + imageWidth * 0.75D) - 25, topPos + imageHeight / 2 + 5, (int) (leftPos + imageWidth * 0.75D) + 25, topPos + imageHeight / 2 + 55, 30, 0.0625F, mouseX, mouseY, dummyPlayer);
        //InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, guiLeft + TEXTURE_X / 2 - 25, guiTop + 70, guiLeft + TEXTURE_X / 2 + 25, guiTop + 140, 30, 0.0625F, mouseX, mouseY, player);

        if (mouseX >= leftPos + 7 && mouseX <= leftPos + hSplit && mouseY >= topPos + 70 && mouseY <= topPos + 100 + font.lineHeight) {
            guiGraphics.renderTooltip(font, Collections.singletonList(Component.translatable("tooltip.corpse.teleport").getVisualOrderText()), mouseX - leftPos, mouseY - topPos);
        } else if (mouseX >= leftPos + 7 && mouseX <= leftPos + hSplit && mouseY >= topPos + 55 && mouseY <= topPos + 55 + font.lineHeight) {
            guiGraphics.renderTooltip(font, Lists.newArrayList(Component.translatable("gui.corpse.death_history.dimension").getVisualOrderText(), Component.literal(death.getDimension()).withStyle(ChatFormatting.GRAY).getVisualOrderText()), mouseX - leftPos, mouseY - topPos);

        }
    }

    private static boolean errorShown;

    public static Component getDate(long timestamp) {
        SimpleDateFormat dateFormat;
        try {
            dateFormat = new SimpleDateFormat(Component.translatable("gui.corpse.death_history.date_format").getString());
        } catch (Exception e) {
            if (!errorShown) {
                Main.LOGGER.error("Failed to create date format. This indicates a broken translation: 'gui.corpse.death_history.date_format' translated to {}", Component.translatable("gui.corpse.death_history.date_format").getString());
                errorShown = true;
            }
            dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        }
        return Component.literal(dateFormat.format(new Date(timestamp)));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        previous.active = index > 0;

        next.active = index < deaths.size() - 1;
    }

    public void drawLeft(GuiGraphics guiGraphics, MutableComponent text, int height) {
        guiGraphics.drawString(font, text.getVisualOrderText(), 7, height, 0, false);
    }

    public void drawRight(GuiGraphics guiGraphics, MutableComponent text, int height) {
        int strWidth = font.width(text);
        guiGraphics.drawString(font, text.getVisualOrderText(), hSplit - strWidth, height, 0, false);
    }

    public Death getCurrentDeath() {
        return deaths.get(index);
    }

}
