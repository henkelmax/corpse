package de.maxhenkel.corpse.gui;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.corelib.CachedMap;
import de.maxhenkel.corelib.inventory.ScreenBase;
import de.maxhenkel.corpse.Death;
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
    protected void func_231160_c_() {
        super.func_231160_c_();

        field_230710_m_.clear();
        int padding = 7;
        int buttonWidth = 50;
        int buttonHeight = 20;
        previous = func_230480_a_(new Button(guiLeft + padding, guiTop + ySize - buttonHeight - padding, buttonWidth, buttonHeight, new TranslationTextComponent("button.corpse.previous"), button -> {
            index--;
            if (index < 0) {
                index = 0;
            }
        }));

        func_230480_a_(new Button(guiLeft + (xSize - buttonWidth) / 2, guiTop + ySize - buttonHeight - padding, buttonWidth, buttonHeight, new TranslationTextComponent("button.corpse.show_items"), button -> {
            Main.SIMPLE_CHANNEL.sendToServer(new MessageShowCorpseInventory(getCurrentDeath().getPlayerUUID(), getCurrentDeath().getId()));
        }));

        next = func_230480_a_(new Button(guiLeft + xSize - buttonWidth - padding, guiTop + ySize - buttonHeight - padding, buttonWidth, buttonHeight, new TranslationTextComponent("button.corpse.next"), button -> {
            index++;
            if (index >= deaths.size()) {
                index = deaths.size() - 1;
            }

        }));
    }

    @Override
    public boolean func_231044_a_(double x, double y, int clickType) {
        if (x >= guiLeft + 7 && x <= guiLeft + hSplit && y >= guiTop + 70 && y <= guiTop + 100 + field_230712_o_.FONT_HEIGHT) {
            BlockPos pos = getCurrentDeath().getBlockPos();
            ITextComponent teleport = TextComponentUtils.func_240647_a_(new TranslationTextComponent("chat.coordinates", pos.getX(), pos.getY(), pos.getZ()))
                    .func_240700_a_((style) -> style
                            .func_240723_c_(TextFormatting.GREEN)
                            .func_240715_a_(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/execute in " + getCurrentDeath().getDimension() + " run tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ()))
                            .func_240716_a_(new HoverEvent(HoverEvent.Action.field_230550_a_, new TranslationTextComponent("chat.coordinates.tooltip")))
                    );
            field_230706_i_.player.sendMessage(new TranslationTextComponent("chat.corpse.teleport_death_location", teleport), Util.field_240973_b_);
            field_230706_i_.getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1F));
            field_230706_i_.displayGuiScreen(null);
        }
        return super.func_231044_a_(x, y, clickType);
    }

    @Override
    protected void func_230451_b_(MatrixStack matrixStack, int mouseX, int mouseY) {
        super.func_230451_b_(matrixStack, mouseX, mouseY);
        Death death = getCurrentDeath();

        // Title
        IFormattableTextComponent title = new TranslationTextComponent("gui.corpse.death_history.title").func_240699_a_(TextFormatting.BLACK);
        int titleWidth = field_230712_o_.getStringWidth(title.getString());
        field_230712_o_.func_238422_b_(matrixStack, title.func_241878_f(), (xSize - titleWidth) / 2, 7, 0);

        // Date
        IFormattableTextComponent date = new StringTextComponent(getDate(death.getTimestamp()).getString()).func_240699_a_(TextFormatting.DARK_GRAY);
        int dateWidth = field_230712_o_.getStringWidth(date.getString());
        field_230712_o_.func_238422_b_(matrixStack, date.func_241878_f(), (xSize - dateWidth) / 2, 20, 0);

        // Name
        drawLeft(matrixStack,
                new TranslationTextComponent("gui.corpse.death_history.name")
                        .func_230529_a_(new StringTextComponent(":"))
                        .func_240699_a_(TextFormatting.DARK_GRAY),
                40);

        drawRight(matrixStack, new StringTextComponent(death.getPlayerName()).func_240699_a_(TextFormatting.GRAY), 40);

        // Dimension
        IFormattableTextComponent dimension = new TranslationTextComponent("gui.corpse.death_history.dimension")
                .func_230529_a_(new StringTextComponent(":"))
                .func_240699_a_(TextFormatting.DARK_GRAY);

        drawLeft(matrixStack, dimension, 55);

        String dimensionName = death.getDimension().split(":")[1];
        boolean shortened = false;

        int dimWidth = field_230712_o_.getStringWidth(dimension.getString());

        while (dimWidth + field_230712_o_.getStringWidth(dimensionName + (shortened ? "..." : "")) >= hSplit - 7) {
            dimensionName = dimensionName.substring(0, dimensionName.length() - 1);
            shortened = true;
        }

        drawRight(matrixStack,
                new StringTextComponent(dimensionName + (shortened ? "..." : "")).func_240699_a_(TextFormatting.GRAY), 55);

        // Location
        drawLeft(matrixStack,
                new TranslationTextComponent("gui.corpse.death_history.location")
                        .func_230529_a_(new StringTextComponent(":"))
                        .func_240699_a_(TextFormatting.DARK_GRAY)
                , 70);


        drawRight(matrixStack, new StringTextComponent(Math.round(death.getPosX()) + " X").func_240699_a_(TextFormatting.GRAY), 70);
        drawRight(matrixStack, new StringTextComponent(Math.round(death.getPosY()) + " Y").func_240699_a_(TextFormatting.GRAY), 85);
        drawRight(matrixStack, new StringTextComponent(Math.round(death.getPosZ()) + " Z").func_240699_a_(TextFormatting.GRAY), 100);

        // Player
        RenderSystem.color4f(1F, 1F, 1F, 1F);

        DummyPlayer dummyPlayer = players.get(death, () -> new DummyPlayer(field_230706_i_.world, new GameProfile(death.getPlayerUUID(), death.getPlayerName()), death.getEquipment()));

        InventoryScreen.drawEntityOnScreen((int) (xSize * 0.75D), ySize / 2 + 30, 40, (int) (guiLeft + (xSize * 0.75D)) - mouseX, (ySize / 2) - mouseY, dummyPlayer);

        if (mouseX >= guiLeft + 7 && mouseX <= guiLeft + hSplit && mouseY >= guiTop + 70 && mouseY <= guiTop + 100 + field_230712_o_.FONT_HEIGHT) {
            func_238654_b_(matrixStack, Collections.singletonList(new TranslationTextComponent("tooltip.corpse.teleport").func_241878_f()), mouseX - guiLeft, mouseY - guiTop);
        } else if (mouseX >= guiLeft + 7 && mouseX <= guiLeft + hSplit && mouseY >= guiTop + 55 && mouseY <= guiTop + 55 + field_230712_o_.FONT_HEIGHT) {
            func_238654_b_(matrixStack, Lists.newArrayList(new TranslationTextComponent("gui.corpse.death_history.dimension").func_241878_f(), new StringTextComponent(death.getDimension()).func_240699_a_(TextFormatting.GRAY).func_241878_f()), mouseX - guiLeft, mouseY - guiTop);

        }
    }

    public static ITextComponent getDate(long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(new TranslationTextComponent("gui.corpse.death_history.date_format").getString());
        return new StringTextComponent(dateFormat.format(new Date(timestamp)));
    }

    @Override
    public void func_231023_e_() {
        super.func_231023_e_();
        if (index <= 0) {
            previous.field_230693_o_ = false;
        } else {
            previous.field_230693_o_ = true;
        }

        if (index >= deaths.size() - 1) {
            next.field_230693_o_ = false;
        } else {
            next.field_230693_o_ = true;
        }
    }

    public void drawLeft(MatrixStack matrixStack, IFormattableTextComponent text, int height) {
        field_230712_o_.func_238422_b_(matrixStack, text.func_241878_f(), 7, height, 0);
    }

    public void drawRight(MatrixStack matrixStack, IFormattableTextComponent text, int height) {
        int strWidth = field_230712_o_.getStringWidth(text.getString());
        field_230712_o_.func_238422_b_(matrixStack, text.func_241878_f(), hSplit - strWidth, height, 0);
    }

    public Death getCurrentDeath() {
        return deaths.get(index);
    }

}
