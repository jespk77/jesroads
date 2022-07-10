package mod.jesroads2.entity;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityCar extends EntityDrivable {
    public static final ResourceLocation name = new ResourceLocation("jesroads2", "car");

    public enum EnumCarType {
        BASIC;

        public final EntityDrivable.VehicleProperty property;

        static {
            BASIC.property.setWidth(2.5F).setHeight(2.F).setMaxSpeed(1.F).setAcceleration(0.01F);
        }

        EnumCarType() {
            property = new VehicleProperty();
        }

        public static EnumCarType fromID(int id) {
            EnumCarType[] values = EnumCarType.values();
            if (id > 0 && id < values.length) return values[id];
            else return values[0];
        }
    }

    public static final DataParameter<Integer> TYPE = EntityDataManager.createKey(EntityCar.class, DataSerializers.VARINT);

    public EntityCar(World world) {
        super(world);

        dataManager.set(TYPE, EnumCarType.BASIC.ordinal());
        setSize(2.3F, 2.F);
        rotationYaw = 0.F;
    }

    public EntityCar(World world, double x, double y, double z) {
        this(world);

        setPosition(x, y, z);
        motionX = 0.D;
        motionY = 0.D;
        motionZ = 0.D;
        prevPosX = x;
        prevPosY = y;
        prevPosZ = z;

        motion = 0.F;
        prevMotion = 0.F;
        driverX = 1.D;
        driverY = 0.D;
        driverZ = 1.D;
    }

    public void setCarType(EnumCarType type) {
        dataManager.set(TYPE, type.ordinal());
        setProperty(type.property);
    }

    @Override
    protected void entityInit() {
        super.entityInit();

        dataManager.register(TYPE, 0);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        dataManager.set(TYPE, nbt.getInteger("type"));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        nbt.setInteger("type", dataManager.get(TYPE));
    }

    @Override
    public double getMountedYOffset() {
        return 0.4D;
    }

    @Override
    public ItemStack returnItem() {
        return null;//new ItemStack(JesRoads2.items.car, 1, dataManager.get(TYPE));
    }
}