package de.maxhenkel.corpse;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;

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

    public DimensionType getDimensionType() {
        return DimensionType.byName(new ResourceLocation(dimension));
    }

    @Override
    public String toString() {
        return "Death{name=" + playerName + "timestamp=" + timestamp + "}";
    }

    public static Death fromPlayer(EntityPlayer player, NonNullList<ItemStack> items) {
        Death death = new Death();
        death.id = UUID.randomUUID();
        death.playerUUID = player.getUniqueID();
        death.playerName = player.getName().getUnformattedComponentText();
        death.items = items;
        death.timestamp = System.currentTimeMillis();
        death.experience = player.experienceLevel;
        death.posX = player.posX;
        death.posY = player.posY;
        death.posZ = player.posZ;
        death.dimension = DimensionType.func_212678_a(player.dimension).toString();

        return death;
    }

    public static Death fromNBT(NBTTagCompound compound) {
        Death death = new Death();
        death.id = new UUID(compound.getLong("IdMost"), compound.getLong("IdLeast"));
        death.playerUUID = new UUID(compound.getLong("PlayerUuidMost"), compound.getLong("PlayerUuidLeast"));
        death.playerName = compound.getString("PlayerName");

        death.items = NonNullList.create();
        if(compound.hasKey("Items")){
            NBTTagList itemList = compound.getList("Items", 10);
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

        if(withItems){
            NBTTagList itemList = new NBTTagList();
            for (ItemStack stack : items) {
                itemList.add(stack.write(new NBTTagCompound()));
            }
            compound.setTag("Items", itemList);
        }

        compound.setLong("Timestamp", timestamp);
        compound.setInt("Experience", experience);
        compound.setDouble("PosX", posX);
        compound.setDouble("PosY", posY);
        compound.setDouble("PosZ", posZ);
        compound.setString("Dimension", dimension);

        return compound;
    }

}
