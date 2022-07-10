package mod.jesroads2.entity;

import java.util.ArrayList;

import mod.jesroads2.block.road.BlockRoad;
import mod.jesroads2.entity.EntityCar.EnumCarType;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public abstract class EntityDrivable extends EntityLivingBase {

    public static class VehicleProperty {
        public float width, height;
        public float maxSpeed, acceleration;

        public VehicleProperty() {
            width = 1.F;
            height = 1.F;
            maxSpeed = 1.F;
            acceleration = 0.1F;
        }

        public VehicleProperty setWidth(float width) {
            this.width = width;
            return this;
        }

        public VehicleProperty setHeight(float height) {
            this.height = height;
            return this;
        }

        public VehicleProperty setMaxSpeed(float speed) {
            maxSpeed = speed;
            return this;
        }

        public VehicleProperty setAcceleration(float acceleration) {
            this.acceleration = acceleration;
            return this;
        }

    }

    public enum EnumVehicleStatus {
        TERRAIN,
        ROAD,
        LIQUID;

        public float adjustValueForState(float value) {
            switch (this) {
                case TERRAIN:
                    return (float) 0.5 * value;
                case ROAD:
                    return value;
                case LIQUID:
                    return (float) 0.1 * value;
                default:
                    return 0;
            }
        }
    }

    protected final double friction = 0.01D;
    private static final double dismountRange = 2.D;
    private static final float turnAngle = 3.5F;

    private VehicleProperty property;
    private EnumVehicleStatus status;

    public double driverX, driverY, driverZ;
    protected float motion, prevMotion;

    public EntityDrivable(World world) {
        super(world);

        stepHeight = 1;
        property = EnumCarType.BASIC.property;
        status = EnumVehicleStatus.TERRAIN;
    }

    public void setProperty(VehicleProperty property) {
        this.property = property;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        if (!world.isRemote && !isBeingRidden()) player.startRiding(this);
        return true;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (!isEntityInvulnerable(source) && !isBeingRidden()) {
            ItemStack item = returnItem();
            if (item != null) {
                Entity entity = source.getTrueSource();
                if (entity instanceof EntityPlayer) ((EntityPlayer) entity).inventory.addItemStackToInventory(item);
            }
            setDead();
            return true;
        } else return false;
    }

    @Override
    public AxisAlignedBB getCollisionBox(Entity entity) {
        return getEntityBoundingBox();
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox() {
        return getEntityBoundingBox();
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    public void setExitPos(double x, double y, double z) {
        double limitW = width + dismountRange, limitH = height + dismountRange;
        driverX = Math.min(Math.max(posX - x, -limitW), limitW);
        driverY = Math.min(Math.max(posY - y, -limitH), limitH);
        driverZ = Math.min(Math.max(posZ - z, -limitW), limitW);
    }

    @Override
    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        status = getStatus();
        super.onUpdate();

        updateDirection();
        if (isBeingRidden()) updateMotion();
        else motion = 0.F;
    }

    private EnumVehicleStatus getStatus() {
        MutableBlockPos p = new MutableBlockPos((int) posX, (int) posY, (int) posZ);
        if (world.getBlockState(p).getBlock() instanceof BlockLiquid) return EnumVehicleStatus.LIQUID;

        p.move(EnumFacing.DOWN);
        if (world.getBlockState(p).getBlock() instanceof BlockRoad) return EnumVehicleStatus.ROAD;
        else return EnumVehicleStatus.TERRAIN;
    }

    private void updateDirection() {
        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        boolean ridden = isBeingRidden();
        boolean forward = ridden && settings.keyBindForward.isKeyDown();
        boolean backward = ridden && settings.keyBindBack.isKeyDown();
        boolean left = ridden && settings.keyBindLeft.isKeyDown();
        boolean right = ridden && settings.keyBindRight.isKeyDown();
		
		/*float angle = motion > 0? turnAngle: motion < 0? -turnAngle: 0.F;
		if(left && !right) rotationYaw -= angle;
		else if(right && !left) rotationYaw += angle;*/
        Entity r = isBeingRidden() ? getPassengers().get(0) : null;
        if (r != null) rotationYaw = r.rotationYaw;
        rotationYawHead = rotationYaw;

        if (forward && !backward) motion += property.acceleration;
        else if (!forward && backward) motion -= (property.acceleration / 2);
        else motion = (float) (motion > friction ? (motion - friction) :
                    (motion < -motion) ? (motion + friction) : 0.F);
    }

    private void updateMotion() {
        if (motion > property.maxSpeed) motion = status.adjustValueForState(property.maxSpeed);

        motionX = MathHelper.sin(-rotationYaw * 0.017453292F) * motion;
        motionZ = MathHelper.cos(rotationYaw * 0.017453292F) * motion;

        move(MoverType.SELF, motionX, motionY, motionZ);
    }

    @Override
    public boolean canBeHitWithPotion() {
        return false;
    }

    @Override
    public Iterable<ItemStack> getArmorInventoryList() {
        return new ArrayList<>();
    }

    @Override
    public ItemStack getItemStackFromSlot(EntityEquipmentSlot slot) {
        return null;//new ItemStack(JesRoads2.items.car);
    }

    @Override
    public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack) {

    }

    @Override
    public EnumHandSide getPrimaryHand() {
        return EnumHandSide.LEFT;
    }

    public abstract ItemStack returnItem();
}