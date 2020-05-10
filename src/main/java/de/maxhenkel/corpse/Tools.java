package de.maxhenkel.corpse;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Tools {

    public static ITextComponent getDate(long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(new TranslationTextComponent("gui.death_history.date_format").getUnformattedComponentText());
        return new StringTextComponent(dateFormat.format(new Date(timestamp)));
    }

    public static int getStackCount(IInventory inventory) {
        int count = 0;

        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stackInSlot = inventory.getStackInSlot(i);
            if (!stackInSlot.isEmpty()) {
                count++;
            }
        }
        return count;
    }

}
