package de.maxhenkel.corpse;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class Death {

    private UUID id;
    private UUID playerUUID;
    private String playerName;
    private NonNullList<ItemStack> items;
    private long timestamp;
    private int experience;
    private double posX;
    private double posY;
    private double posZ;
    private int dimension;

    private Death() {

    }

    public UUID getId() {
        return id;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getExperience() {
        return experience;
    }

    public double getPosX() {
        return posX;
    }

    public double getPosY() {
        return posY;
    }

    public double getPosZ() {
        return posZ;
    }

    public BlockPos getBlockPos() {
        return new BlockPos(posX, posY, posZ);
    }

    public int getDimension() {
        return dimension;
    }

    @Override
    public String toString() {
        return "Death{name=" + playerName + "timestamp=" + timestamp + "}";
    }

    public static Death fromPlayer(EntityPlayer player, NonNullList<ItemStack> items) {
        Death death = new Death();
        death.id = UUID.randomUUID();
        death.playerUUID = player.getUniqueID();
        death.playerName = player.getName();
        death.items = items;
        death.timestamp = System.currentTimeMillis();
        death.experience = player.experienceLevel;
        death.posX = player.posX;
        death.posY = player.posY;
        death.posZ = player.posZ;
        death.dimension = player.dimension;

        return death;
    }

    public static Death fromNBT(NBTTagCompound compound) {
        Death death = new Death();
        death.id = new UUID(compound.getLong("IdMost"), compound.getLong("IdLeast"));
        death.playerUUID = new UUID(compound.getLong("PlayerUuidMost"), compound.getLong("PlayerUuidLeast"));
        death.playerName = compound.getString("PlayerName");

        death.items = NonNullList.create();
        if (compound.hasKey("Items")) {
            NBTTagList itemList = compound.getTagList("Items", 10);
            for (int i = 0; i < itemList.tagCount(); i++) {
                death.items.add(new ItemStack(itemList.getCompoundTagAt(i)));
            }
        }

        death.timestamp = compound.getLong("Timestamp");
        death.experience = compound.getInteger("Experience");
        death.posX = compound.getDouble("PosX");
        death.posY = compound.getDouble("PosY");
        death.posZ = compound.getDouble("PosZ");
        death.dimension = compound.getInteger("Dimension");

        return death;
    }

    public NBTTagCompound toNBT() {
        return toNBT(true);
    }

    public NBTTagCompound toNBT(boolean withItems) {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setLong("IdMost", id.getMostSignificantBits());
        compound.setLong("IdLeast", id.getLeastSignificantBits());
        compound.setLong("PlayerUuidMost", playerUUID.getMostSignificantBits());
        compound.setLong("PlayerUuidLeast", playerUUID.getLeastSignificantBits());
        compound.setString("PlayerName", playerName);

        if (withItems) {
            NBTTagList itemList = new NBTTagList();
            for (ItemStack stack : items) {
                itemList.appendTag(stack.writeToNBT(new NBTTagCompound()));
            }
            compound.setTag("Items", itemList);
        }

        compound.setLong("Timestamp", timestamp);
        compound.setInteger("Experience", experience);
        compound.setDouble("PosX", posX);
        compound.setDouble("PosY", posY);
        compound.setDouble("PosZ", posZ);
        compound.setInteger("Dimension", dimension);

        return compound;
    }

}
