package de.maxhenkel.corpse.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public abstract class EntityCorpseInventoryBase extends Entity implements IInventory {

    private static final DataParameter<Integer> INVENTORY_SIZE = EntityDataManager.createKey(EntityCorpse.class, DataSerializers.VARINT);

    protected IInventory inventory;

    public EntityCorpseInventoryBase(World worldIn) {
        super(worldIn);
    }

    private IInventory getInventory() {
        if (inventory == null) {
            inventory = new InventoryBasic("", false, getFittingInventorySize(dataManager.get(INVENTORY_SIZE)));
        }
        return inventory;
    }

    private int getFittingInventorySize(int size) {
        if (size < 54) {
            size = 54;
        }

        if (size % 54 != 0) {
            size += 54 - (size % 54);
        }
        return size;
    }

    /**
     * Resets the whole corpse inventory and adds the provided items
     *
     * @param items the items that will be added to the inventory
     */
    public void setItems(NonNullList<ItemStack> items) {
        dataManager.set(INVENTORY_SIZE, items.size());
        for (int i = 0; i < getSizeInventory() && i < items.size(); i++) {
            setInventorySlotContents(i, items.get(i));
        }
    }


    @Override
    protected void entityInit() {
        dataManager.register(INVENTORY_SIZE, 54);
    }


    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        int size = compound.getInteger("InventorySize");

        NBTTagList nbttaglist = compound.getTagList("Inventory", 10);

        inventory = new InventoryBasic(new TextComponentString(""), size);

        for (int i = 0; i < nbttaglist.tagCount(); i++) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound.getInteger("Slot");

            if (j >= 0 && j < inventory.getSizeInventory()) {
                inventory.setInventorySlotContents(j, new ItemStack(nbttagcompound));
            }
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < getSizeInventory(); i++) {
            if (!getStackInSlot(i).isEmpty()) {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setInteger("Slot", i);
                getStackInSlot(i).writeToNBT(nbttagcompound);
                nbttaglist.appendTag(nbttagcompound);
            }
        }

        compound.setTag("Inventory", nbttaglist);
        compound.setInteger("InventorySize", getSizeInventory());
    }

    @Override
    public int getSizeInventory() {
        return getInventory().getSizeInventory();
    }

    @Override
    public boolean isEmpty() {
        return getInventory().isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return getInventory().getStackInSlot(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return getInventory().decrStackSize(index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return getInventory().removeStackFromSlot(index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        getInventory().setInventorySlotContents(index, stack);
    }

    @Override
    public int getInventoryStackLimit() {
        return getInventory().getInventoryStackLimit();
    }

    @Override
    public void markDirty() {
        getInventory().markDirty();
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return getInventory().isUsableByPlayer(player);
    }

    @Override
    public void openInventory(EntityPlayer player) {
        getInventory().openInventory(player);
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        getInventory().closeInventory(player);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return getInventory().isItemValidForSlot(index, stack);
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        getInventory().clear();
    }

}
