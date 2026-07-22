package de.maxhenkel.corpse.data;

import de.maxhenkel.corpse.Death;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.Locale;

public class CorpseRegistry extends WorldSavedData {

    private static final String DATA_NAME = "corpse_recovery_registry";
    private static final int ID_LENGTH = 6;
    private static final String ID_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Random RANDOM = new Random();

    private final Map<String, Entry> entries;

    public CorpseRegistry() {
        this(DATA_NAME);
    }

    public CorpseRegistry(String name) {
        super(name);
        entries = new LinkedHashMap<>();
    }

    public static CorpseRegistry get(World world) {
        WorldServer storageWorld = getStorageWorld(world);
        MapStorage storage = storageWorld.getMapStorage();
        CorpseRegistry registry = (CorpseRegistry) storage.getOrLoadData(CorpseRegistry.class, DATA_NAME);
        if (registry == null) {
            registry = new CorpseRegistry();
            storage.setData(DATA_NAME, registry);
        }
        return registry;
    }

    private static WorldServer getStorageWorld(World world) {
        if (world instanceof WorldServer) {
            WorldServer worldServer = (WorldServer) world;
            if (worldServer.getMinecraftServer() != null) {
                return worldServer.getMinecraftServer().getWorld(0);
            }
            return worldServer;
        }
        throw new IllegalStateException("Corpse registry can only be accessed on the server");
    }

    public synchronized Entry register(Death death) {
        String recoveryId = generateId();
        Entry entry = Entry.fromDeath(recoveryId, death);
        entries.put(recoveryId, entry);
        markDirty();
        return entry;
    }

    @Nullable
    public synchronized Entry claim(String id) {
        Entry entry = entries.remove(normalizeId(id));
        if (entry != null) {
            markDirty();
        }
        return entry;
    }

    @Nullable
    public synchronized Entry get(String id) {
        return entries.get(normalizeId(id));
    }

    public synchronized Collection<Entry> getEntries() {
        return new ArrayList<>(entries.values());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        entries.clear();
        NBTTagList list = nbt.getTagList("Entries", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            Entry entry = Entry.fromNBT(list.getCompoundTagAt(i));
            if (entry != null) {
                entry.id = normalizeId(entry.id);
                if (entry.id.length() != ID_LENGTH) {
                    entry.id = shortenLegacyId(entry.id);
                }
                while (entries.containsKey(entry.id)) {
                    entry.id = generateId();
                }
                entries.put(entry.getId(), entry);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (Entry entry : entries.values()) {
            list.appendTag(entry.toNBT());
        }
        compound.setTag("Entries", list);
        return compound;
    }

    public static class Entry {
        private String id;
        private UUID playerUUID;
        private String playerName;
        private long timestamp;
        private int dimension;
        private double posX;
        private double posY;
        private double posZ;
        private NonNullList<ItemStack> items;

        private Entry() {
            items = NonNullList.create();
        }

        public static Entry fromDeath(String recoveryId, Death death) {
            Entry entry = new Entry();
            entry.id = normalizeId(recoveryId);
            entry.playerUUID = death.getPlayerUUID();
            entry.playerName = death.getPlayerName();
            entry.timestamp = death.getTimestamp();
            entry.dimension = death.getDimension();
            entry.posX = death.getPosX();
            entry.posY = death.getPosY();
            entry.posZ = death.getPosZ();
            entry.items = copyItems(death.getItems());
            return entry;
        }

        @Nullable
        public static Entry fromNBT(NBTTagCompound compound) {
            if (!compound.hasKey("RecoveryId") && (!compound.hasKey("IdMost") || !compound.hasKey("IdLeast"))) {
                return null;
            }

            Entry entry = new Entry();
            if (compound.hasKey("RecoveryId")) {
                entry.id = compound.getString("RecoveryId");
            } else {
                entry.id = shortenLegacyId(new UUID(compound.getLong("IdMost"), compound.getLong("IdLeast")).toString());
            }
            entry.playerUUID = new UUID(compound.getLong("PlayerUuidMost"), compound.getLong("PlayerUuidLeast"));
            entry.playerName = compound.getString("PlayerName");
            entry.timestamp = compound.getLong("Timestamp");
            entry.dimension = compound.getInteger("Dimension");
            entry.posX = compound.getDouble("PosX");
            entry.posY = compound.getDouble("PosY");
            entry.posZ = compound.getDouble("PosZ");

            NBTTagList itemList = compound.getTagList("Items", 10);
            for (int i = 0; i < itemList.tagCount(); i++) {
                ItemStack stack = new ItemStack(itemList.getCompoundTagAt(i));
                if (!stack.isEmpty()) {
                    entry.items.add(stack);
                }
            }

            return entry;
        }

        public NBTTagCompound toNBT() {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setString("RecoveryId", id);
            compound.setLong("PlayerUuidMost", playerUUID.getMostSignificantBits());
            compound.setLong("PlayerUuidLeast", playerUUID.getLeastSignificantBits());
            compound.setString("PlayerName", playerName == null ? "" : playerName);
            compound.setLong("Timestamp", timestamp);
            compound.setInteger("Dimension", dimension);
            compound.setDouble("PosX", posX);
            compound.setDouble("PosY", posY);
            compound.setDouble("PosZ", posZ);

            NBTTagList itemList = new NBTTagList();
            for (ItemStack stack : items) {
                if (!stack.isEmpty()) {
                    itemList.appendTag(stack.writeToNBT(new NBTTagCompound()));
                }
            }
            compound.setTag("Items", itemList);
            return compound;
        }

        private static NonNullList<ItemStack> copyItems(List<ItemStack> items) {
            NonNullList<ItemStack> copies = NonNullList.create();
            for (ItemStack stack : items) {
                if (!stack.isEmpty()) {
                    copies.add(stack.copy());
                }
            }
            return copies;
        }

        public String getId() {
            return id;
        }

        public UUID getPlayerUUID() {
            return playerUUID;
        }

        public String getPlayerName() {
            return playerName;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public int getDimension() {
            return dimension;
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

        public NonNullList<ItemStack> getItems() {
            return copyItems(items);
        }
    }

    public static String normalizeId(String id) {
        return id == null ? "" : id.trim().toUpperCase(Locale.ROOT);
    }

    public static boolean isValidId(String id) {
        String normalized = normalizeId(id);
        return normalized.length() == ID_LENGTH && normalized.matches("[A-Z0-9]+");
    }

    public static String shortenLegacyId(String uuidString) {
        String normalized = normalizeId(uuidString.replace("-", ""));
        if (normalized.length() >= ID_LENGTH) {
            return normalized.substring(0, ID_LENGTH);
        }
        StringBuilder builder = new StringBuilder(normalized);
        while (builder.length() < ID_LENGTH) {
            builder.append('0');
        }
        return builder.toString();
    }

    private String generateId() {
        String id;
        do {
            StringBuilder builder = new StringBuilder(ID_LENGTH);
            for (int i = 0; i < ID_LENGTH; i++) {
                builder.append(ID_ALPHABET.charAt(RANDOM.nextInt(ID_ALPHABET.length())));
            }
            id = builder.toString();
        } while (entries.containsKey(id));
        return id;
    }
}
