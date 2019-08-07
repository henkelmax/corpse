package de.maxhenkel.corpse.entities;

import de.maxhenkel.corpse.Config;
import de.maxhenkel.corpse.Death;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.gui.ScreenManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class CorpseEntity extends CorpseInventoryBaseEntity {

    private static final DataParameter<Optional<UUID>> ID = EntityDataManager.createKey(CorpseEntity.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final DataParameter<String> NAME = EntityDataManager.createKey(CorpseEntity.class, DataSerializers.STRING);
    private static final DataParameter<Float> ROTATION = EntityDataManager.createKey(CorpseEntity.class, DataSerializers.FLOAT);
    private static final DataParameter<Integer> AGE = EntityDataManager.createKey(CorpseEntity.class, DataSerializers.VARINT);

    private static final AxisAlignedBB NULL_AABB = new AxisAlignedBB(0D, 0D, 0D, 0D, 0D, 0D);
    private static final UUID NULL_UUID = new UUID(0L, 0L);

    private AxisAlignedBB boundingBox;

    public CorpseEntity(EntityType type, World world) {
        super(type, world);
        boundingBox = NULL_AABB;
        preventEntitySpawning = true;
    }

    public CorpseEntity(World world) {
        this(Main.CORPSE_ENTITY_TYPE, world);
    }

    public static CorpseEntity createFromDeath(PlayerEntity player, Death death) {
        CorpseEntity corpse = new CorpseEntity(player.world);
        corpse.setCorpseUUID(death.getPlayerUUID());
        corpse.setCorpseName(death.getPlayerName());
        corpse.setItems(death.getItems());
        corpse.setPosition(death.getPosX(), death.getPosY() < 0D ? 0D : death.getPosY(), death.getPosZ());
        corpse.setCorpseRotation(player.rotationYaw);
        return corpse;
    }

    @Override
    public void tick() {
        super.tick();
        recalculateBoundingBox();
        setCorpseAge(getCorpseAge() + 1);

        if (!collidedVertically && posY > 0D) {
            setMotion(getMotion().x, Math.max(-2D, getMotion().y - 0.0625D), getMotion().z);
        } else {
            setMotion(getMotion().x, 0D, getMotion().z);
        }

        if (posY < 0D) {
            setPositionAndUpdate(posX, 0F, posZ);
        }

        move(MoverType.SELF, getMotion());

        if (world.isRemote) {
            return;
        }

        if ((isEmpty() && getCorpseAge() > Config.SERVER.corpseDespawnTime.get()) || (Config.SERVER.corpseForceDespawnTime.get() > 0 && getCorpseAge() > Config.SERVER.corpseForceDespawnTime.get())) {
            remove();
        }
    }

    @Override
    public boolean processInitialInteract(PlayerEntity player, Hand hand) {
        if (!world.isRemote && player instanceof ServerPlayerEntity) {
            ServerPlayerEntity playerMP = (ServerPlayerEntity) player;
            if (Config.SERVER.onlyOwnerAccess.get()) {
                boolean isOp = playerMP.hasPermissionLevel(playerMP.server.getOpPermissionLevel());

                if (!isOp || !playerMP.getUniqueID().equals(getCorpseUUID())) {
                    return true;
                }
            }
            ScreenManager.openCorpseGUI((ServerPlayerEntity) player, this);
        }
        return true;
    }

    public void recalculateBoundingBox() {
        Direction facing = dataManager == null ? Direction.NORTH : Direction.fromAngle(getCorpseRotation());
        boundingBox = new AxisAlignedBB(
                posX - (facing.getXOffset() != 0 ? 1D : 0.5D),
                posY,
                posZ - (facing.getZOffset() != 0 ? 1D : 0.5D),
                posX + (facing.getXOffset() != 0 ? 1D : 0.5D),
                posY + 0.5D,
                posZ + (facing.getZOffset() != 0 ? 1D : 0.5D)
        );
    }

    @Override
    public ITextComponent getDisplayName() {
        String name = getCorpseName();
        if (name == null || name.trim().isEmpty()) {
            return super.getDisplayName();
        } else {
            return new TranslationTextComponent("entity.corpse.corpse_of", getCorpseName());
        }
    }

    @Override
    public boolean canRenderOnFire() {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return boundingBox;
    }

    @Override
    public void setBoundingBox(AxisAlignedBB boundingBox) {
        this.boundingBox = boundingBox;
    }

    @Override
    public void setPosition(double x, double y, double z) {
        super.setPosition(x, y, z);
        recalculateBoundingBox();
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox() {
        return null;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBox(Entity entityIn) {
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return getBoundingBox();
    }

    @Override
    public boolean canBeCollidedWith() {
        return !removed;
    }

    public UUID getCorpseUUID() {
        Optional<UUID> uuid = dataManager.get(ID);
        if (uuid.isPresent()) {
            return uuid.get();
        } else {
            return NULL_UUID;
        }
    }

    public void setCorpseUUID(UUID uuid) {
        if (uuid == null) {
            dataManager.set(ID, Optional.of(NULL_UUID));
        } else {
            dataManager.set(ID, Optional.of(uuid));
        }
    }

    public String getCorpseName() {
        return dataManager.get(NAME);
    }

    public void setCorpseName(String name) {
        dataManager.set(NAME, name);
    }

    public float getCorpseRotation() {
        return dataManager.get(ROTATION);
    }

    public void setCorpseRotation(float rotation) {
        dataManager.set(ROTATION, rotation);
        recalculateBoundingBox();
    }

    public int getCorpseAge() {
        return dataManager.get(AGE);
    }

    public void setCorpseAge(int age) {
        dataManager.set(AGE, age);
    }

    @Override
    protected void registerData() {
        super.registerData();
        dataManager.register(ID, Optional.of(NULL_UUID));
        dataManager.register(NAME, "");
        dataManager.register(ROTATION, 0F);
        dataManager.register(AGE, 0);
    }

    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);

        UUID uuid = getCorpseUUID();
        if (uuid != null) {
            compound.putLong("IDMost", uuid.getMostSignificantBits());
            compound.putLong("IDLeast", uuid.getLeastSignificantBits());
        }
        compound.putString("Name", getCorpseName());
        compound.putFloat("Rotation", getCorpseRotation());
        compound.putInt("Age", getCorpseAge());
    }

    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);

        if (compound.contains("IDMost") && compound.contains("IDLeast")) {
            setCorpseUUID(new UUID(compound.getLong("IDMost"), compound.getLong("IDLeast")));
        }
        setCorpseName(compound.getString("Name"));
        setCorpseRotation(compound.getFloat("Rotation"));
        setCorpseAge(compound.getInt("Age"));
    }
}
