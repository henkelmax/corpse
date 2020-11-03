package de.maxhenkel.corpse.gui;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.corelib.CachedMap;
import de.maxhenkel.corelib.death.Death;
import de.maxhenkel.corelib.inventory.ScreenBase;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.entities.DummyPlayer;
import de.maxhenkel.corpse.net.MessageShowCorpseInventory;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DeathHistoryScreen extends ScreenBase<Container> {

    private static final ResourceLocation DEATH_HISTORY_GUI_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/gui_death_history.png");

    private final CachedMap<Death, DummyPlayer> players;

    private Button previous;
    private Button next;

    private List<Death> deaths;
    private int index;
    private int hSplit;

    public DeathHistoryScreen(List<Death> deaths) {
        super(DEATH_HISTORY_GUI_TEXTURE, new DeathHistoryContainer(), null, new TranslationTextComponent("gui.death_history.corpse.title"));
        this.players = new CachedMap<>(10_000L);
        this.deaths = deaths;
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
        previous = addButton(new Button(guiLeft + padding, guiTop + ySize - buttonHeight - padding, buttonWidth, buttonHeight, new TranslationTextComponent("button.corpse.previous"), button -> {
            index--;
            if (index < 0) {
                index = 0;
            }
        }));

        addButton(new Button(guiLeft + (xSize - buttonWidth) / 2, guiTop + ySize - buttonHeight - padding, buttonWidth, buttonHeight, new TranslationTextComponent("button.corpse.show_items"), button -> {
            Main.SIMPLE_CHANNEL.sendToServer(new MessageShowCorpseInventory(getCurrentDeath().getPlayerUUID(), getCurrentDeath().getId()));
        }));

        next = addButton(new Button(guiLeft + xSize - buttonWidth - padding, guiTop + ySize - buttonHeight - padding, buttonWidth, buttonHeight, new TranslationTextComponent("button.corpse.next"), button -> {
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
            ITextComponent teleport = TextComponentUtils.wrapWithSquareBrackets(new TranslationTextComponent("chat.coordinates", pos.getX(), pos.getY(), pos.getZ()))
                    .modifyStyle((style) -> style
                            .applyFormatting(TextFormatting.GREEN)
                            .setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/execute in " + getCurrentDeath().getDimension() + " run tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ()))
                            .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("chat.coordinates.tooltip")))
                    );
            minecraft.player.sendMessage(new TranslationTextComponent("chat.corpse.teleport_death_location", teleport), Util.DUMMY_UUID);
            minecraft.getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1F));
            minecraft.displayGuiScreen(null);
        }
        return super.mouseClicked(x, y, clickType);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        Death death = getCurrentDeath();

        // Title
        IFormattableTextComponent title = new TranslationTextComponent("gui.corpse.death_history.title").mergeStyle(TextFormatting.BLACK);
        int titleWidth = font.getStringWidth(title.getString());
        font.func_238422_b_(matrixStack, title.func_241878_f(), (xSize - titleWidth) / 2, 7, 0);

        // Date
        IFormattableTextComponent date = new StringTextComponent(getDate(death.getTimestamp()).getString()).mergeStyle(TextFormatting.DARK_GRAY);
        int dateWidth = font.getStringPropertyWidth(date);
        font.func_238422_b_(matrixStack, date.func_241878_f(), (xSize - dateWidth) / 2, 20, 0);

        // Name
        drawLeft(matrixStack,
                new TranslationTextComponent("gui.corpse.death_history.name")
                        .append(new StringTextComponent(":"))
                        .mergeStyle(TextFormatting.DARK_GRAY),
                40);

        drawRight(matrixStack, new StringTextComponent(death.getPlayerName()).mergeStyle(TextFormatting.GRAY), 40);

        // Dimension
        IFormattableTextComponent dimension = new TranslationTextComponent("gui.corpse.death_history.dimension")
                .append(new StringTextComponent(":"))
                .mergeStyle(TextFormatting.DARK_GRAY);

        drawLeft(matrixStack, dimension, 55);

        String dimensionName = death.getDimension().split(":")[1];
        boolean shortened = false;

        int dimWidth = font.getStringPropertyWidth(dimension);

        while (dimWidth + font.getStringWidth(dimensionName + (shortened ? "..." : "")) >= hSplit - 7) {
            dimensionName = dimensionName.substring(0, dimensionName.length() - 1);
            shortened = true;
        }

        drawRight(matrixStack,
                new StringTextComponent(dimensionName + (shortened ? "..." : "")).mergeStyle(TextFormatting.GRAY), 55);

        // Location
        drawLeft(matrixStack,
                new TranslationTextComponent("gui.corpse.death_history.location")
                        .append(new StringTextComponent(":"))
                        .mergeStyle(TextFormatting.DARK_GRAY)
                , 70);


        drawRight(matrixStack, new StringTextComponent(Math.round(death.getPosX()) + " X").mergeStyle(TextFormatting.GRAY), 70);
        drawRight(matrixStack, new StringTextComponent(Math.round(death.getPosY()) + " Y").mergeStyle(TextFormatting.GRAY), 85);
        drawRight(matrixStack, new StringTextComponent(Math.round(death.getPosZ()) + " Z").mergeStyle(TextFormatting.GRAY), 100);

        // Player
        RenderSystem.color4f(1F, 1F, 1F, 1F);

        DummyPlayer dummyPlayer = players.get(death, () -> new DummyPlayer(minecraft.world, new GameProfile(death.getPlayerUUID(), death.getPlayerName()), death.getEquipment(), death.getModel()));

        InventoryScreen.drawEntityOnScreen((int) (xSize * 0.75D), ySize / 2 + 30, 40, (int) (guiLeft + (xSize * 0.75D)) - mouseX, (ySize / 2) - mouseY, dummyPlayer);

        if (mouseX >= guiLeft + 7 && mouseX <= guiLeft + hSplit && mouseY >= guiTop + 70 && mouseY <= guiTop + 100 + font.FONT_HEIGHT) {
            renderTooltip(matrixStack, Collections.singletonList(new TranslationTextComponent("tooltip.corpse.teleport").func_241878_f()), mouseX - guiLeft, mouseY - guiTop);
        } else if (mouseX >= guiLeft + 7 && mouseX <= guiLeft + hSplit && mouseY >= guiTop + 55 && mouseY <= guiTop + 55 + font.FONT_HEIGHT) {
            renderTooltip(matrixStack, Lists.newArrayList(new TranslationTextComponent("gui.corpse.death_history.dimension").func_241878_f(), new StringTextComponent(death.getDimension()).mergeStyle(TextFormatting.GRAY).func_241878_f()), mouseX - guiLeft, mouseY - guiTop);

        }
    }

    private static boolean errorShown;

    public static ITextComponent getDate(long timestamp) {
        SimpleDateFormat dateFormat;
        try {
            dateFormat = new SimpleDateFormat(new TranslationTextComponent("gui.corpse.death_history.date_format").getString());
        } catch (Exception e) {
            if (!errorShown) {
                Main.LOGGER.error("Failed to create date format. This indicates a broken translation: 'gui.corpse.death_history.date_format' translated to {}", new TranslationTextComponent("gui.corpse.death_history.date_format").getString());
                errorShown = true;
            }
            dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        }
        return new StringTextComponent(dateFormat.format(new Date(timestamp)));
    }

    @Override
    public void tick() {
        super.tick();
        previous.active = index > 0;

        next.active = index < deaths.size() - 1;
    }

    public void drawLeft(MatrixStack matrixStack, IFormattableTextComponent text, int height) {
        font.func_238422_b_(matrixStack, text.func_241878_f(), 7, height, 0);
    }

    public void drawRight(MatrixStack matrixStack, IFormattableTextComponent text, int height) {
        int strWidth = font.getStringPropertyWidth(text);
        font.func_238422_b_(matrixStack, text.func_241878_f(), hSplit - strWidth, height, 0);
    }

    public Death getCurrentDeath() {
        return deaths.get(index);
    }

}
