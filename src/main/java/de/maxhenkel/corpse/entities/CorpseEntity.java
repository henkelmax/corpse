package de.maxhenkel.corpse.entities;

import de.maxhenkel.corelib.dataserializers.DataSerializerItemList;
import de.maxhenkel.corelib.death.Death;
import de.maxhenkel.corelib.item.ItemUtils;
import de.maxhenkel.corelib.net.NetUtils;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.gui.Guis;
import de.maxhenkel.corpse.net.MessageSpawnDeathParticles;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.Optional;
import java.util.UUID;

public class CorpseEntity extends CorpseBoundingBoxBase {

    private static final DataParameter<Optional<UUID>> ID = EntityDataManager.defineId(CorpseEntity.class, DataSerializers.OPTIONAL_UUID);
    private static final DataParameter<String> NAME = EntityDataManager.defineId(CorpseEntity.class, DataSerializers.STRING);
    private static final DataParameter<Boolean> SKELETON = EntityDataManager.defineId(CorpseEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Byte> MODEL = EntityDataManager.defineId(CorpseEntity.class, DataSerializers.BYTE);
    private static final DataParameter<NonNullList<ItemStack>> EQUIPMENT = EntityDataManager.defineId(CorpseEntity.class, DataSerializerItemList.ITEM_LIST);

    private int age;
    private int emptyAge;

    protected Death death;

    public CorpseEntity(EntityType type, World world) {
        super(type, world);
        blocksBuilding = true;
        emptyAge = -1;
        death = new Death.Builder(new UUID(0L, 0L), new UUID(0L, 0L)).build();
    }

    public CorpseEntity(World world) {
        this(Main.CORPSE_ENTITY_TYPE, world);
    }

    public static CorpseEntity createFromDeath(PlayerEntity player, Death death) {
        CorpseEntity corpse = new CorpseEntity(player.level);
        corpse.death = death;
        corpse.setCorpseUUID(death.getPlayerUUID());
        corpse.setCorpseName(death.getPlayerName());
        corpse.setEquipment(death.getEquipment());
        corpse.setPos(death.getPosX(), Math.max(death.getPosY(), 0D), death.getPosZ());
        corpse.yRot = player.yRot;
        corpse.setCorpseModel(death.getModel());
        return corpse;
    }

    @Override
    protected float getEyeHeight(Pose poseIn, EntitySize sizeIn) {
        return sizeIn.height * 0.35F;
    }

    @Override
    public void tick() {
        super.tick();
        age++;
        setIsSkeleton(age >= Main.SERVER_CONFIG.corpseSkeletonTime.get());

        if (!isNoGravity()) {
            double yMotion = 0D;
            Vector3d motion = getDeltaMovement();
            if (isEyeInFluid(FluidTags.WATER) || isEyeInFluid(FluidTags.LAVA)) {
                if (motion.y < 0D) {
                    yMotion = motion.y + (motion.y < 0.03D ? 0.01D : 0D);
                } else {
                    yMotion = motion.y + (motion.y < 0.03D ? 5E-4D : 0D);
                }
            } else if (Main.SERVER_CONFIG.fallIntoVoid.get() || getY() > 0D) {
                yMotion = Math.max(-2D, motion.y - 0.0625D);
            }
            setDeltaMovement(getDeltaMovement().x * 0.75D, yMotion, getDeltaMovement().z * 0.75D);

            if (!Main.SERVER_CONFIG.fallIntoVoid.get() && getY() < 0D) {
                teleportTo(getX(), 0F, getZ());
            }

            move(MoverType.SELF, getDeltaMovement());
        }

        if (level.isClientSide) {
            return;
        }
        if (Main.SERVER_CONFIG.corpseForceDespawnTime.get() > 0 && age > Main.SERVER_CONFIG.corpseForceDespawnTime.get()) {
            remove();
            return;
        }
        boolean empty = isEmpty();
        if (empty && emptyAge < 0) {
            emptyAge = age;
        } else if (empty && age - emptyAge >= Main.SERVER_CONFIG.corpseDespawnTime.get()) {
            remove();
        }
    }

    public boolean isMainInventoryEmpty() {
        return death.getMainInventory().stream().allMatch(ItemStack::isEmpty)
                && death.getArmorInventory().stream().allMatch(ItemStack::isEmpty)
                && death.getOffHandInventory().stream().allMatch(ItemStack::isEmpty);
    }

    public boolean isAdditionalInventoryEmpty() {
        return death.getAdditionalItems().stream().allMatch(ItemStack::isEmpty);
    }

    public boolean isEmpty() {
        return isMainInventoryEmpty() && isAdditionalInventoryEmpty();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (Main.SERVER_CONFIG.lavaDamage.get() && source.isFire() && amount >= 2F) {
            remove();
        }
        return super.hurt(source, amount);
    }

    @Override
    public ActionResultType interact(PlayerEntity player, Hand hand) {
        if (!level.isClientSide && player instanceof ServerPlayerEntity) {
            ServerPlayerEntity playerMP = (ServerPlayerEntity) player;
            if (Main.SERVER_CONFIG.onlyOwnerAccess.get()) {
                boolean isOp = playerMP.hasPermissions(playerMP.server.getOperatorUserPermissionLevel());
                if (isOp || !getCorpseUUID().isPresent() || playerMP.getUUID().equals(getCorpseUUID().get())) {
                    Guis.openCorpseGUI((ServerPlayerEntity) player, this);
                } else if (Main.SERVER_CONFIG.skeletonAccess.get() && isSkeleton()) {
                    Guis.openCorpseGUI((ServerPlayerEntity) player, this);
                }
            } else {
                Guis.openCorpseGUI((ServerPlayerEntity) player, this);
            }
        }
        return ActionResultType.SUCCESS;
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
    public boolean displayFireAnimation() {
        return false;
    }


    @OnlyIn(Dist.CLIENT)
    @Override
    public AxisAlignedBB getBoundingBoxForCulling() {
        return getBoundingBox();
    }

    @Override
    public boolean isPickable() {
        return !removed;
    }

    public Optional<UUID> getCorpseUUID() {
        return entityData.get(ID);
    }

    public void setCorpseUUID(UUID uuid) {
        if (uuid == null) {
            entityData.set(ID, Optional.empty());
        } else {
            entityData.set(ID, Optional.of(uuid));
        }
    }

    public Death getDeath() {
        return death;
    }

    @OnlyIn(Dist.CLIENT)
    public void setDeath(Death death) {
        this.death = death;
    }

    public String getCorpseName() {
        return entityData.get(NAME);
    }

    public void setCorpseName(String name) {
        entityData.set(NAME, name);
    }

    public boolean isSkeleton() {
        return entityData.get(SKELETON);
    }

    public void setIsSkeleton(boolean skeleton) {
        entityData.set(SKELETON, skeleton);
    }

    public byte getCorpseModel() {
        return entityData.get(MODEL);
    }

    public void setCorpseModel(byte model) {
        entityData.set(MODEL, model);
    }

    public void setEquipment(NonNullList<ItemStack> equipment) {
        entityData.set(EQUIPMENT, equipment);
    }

    public NonNullList<ItemStack> getEquipment() {
        return entityData.get(EQUIPMENT);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(ID, Optional.empty());
        entityData.define(NAME, "");
        entityData.define(SKELETON, false);
        entityData.define(MODEL, (byte) 0);
        entityData.define(EQUIPMENT, NonNullList.withSize(EquipmentSlotType.values().length, ItemStack.EMPTY));
    }

    @Override
    public void remove() {
        for (ItemStack item : death.getAllItems()) {
            InventoryHelper.dropItemStack(level, getX(), getY(), getZ(), item);
        }
        super.remove();

        if (level instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) level;
            serverWorld.getPlayers(player -> player.distanceToSqr(getX(), getY(), getZ()) <= 64D * 64D).forEach(playerEntity -> NetUtils.sendTo(Main.SIMPLE_CHANNEL, playerEntity, new MessageSpawnDeathParticles(getUUID())));
        }
    }

    public void spawnDeathParticles() {
        double x = getX();
        double y = getY();
        double z = getZ();
        Vector3d lookVec = getLookAngle().normalize();
        for (int i = 0; i <= 10; i++) {
            double d = ((((double) i) / 10D) - 0.5D) * 2D;
            level.addParticle(ParticleTypes.LARGE_SMOKE, x + lookVec.x * d + (level.random.nextDouble() - 0.5D), y + 0.25D, z + lookVec.z * d + (level.random.nextDouble() - 0.5D), 0D, 0D, 0D);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT compound) {
        if (compound.contains("Death")) {
            death = Death.fromNBT(compound.getCompound("Death"));
        } else { // Compatibility
            UUID playerUUID = new UUID(compound.getLong("IDMost"), compound.getLong("IDLeast"));
            UUID deathID = new UUID(compound.getLong("DeathIDMost"), compound.getLong("DeathIDLeast"));

            Death.Builder builder = new Death.Builder(playerUUID, deathID);

            int size = compound.getInt("InventorySize");
            NonNullList<ItemStack> additionalItems = NonNullList.withSize(size, ItemStack.EMPTY);
            ItemUtils.readInventory(compound, "Inventory", additionalItems);
            builder.additionalItems(additionalItems);
            NonNullList<ItemStack> equipment = NonNullList.withSize(EquipmentSlotType.values().length, ItemStack.EMPTY);
            ItemUtils.readItemList(compound, "Equipment", equipment);
            builder.equipment(equipment);
            builder.playerName(compound.getString("Name"));
            death = builder.build();
        }
        setEquipment(death.getEquipment());
        setCorpseUUID(death.getPlayerUUID());
        setCorpseName(death.getPlayerName());
        age = compound.getInt("Age");
        if (compound.contains("EmptyAge")) {
            emptyAge = compound.getInt("EmptyAge");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT compound) {
        compound.put("Death", death.toNBT());
        compound.putInt("Age", age);
        if (emptyAge >= 0) {
            compound.putInt("EmptyAge", emptyAge);
        }
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

}
