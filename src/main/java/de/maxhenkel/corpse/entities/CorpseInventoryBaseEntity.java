package de.maxhenkel.corpse.entities;

import de.maxhenkel.corelib.dataserializers.DataSerializerItemList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public abstract class CorpseInventoryBaseEntity extends Entity implements IInventory {

    private static final DataParameter<Integer> INVENTORY_SIZE = EntityDataManager.createKey(CorpseInventoryBaseEntity.class, DataSerializers.VARINT);
    private static final DataParameter<NonNullList<ItemStack>> EQUIPMENT = EntityDataManager.createKey(CorpseInventoryBaseEntity.class, DataSerializerItemList.ITEM_LIST);

    protected IInventory inventory;

    public CorpseInventoryBaseEntity(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    public void setEquipment(NonNullList<ItemStack> equipment) {
        dataManager.set(EQUIPMENT, equipment);
    }

    public NonNullList<ItemStack> getEquipment() {
        return dataManager.get(EQUIPMENT);
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
        dataManager.register(EQUIPMENT, NonNullList.withSize(EquipmentSlotType.values().length, ItemStack.EMPTY));
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        int size = compound.getInt("InventorySize");

        ListNBT inv = compound.getList("Inventory", 10);
        inventory = new Inventory(size);
        for (int i = 0; i < inv.size(); i++) {
            CompoundNBT slot = inv.getCompound(i);
            int j = slot.getInt("Slot");

            if (j >= 0 && j < inventory.getSizeInventory()) {
                inventory.setInventorySlotContents(j, ItemStack.read(slot));
            }
        }

        ListNBT equip = compound.getList("Equipment", 10);
        NonNullList<ItemStack> equipment = NonNullList.withSize(EquipmentSlotType.values().length, ItemStack.EMPTY);
        for (int i = 0; i < equip.size(); i++) {
            CompoundNBT stack = equip.getCompound(i);
            equipment.set(i, ItemStack.read(stack));
        }
        setEquipment(equipment);
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        ListNBT inv = new ListNBT();

        for (int i = 0; i < getSizeInventory(); i++) {
            if (!getStackInSlot(i).isEmpty()) {
                CompoundNBT slot = new CompoundNBT();
                slot.putInt("Slot", i);
                getStackInSlot(i).write(slot);
                inv.add(slot);
            }
        }

        compound.put("Inventory", inv);
        compound.putInt("InventorySize", getSizeInventory());

        ListNBT equip = new ListNBT();
        for (ItemStack stack : getEquipment()) {
            CompoundNBT slot = new CompoundNBT();
            stack.write(slot);
            equip.add(slot);
        }
        compound.put("Equipment", equip);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (!removed && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return LazyOptional.of(this::getItemHandler).cast();
        }
        return super.getCapability(cap, side);
    }

    private InvWrapper handler;

    public InvWrapper getItemHandler() {
        if (handler == null || handler.getInv() != getInventory()) {
            handler = new InvWrapper(getInventory());
        }
        return handler;
    }

    @Override
    public void remove() {
        for (int i = 0; i < getSizeInventory(); ++i) {
            InventoryHelper.spawnItemStack(world, getPosX(), getPosY(), getPosZ(), removeStackFromSlot(i));
        }
        super.remove();
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
