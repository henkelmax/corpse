package de.maxhenkel.corpse.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public abstract class CorpseInventoryBaseEntity extends Entity implements IInventory {

    private static final DataParameter<Integer> INVENTORY_SIZE = EntityDataManager.createKey(CorpseEntity.class, DataSerializers.VARINT);

    protected IInventory inventory;

    public CorpseInventoryBaseEntity(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    private IInventory getInventory() {
        if (inventory == null) {
            inventory = new Inventory(getFittingInventorySize(dataManager.get(INVENTORY_SIZE)));
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
    protected void registerData() {
        dataManager.register(INVENTORY_SIZE, 54);
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        int size = compound.getInt("InventorySize");

        ListNBT nbttaglist = compound.getList("Inventory", 10);

        inventory = new Inventory(size);

        for (int i = 0; i < nbttaglist.size(); i++) {
            CompoundNBT nbttagcompound = nbttaglist.getCompound(i);
            int j = nbttagcompound.getInt("Slot");

            if (j >= 0 && j < inventory.getSizeInventory()) {
                inventory.setInventorySlotContents(j, ItemStack.read(nbttagcompound));
            }
        }
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        ListNBT nbttaglist = new ListNBT();

        for (int i = 0; i < getSizeInventory(); i++) {
            if (!getStackInSlot(i).isEmpty()) {
                CompoundNBT nbttagcompound = new CompoundNBT();
                nbttagcompound.putInt("Slot", i);
                getStackInSlot(i).write(nbttagcompound);
                nbttaglist.add(nbttagcompound);
            }
        }

        compound.put("Inventory", nbttaglist);
        compound.putInt("InventorySize", getSizeInventory());
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
    public boolean isUsableByPlayer(PlayerEntity player) {
        return getInventory().isUsableByPlayer(player);
    }

    @Override
    public void openInventory(PlayerEntity player) {
        getInventory().openInventory(player);
    }

    @Override
    public void closeInventory(PlayerEntity player) {
        getInventory().closeInventory(player);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return getInventory().isItemValidForSlot(index, stack);
    }

    @Override
    public void clear() {
        getInventory().clear();
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
