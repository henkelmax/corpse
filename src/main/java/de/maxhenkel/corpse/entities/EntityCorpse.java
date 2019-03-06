package de.maxhenkel.corpse.entities;

import de.maxhenkel.corpse.Config;
import de.maxhenkel.corpse.Death;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.gui.GUIManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class EntityCorpse extends EntityCorpseInventoryBase {

    private static final DataParameter<Optional<UUID>> ID = EntityDataManager.createKey(EntityCorpse.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final DataParameter<String> NAME = EntityDataManager.createKey(EntityCorpse.class, DataSerializers.STRING);
    private static final DataParameter<Float> ROTATION = EntityDataManager.createKey(EntityCorpse.class, DataSerializers.FLOAT);
    private static final DataParameter<Integer> AGE = EntityDataManager.createKey(EntityCorpse.class, DataSerializers.VARINT);

    private static final AxisAlignedBB NULL_AABB = new AxisAlignedBB(0D, 0D, 0D, 0D, 0D, 0D);
    private static final UUID NULL_UUID = new UUID(0L, 0L);

    private AxisAlignedBB boundingBox;

    public EntityCorpse(World world) {
        super(Main.CORPSE_ENTITY_TYPE, world);
        width = 2F;
        height = 0.5F;
        boundingBox = NULL_AABB;
    }

    public static EntityCorpse createFromDeath(EntityPlayer player, Death death) {
        EntityCorpse corpse = new EntityCorpse(player.world);
        corpse.setCorpseUUID(death.getPlayerUUID());
        corpse.setCorpseName(death.getPlayerName());
        corpse.setItems(death.getItems());
        corpse.setPosition(death.getPosX(), death.getPosY(), death.getPosZ());
        corpse.setCorpseRotation(player.rotationYaw);
        return corpse;
    }

    @Override
    public void tick() {
        super.tick();
        recalculateBoundingBox();
        setCorpseAge(getCorpseAge() + 1);

        if (!collidedVertically && posY > 0D) {
            motionY = Math.max(-2D, motionY - 0.0625D);
        } else {
            motionY = 0D;
        }

        if (posY < 0D) {
            setPositionAndUpdate(posX, 0F, posZ);
        }

        move(MoverType.SELF, motionX, motionY, motionZ);

        if (world.isRemote) {
            return;
        }

        if (isEmpty() && ticksExisted > 200) {
            remove();
        }
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        if (!world.isRemote && player instanceof EntityPlayerMP) {
            EntityPlayerMP playerMP = (EntityPlayerMP) player;
            if (Config.SERVER.onlyOwnerAccess.get()) {
                boolean isOp = playerMP.hasPermissionLevel(playerMP.server.getOpPermissionLevel());

                if (!isOp || !playerMP.getUniqueID().equals(getCorpseUUID())) {
                    return true;
                }
            }
            GUIManager.openCorpseGUI((EntityPlayerMP) player, this);
        }
        return true;
    }

    public void recalculateBoundingBox() {
        EnumFacing facing = dataManager == null ? EnumFacing.NORTH : EnumFacing.fromAngle(getCorpseRotation());
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
            return new TextComponentTranslation("entity.corpse.corpse_of", getCorpseName());
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

    public void writeAdditional(NBTTagCompound compound) {
        super.writeAdditional(compound);

        UUID uuid = getCorpseUUID();
        if (uuid != null) {
            compound.setLong("IDMost", uuid.getMostSignificantBits());
            compound.setLong("IDLeast", uuid.getLeastSignificantBits());
        }
        compound.setString("Name", getCorpseName());
        compound.setFloat("Rotation", getCorpseRotation());
        compound.setInt("Age", getCorpseAge());
    }

    public void readAdditional(NBTTagCompound compound) {
        super.readAdditional(compound);

        if (compound.hasKey("IDMost") && compound.hasKey("IDLeast")) {
            setCorpseUUID(new UUID(compound.getLong("IDMost"), compound.getLong("IDLeast")));
        }
        setCorpseName(compound.getString("Name"));
        setCorpseRotation(compound.getFloat("Rotation"));
        setCorpseAge(compound.getInt("Age"));
    }
}
