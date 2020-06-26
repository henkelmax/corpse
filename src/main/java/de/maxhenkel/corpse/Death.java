package de.maxhenkel.corpse;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
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
    private String dimension;

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

    public String getDimension() {
        return dimension;
    }

    @Override
    public String toString() {
        return "Death{name=" + playerName + "timestamp=" + timestamp + "}";
    }

    public static Death fromPlayer(PlayerEntity player, NonNullList<ItemStack> items) {
        Death death = new Death();
        death.id = UUID.randomUUID();
        death.playerUUID = player.getUniqueID();
        death.playerName = player.getName().getUnformattedComponentText();
        death.items = items;
        death.timestamp = System.currentTimeMillis();
        death.experience = player.experienceLevel;
        death.posX = player.getPosX();
        death.posY = player.getPosY();
        death.posZ = player.getPosZ();
        death.dimension = player.world.func_234923_W_().func_240901_a_().toString();

        return death;
    }

    public static Death fromNBT(CompoundNBT compound) {
        Death death = new Death();
        death.id = new UUID(compound.getLong("IdMost"), compound.getLong("IdLeast"));
        death.playerUUID = new UUID(compound.getLong("PlayerUuidMost"), compound.getLong("PlayerUuidLeast"));
        death.playerName = compound.getString("PlayerName");

        death.items = NonNullList.create();
        if (compound.contains("Items")) {
            ListNBT itemList = compound.getList("Items", 10);
            for (int i = 0; i < itemList.size(); i++) {
                death.items.add(ItemStack.read(itemList.getCompound(i)));
            }
        }

        death.timestamp = compound.getLong("Timestamp");
        death.experience = compound.getInt("Experience");
        death.posX = compound.getDouble("PosX");
        death.posY = compound.getDouble("PosY");
        death.posZ = compound.getDouble("PosZ");
        death.dimension = compound.getString("Dimension");

        return death;
    }

    public CompoundNBT toNBT() {
        return toNBT(true);
    }

    public CompoundNBT toNBT(boolean withItems) {
        CompoundNBT compound = new CompoundNBT();
        compound.putLong("IdMost", id.getMostSignificantBits());
        compound.putLong("IdLeast", id.getLeastSignificantBits());
        compound.putLong("PlayerUuidMost", playerUUID.getMostSignificantBits());
        compound.putLong("PlayerUuidLeast", playerUUID.getLeastSignificantBits());
        compound.putString("PlayerName", playerName);

        if (withItems) {
            ListNBT itemList = new ListNBT();
            for (ItemStack stack : items) {
                itemList.add(stack.write(new CompoundNBT()));
            }
            compound.put("Items", itemList);
        }

        compound.putLong("Timestamp", timestamp);
        compound.putInt("Experience", experience);
        compound.putDouble("PosX", posX);
        compound.putDouble("PosY", posY);
        compound.putDouble("PosZ", posZ);
        compound.putString("Dimension", dimension);

        return compound;
    }
}
