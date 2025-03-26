package de.maxhenkel.corpse.entities;

import de.maxhenkel.corelib.death.Death;
import de.maxhenkel.corelib.item.ItemUtils;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.gui.Guis;
import de.maxhenkel.corpse.net.MessageSpawnDeathParticles;
import net.minecraft.Util;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.EnumMap;
import java.util.UUID;

public class CorpseEntity extends CorpseBoundingBoxBase {

    private static final EntityDataAccessor<UUID> ID = SynchedEntityData.defineId(CorpseEntity.class, Main.UUID_SERIALIZER.get());
    private static final EntityDataAccessor<String> NAME = SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> SKELETON = SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Byte> MODEL = SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<EnumMap<EquipmentSlot, ItemStack>> EQUIPMENT = SynchedEntityData.defineId(CorpseEntity.class, Main.EQUIPMENT_SERIALIZER.get());

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
        corpse.setPlayerUuid(death.getPlayerUUID());
        corpse.setCorpseName(death.getPlayerName());
        corpse.setEquipment(death.getEquipment());
        corpse.setPos(death.getPosX(), Math.max(death.getPosY(), player.level().getMinY()), death.getPosZ());
        corpse.setYRot(player.getYRot());
        corpse.setCorpseModel(death.getModel());
        return corpse;
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
            } else if (Main.SERVER_CONFIG.fallIntoVoid.get() || getY() > level().getMinY()) {
                yMotion = Math.max(-2D, motion.y - 0.0625D);
            }
            setDeltaMovement(getDeltaMovement().x * 0.75D, yMotion, getDeltaMovement().z * 0.75D);

            if (!Main.SERVER_CONFIG.fallIntoVoid.get() && getY() < level().getMinY()) {
                teleportTo(getX(), level().getMinY(), getZ());
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
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (Main.SERVER_CONFIG.lavaDamage.get() && source.is(DamageTypeTags.IS_FIRE) && amount >= 2F) {
            discard();
            return true;
        }
        return false;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!level().isClientSide && player instanceof ServerPlayer) {
            ServerPlayer playerMP = (ServerPlayer) player;
            if (Main.SERVER_CONFIG.onlyOwnerAccess.get()) {
                boolean isOp = playerMP.hasPermissions(playerMP.server.getOperatorUserPermissionLevel());
                if (isOp || playerMP.getUUID().equals(getPlayerUuid())) {
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

    @Override
    public boolean isPickable() {
        return isAlive();
    }

    public UUID getPlayerUuid() {
        return entityData.get(ID);
    }

    public void setPlayerUuid(UUID uuid) {
        entityData.set(ID, uuid);
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

    public void setEquipment(EnumMap<EquipmentSlot, ItemStack> equipment) {
        entityData.set(EQUIPMENT, equipment);
    }

    public EnumMap<EquipmentSlot, ItemStack> getEquipment() {
        return entityData.get(EQUIPMENT);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ID, Util.NIL_UUID);
        builder.define(NAME, "");
        builder.define(SKELETON, false);
        builder.define(MODEL, (byte) 0);
        builder.define(EQUIPMENT, new EnumMap<>(EquipmentSlot.class));
    }

    @Override
    public void remove(RemovalReason reason) {
        for (ItemStack item : death.getAllItems()) {
            Containers.dropItemStack(level(), getX(), getY(), getZ(), item);
        }
        super.remove(reason);
        if (level() instanceof ServerLevel serverWorld) {
            serverWorld.getPlayers(player -> player.distanceToSqr(getX(), getY(), getZ()) <= 64D * 64D).forEach(playerEntity -> PacketDistributor.sendToPlayer(playerEntity, new MessageSpawnDeathParticles(getUUID())));
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
            death = Death.fromNBT(registryAccess(), compound.getCompoundOrEmpty("Death"));
        } else { // Compatibility
            UUID playerUUID = new UUID(compound.getLongOr("IDMost", 0L), compound.getLongOr("IDLeast", 0L));
            UUID deathID = new UUID(compound.getLongOr("DeathIDMost", 0L), compound.getLongOr("DeathIDLeast", 0L));

            Death.Builder builder = new Death.Builder(playerUUID, deathID);

            int size = compound.getIntOr("InventorySize", 0);
            NonNullList<ItemStack> additionalItems = NonNullList.withSize(size, ItemStack.EMPTY);
            ItemUtils.readInventory(registryAccess(), compound, "Inventory", additionalItems);
            builder.additionalItems(additionalItems);
            // We just don't apply legacy equipment
            //NonNullList<ItemStack> equipment = NonNullList.withSize(EquipmentSlot.values().length, ItemStack.EMPTY);
            //ItemUtils.readItemList(registryAccess(), compound, "Equipment", equipment);
            //builder.equipment(equipment);
            builder.playerName(compound.getStringOr("Name", ""));
            death = builder.build();
        }
        setEquipment(death.getEquipment());
        setPlayerUuid(death.getPlayerUUID());
        setCorpseName(death.getPlayerName());
        setCorpseModel(death.getModel());
        age = compound.getIntOr("Age", 0);
        emptyAge = compound.getIntOr("EmptyAge", -1);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.put("Death", death.toNBT(registryAccess()));
        compound.putInt("Age", age);
        if (emptyAge >= 0) {
            compound.putInt("EmptyAge", emptyAge);
        }
    }

}
