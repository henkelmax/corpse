package de.maxhenkel.corpse.entities;

import de.maxhenkel.corpse.Death;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.gui.CorpseContainerProvider;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class CorpseEntity extends CorpseInventoryBaseEntity {

    private static final DataParameter<Optional<UUID>> ID = EntityDataManager.createKey(CorpseEntity.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final DataParameter<Optional<UUID>> DEATH_ID = EntityDataManager.createKey(CorpseEntity.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final DataParameter<String> NAME = EntityDataManager.createKey(CorpseEntity.class, DataSerializers.STRING);
    private static final DataParameter<Float> ROTATION = EntityDataManager.createKey(CorpseEntity.class, DataSerializers.FLOAT);
    private static final DataParameter<Integer> AGE = EntityDataManager.createKey(CorpseEntity.class, DataSerializers.VARINT);

    private static final AxisAlignedBB NULL_AABB = new AxisAlignedBB(0D, 0D, 0D, 0D, 0D, 0D);
    private static final UUID NULL_UUID = new UUID(0L, 0L);

    private AxisAlignedBB boundingBox;
    private int emptyAge;

    public CorpseEntity(EntityType type, World world) {
        super(type, world);
        boundingBox = NULL_AABB;
        preventEntitySpawning = true;
        emptyAge = -1;
    }

    public CorpseEntity(World world) {
        this(Main.CORPSE_ENTITY_TYPE, world);
    }

    public static CorpseEntity createFromDeath(PlayerEntity player, Death death) {
        CorpseEntity corpse = new CorpseEntity(player.world);
        corpse.setCorpseUUID(death.getPlayerUUID());
        corpse.setDeathUUID(death.getId());
        corpse.setCorpseName(death.getPlayerName());
        corpse.setItems(death.getItems());
        corpse.setPosition(death.getPosX(), Math.max(death.getPosY(), 0D), death.getPosZ());
        corpse.setCorpseRotation(player.rotationYaw);
        return corpse;
    }

    @Override
    protected float getEyeHeight(Pose poseIn, EntitySize sizeIn) {
        return sizeIn.height * 0.35F;
    }

    @Override
    public void tick() {
        super.tick();
        recalculateBoundingBox();
        setCorpseAge(getCorpseAge() + 1);

        if (!hasNoGravity()) {
            double yMotion = 0D;
            Vector3d motion = getMotion();
            if (areEyesInFluid(FluidTags.WATER) || areEyesInFluid(FluidTags.LAVA)) {
                if (motion.y < 0D) {
                    yMotion = motion.y + (motion.y < 0.03D ? 0.01D : 0D);
                } else {
                    yMotion = motion.y + (motion.y < 0.03D ? 5E-4D : 0D);
                }
            } else if (Main.SERVER_CONFIG.fallIntoVoid.get() || getPosY() > 0D) {
                yMotion = Math.max(-2D, motion.y - 0.0625D);
            }
            setMotion(getMotion().x * 0.75D, yMotion, getMotion().z * 0.75D);

            if (!Main.SERVER_CONFIG.fallIntoVoid.get() && getPosY() < 0D) {
                setPositionAndUpdate(getPosX(), 0F, getPosZ());
            }

            move(MoverType.SELF, getMotion());
        }

        if (world.isRemote) {
            return;
        }
        if (Main.SERVER_CONFIG.corpseForceDespawnTime.get() > 0 && getCorpseAge() > Main.SERVER_CONFIG.corpseForceDespawnTime.get()) {
            remove();
            return;
        }

        if (isEmpty() && emptyAge < 0) {
            emptyAge = getCorpseAge();
        } else if (isEmpty() && getCorpseAge() - emptyAge >= Main.SERVER_CONFIG.corpseDespawnTime.get()) {
            remove();
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (Main.SERVER_CONFIG.lavaDamage.get() && source.isFireDamage() && amount >= 2F) {
            remove();
        }
        return super.attackEntityFrom(source, amount);
    }

    @Override
    public ActionResultType processInitialInteract(PlayerEntity player, Hand hand) {
        if (!world.isRemote && player instanceof ServerPlayerEntity) {
            ServerPlayerEntity playerMP = (ServerPlayerEntity) player;
            if (Main.SERVER_CONFIG.onlyOwnerAccess.get()) {
                boolean isOp = playerMP.hasPermissionLevel(playerMP.server.getOpPermissionLevel());
                if (isOp || playerMP.getUniqueID().equals(getCorpseUUID())) {
                    openCorpseGUI((ServerPlayerEntity) player, this);
                } else if (Main.SERVER_CONFIG.skeletonAccess.get() && isSkeleton()) {
                    openCorpseGUI((ServerPlayerEntity) player, this);
                }
            } else {
                openCorpseGUI((ServerPlayerEntity) player, this);
            }
        }
        return ActionResultType.SUCCESS;
    }

    public static void openCorpseGUI(ServerPlayerEntity player, CorpseEntity corpse) {
        NetworkHooks.openGui(player, new CorpseContainerProvider(corpse, true, false), packetBuffer -> {
            packetBuffer.writeBoolean(false);
            packetBuffer.writeLong(corpse.getUniqueID().getMostSignificantBits());
            packetBuffer.writeLong(corpse.getUniqueID().getLeastSignificantBits());
        });
    }

    public boolean isSkeleton() {
        return getCorpseAge() >= Main.SERVER_CONFIG.corpseSkeletonTime.get();
    }

    public void recalculateBoundingBox() {
        Direction facing = dataManager == null ? Direction.NORTH : Direction.fromAngle(getCorpseRotation());
        boundingBox = new AxisAlignedBB(
                getPosX() - (facing.getXOffset() != 0 ? 1D : 0.5D),
                getPosY(),
                getPosZ() - (facing.getZOffset() != 0 ? 1D : 0.5D),
                getPosX() + (facing.getXOffset() != 0 ? 1D : 0.5D),
                getPosY() + 0.5D,
                getPosZ() + (facing.getZOffset() != 0 ? 1D : 0.5D)
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

    public UUID getDeathUUID() {
        return dataManager.get(DEATH_ID).orElse(null);
    }

    public void setDeathUUID(UUID uuid) {
        if (uuid == null) {
            dataManager.set(DEATH_ID, Optional.empty());
        } else {
            dataManager.set(DEATH_ID, Optional.of(uuid));
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
        dataManager.register(DEATH_ID, Optional.empty());
        dataManager.register(NAME, "");
        dataManager.register(ROTATION, 0F);
        dataManager.register(AGE, 0);
    }

    @Override
    public void remove() {
        for (int i = 0; i < getSizeInventory(); ++i) {
            InventoryHelper.spawnItemStack(world, getPosX(), getPosY(), getPosZ(), removeStackFromSlot(i));
        }
        super.remove();
    }

    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);

        UUID uuid = getCorpseUUID();
        if (uuid != null) {
            compound.putLong("IDMost", uuid.getMostSignificantBits());
            compound.putLong("IDLeast", uuid.getLeastSignificantBits());
        }

        UUID deathID = getDeathUUID();
        if (deathID != null) {
            compound.putLong("DeathIDMost", deathID.getMostSignificantBits());
            compound.putLong("DeathIDLeast", deathID.getLeastSignificantBits());
        }

        compound.putString("Name", getCorpseName());
        compound.putFloat("Rotation", getCorpseRotation());
        compound.putInt("Age", getCorpseAge());

        if (emptyAge >= 0) {
            compound.putInt("EmptyAge", emptyAge);
        }
    }

    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);

        if (compound.contains("IDMost") && compound.contains("IDLeast")) {
            setCorpseUUID(new UUID(compound.getLong("IDMost"), compound.getLong("IDLeast")));
        }

        if (compound.contains("DeathIDMost") && compound.contains("DeathIDLeast")) {
            setDeathUUID(new UUID(compound.getLong("DeathIDMost"), compound.getLong("DeathIDLeast")));
        }

        setCorpseName(compound.getString("Name"));
        setCorpseRotation(compound.getFloat("Rotation"));
        setCorpseAge(compound.getInt("Age"));

        if (compound.contains("EmptyAge")) {
            emptyAge = compound.getInt("EmptyAge");
        }
    }
}
