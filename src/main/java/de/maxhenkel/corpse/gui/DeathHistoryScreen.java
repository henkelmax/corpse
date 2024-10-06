package de.maxhenkel.corpse.gui;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.corelib.CachedMap;
import de.maxhenkel.corelib.death.Death;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.entities.DummyPlayer;
import de.maxhenkel.corpse.net.MessageShowCorpseInventory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.PacketDistributor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DeathHistoryScreen extends ScreenBase {

    private static final ResourceLocation DEATH_HISTORY_GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/gui/gui_death_history.png");
    private static final Component TITLE = Component.translatable("gui.corpse.death_history.title").withStyle(ChatFormatting.BLACK);
    private static final Component TELEPORT = Component.translatable("tooltip.corpse.teleport");
    private static final Component DIMENSION = Component.translatable("gui.corpse.death_history.dimension");

    private final CachedMap<Death, DummyPlayer> players;

    private Button previous;
    private Button next;

    private final List<Death> deaths;
    private final int hSplit;
    private int index;

    public DeathHistoryScreen(List<Death> deaths) {
        super(TITLE);
        this.players = new CachedMap<>(10_000L);
        this.deaths = deaths;
        this.index = 0;

        texture = DEATH_HISTORY_GUI_TEXTURE;

        xSize = 248;
        ySize = 166;

        hSplit = xSize / 2;
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
            checkButtons();
        }).bounds(guiLeft + padding, guiTop + ySize - buttonHeight - padding, buttonWidth, buttonHeight).build());

        addRenderableWidget(Button.builder(Component.translatable("button.corpse.show_items"), button -> {
            PacketDistributor.sendToServer(new MessageShowCorpseInventory(getCurrentDeath().getPlayerUUID(), getCurrentDeath().getId()));
        }).bounds(guiLeft + (xSize - buttonWidth) / 2, guiTop + ySize - buttonHeight - padding, buttonWidth, buttonHeight).build());

        next = addRenderableWidget(Button.builder(Component.translatable("button.corpse.next"), button -> {
            index++;
            if (index >= deaths.size()) {
                index = deaths.size() - 1;
            }
            checkButtons();
        }).bounds(guiLeft + xSize - buttonWidth - padding, guiTop + ySize - buttonHeight - padding, buttonWidth, buttonHeight).build());

        checkButtons();
    }

    @Override
    public boolean mouseClicked(double x, double y, int clickType) {
        if (x >= guiLeft + 7 && x <= guiLeft + hSplit && y >= guiTop + 70 && y <= guiTop + 100 + font.lineHeight) {
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
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        Death death = getCurrentDeath();

        // Title
        drawCenteredString(guiGraphics, title, guiLeft + xSize / 2, guiTop + 7, 0);

        // Date
        MutableComponent date = Component.literal(getDate(death.getTimestamp()).getString()).withStyle(ChatFormatting.DARK_GRAY);
        drawCenteredString(guiGraphics, date, guiLeft + xSize / 2, guiTop + 20, 0);

        // Name
        drawLeft(guiGraphics,
                Component.translatable("gui.corpse.death_history.name")
                        .append(Component.literal(":"))
                        .withStyle(ChatFormatting.DARK_GRAY),
                guiTop + 40);

        drawRight(guiGraphics, Component.literal(death.getPlayerName()).withStyle(ChatFormatting.GRAY), guiTop + 40);

        // Dimension
        MutableComponent dimension = Component.translatable("gui.corpse.death_history.dimension")
                .append(Component.literal(":"))
                .withStyle(ChatFormatting.DARK_GRAY);

        drawLeft(guiGraphics, dimension, guiTop + 55);

        String dimensionName = death.getDimension().split(":")[1];
        boolean shortened = false;

        int dimWidth = font.width(dimension);

        while (dimWidth + font.width(dimensionName + (shortened ? "..." : "")) >= hSplit - 7) {
            dimensionName = dimensionName.substring(0, dimensionName.length() - 1);
            shortened = true;
        }

        drawRight(guiGraphics, Component.translatable(dimensionName + (shortened ? "..." : "")).withStyle(ChatFormatting.GRAY), guiTop + 55);

        // Location
        drawLeft(guiGraphics,
                Component.translatable("gui.corpse.death_history.location")
                        .append(Component.literal(":"))
                        .withStyle(ChatFormatting.DARK_GRAY)
                , guiTop + 70);


        drawRight(guiGraphics, Component.literal(Math.round(death.getPosX()) + " X").withStyle(ChatFormatting.GRAY), guiTop + 70);
        drawRight(guiGraphics, Component.literal(Math.round(death.getPosY()) + " Y").withStyle(ChatFormatting.GRAY), guiTop + 85);
        drawRight(guiGraphics, Component.literal(Math.round(death.getPosZ()) + " Z").withStyle(ChatFormatting.GRAY), guiTop + 100);

        // Player
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        DummyPlayer dummyPlayer = players.get(death, () -> new DummyPlayer(minecraft.level, new GameProfile(death.getPlayerUUID(), death.getPlayerName()), death.getEquipment(), death.getModel()));

        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, (int) (guiLeft + xSize * 0.75F - 45), guiTop + 25, (int) (guiLeft + xSize * 0.75F + 45), guiTop + 140, 50, 0.0625F, mouseX, mouseY, dummyPlayer);

        if (mouseX >= guiLeft + 7 && mouseX <= guiLeft + hSplit && mouseY >= guiTop + 70 && mouseY <= guiTop + 100 + font.lineHeight) {
            guiGraphics.renderTooltip(font, TELEPORT, mouseX, mouseY);
        } else if (mouseX >= guiLeft + 7 && mouseX <= guiLeft + hSplit && mouseY >= guiTop + 55 && mouseY <= guiTop + 55 + font.lineHeight) {
            guiGraphics.renderComponentTooltip(font, Lists.newArrayList(DIMENSION, Component.literal(death.getDimension()).withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
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

    private void checkButtons() {
        previous.active = index > 0;
        next.active = index < deaths.size() - 1;
    }

    public void drawLeft(GuiGraphics guiGraphics, MutableComponent text, int height) {
        guiGraphics.drawString(font, text, guiLeft + 7, height, 0, false);
    }

    public void drawRight(GuiGraphics guiGraphics, MutableComponent text, int height) {
        guiGraphics.drawString(font, text, guiLeft + hSplit - font.width(text), height, 0, false);
    }

    public Death getCurrentDeath() {
        return deaths.get(index);
    }

}
