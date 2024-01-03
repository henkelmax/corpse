package de.maxhenkel.corpse.entities;

import de.maxhenkel.corelib.death.Death;
import de.maxhenkel.corelib.item.ItemUtils;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.gui.Guis;
import de.maxhenkel.corpse.net.MessageSpawnDeathParticles;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;
import java.util.UUID;

public class CorpseEntity extends CorpseBoundingBoxBase {

    private static final EntityDataAccessor<Optional<UUID>> ID = SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> NAME = SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> SKELETON = SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Byte> MODEL = SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<NonNullList<ItemStack>> EQUIPMENT = SynchedEntityData.defineId(CorpseEntity.class, Main.ITEM_LIST_SERIALIZER.get());

    private int age;
    private int emptyAge;

    protected Death death;

    public CorpseEntity(EntityType type, Level world) {
        super(type, world);
        blocksBuilding = true;
        emptyAge = -1;
        death = new Death.Builder(new UUID(0L, 0L), new UUID(0L, 0L)).build();
    }

    public CorpseEntity(Level world) {
        this(Main.CORPSE_ENTITY_TYPE.get(), world);
    }

    public static CorpseEntity createFromDeath(Player player, Death death) {
        CorpseEntity corpse = new CorpseEntity(player.level());
        corpse.death = death;
        corpse.setCorpseUUID(death.getPlayerUUID());
        corpse.setCorpseName(death.getPlayerName());
        corpse.setEquipment(death.getEquipment());
        corpse.setPos(death.getPosX(), Math.max(death.getPosY(), player.level().getMinBuildHeight()), death.getPosZ());
        corpse.setYRot(player.getYRot());
        corpse.setCorpseModel(death.getModel());
        return corpse;
    }

    @Override
    protected float getEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
        return sizeIn.height * 0.35F;
    }

    @Override
    public void tick() {
        super.tick();
        if (!isNoGravity()) {
            double yMotion = 0D;
            Vec3 motion = getDeltaMovement();
            if (isEyeInFluid(FluidTags.WATER) || isEyeInFluid(FluidTags.LAVA)) {
                if (motion.y < 0D) {
                    yMotion = motion.y + (motion.y < 0.03D ? 0.01D : 0D);
                } else {
                    yMotion = motion.y + (motion.y < 0.03D ? 5E-4D : 0D);
                }
            } else if (Main.SERVER_CONFIG.fallIntoVoid.get() || getY() > level().getMinBuildHeight()) {
                yMotion = Math.max(-2D, motion.y - 0.0625D);
            }
            setDeltaMovement(getDeltaMovement().x * 0.75D, yMotion, getDeltaMovement().z * 0.75D);

            if (!Main.SERVER_CONFIG.fallIntoVoid.get() && getY() < level().getMinBuildHeight()) {
                teleportTo(getX(), level().getMinBuildHeight(), getZ());
            }

            move(MoverType.SELF, getDeltaMovement());
        }

        if (level().isClientSide) {
            return;
        }

        age++;
        setIsSkeleton(age >= Main.SERVER_CONFIG.corpseSkeletonTime.get());

        if (Main.SERVER_CONFIG.corpseForceDespawnTime.get() > 0 && age > Main.SERVER_CONFIG.corpseForceDespawnTime.get()) {
            discard();
            return;
        }
        boolean empty = isEmpty();
        if (empty && emptyAge < 0) {
            emptyAge = age;
        } else if (empty && age - emptyAge >= Main.SERVER_CONFIG.corpseDespawnTime.get()) {
            discard();
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
        if (Main.SERVER_CONFIG.lavaDamage.get() && source.is(DamageTypeTags.IS_FIRE) && amount >= 2F) {
            discard();
        }
        return super.hurt(source, amount);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!level().isClientSide && player instanceof ServerPlayer) {
            ServerPlayer playerMP = (ServerPlayer) player;
            if (Main.SERVER_CONFIG.onlyOwnerAccess.get()) {
                boolean isOp = playerMP.hasPermissions(playerMP.server.getOperatorUserPermissionLevel());
                if (isOp || !getCorpseUUID().isPresent() || playerMP.getUUID().equals(getCorpseUUID().get())) {
                    Guis.openCorpseGUI((ServerPlayer) player, this);
                } else if (Main.SERVER_CONFIG.skeletonAccess.get() && isSkeleton()) {
                    Guis.openCorpseGUI((ServerPlayer) player, this);
                }
            } else {
                Guis.openCorpseGUI((ServerPlayer) player, this);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public Component getDisplayName() {
        String name = getCorpseName();
        if (name == null || name.trim().isEmpty()) {
            return super.getDisplayName();
        } else {
            return Component.translatable("entity.corpse.corpse_of", getCorpseName());
        }
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }


    @OnlyIn(Dist.CLIENT)
    @Override
    public AABB getBoundingBoxForCulling() {
        return getBoundingBox();
    }

    @Override
    public boolean isPickable() {
        return isAlive();
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
        entityData.define(EQUIPMENT, NonNullList.withSize(EquipmentSlot.values().length, ItemStack.EMPTY));
    }

    @Override
    public void remove(RemovalReason reason) {
        for (ItemStack item : death.getAllItems()) {
            Containers.dropItemStack(level(), getX(), getY(), getZ(), item);
        }
        super.remove(reason);
        if (level() instanceof ServerLevel serverWorld) {
            serverWorld.getPlayers(player -> player.distanceToSqr(getX(), getY(), getZ()) <= 64D * 64D).forEach(playerEntity -> PacketDistributor.PLAYER.with(playerEntity).send(new MessageSpawnDeathParticles(getUUID())));
        }
    }

    public void spawnDeathParticles() {
        double x = getX();
        double y = getY();
        double z = getZ();
        Vec3 lookVec = getLookAngle().normalize();
        for (int i = 0; i <= 10; i++) {
            double d = ((((double) i) / 10D) - 0.5D) * 2D;
            level().addParticle(ParticleTypes.LARGE_SMOKE, x + lookVec.x * d + (level().random.nextDouble() - 0.5D), y + 0.25D, z + lookVec.z * d + (level().random.nextDouble() - 0.5D), 0D, 0D, 0D);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
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
            NonNullList<ItemStack> equipment = NonNullList.withSize(EquipmentSlot.values().length, ItemStack.EMPTY);
            ItemUtils.readItemList(compound, "Equipment", equipment);
            builder.equipment(equipment);
            builder.playerName(compound.getString("Name"));
            death = builder.build();
        }
        setEquipment(death.getEquipment());
        setCorpseUUID(death.getPlayerUUID());
        setCorpseName(death.getPlayerName());
        setCorpseModel(death.getModel());
        age = compound.getInt("Age");
        if (compound.contains("EmptyAge")) {
            emptyAge = compound.getInt("EmptyAge");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.put("Death", death.toNBT());
        compound.putInt("Age", age);
        if (emptyAge >= 0) {
            compound.putInt("EmptyAge", emptyAge);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
