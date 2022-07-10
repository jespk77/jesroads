package mod.jesroads2.tileentity.dynamicsigns.event;

import mod.jesroads2.tileentity.dynamicsigns.TileEntityDynamicSignController;
import net.minecraft.nbt.NBTTagCompound;

public class EventUpdateSpeed implements IFreewayEvent {
    public static final int id = 5;
    private final TileEntityDynamicSignController.ESpeedLimit newSpeed;

    public EventUpdateSpeed(TileEntityDynamicSignController.ESpeedLimit speed){
        newSpeed = speed;
    }

    public EventUpdateSpeed(NBTTagCompound nbt){
        this(TileEntityDynamicSignController.ESpeedLimit.values()[nbt.getInteger("speed")]);
    }

    @Override
    public NBTTagCompound getTag() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("type", id);
        nbt.setInteger("speed", newSpeed.ordinal());
        return nbt;
    }

    @Override
    public void handleEvent(TileEntityDynamicSignController controller) {
        controller.setSpeedLimit(newSpeed);
    }

    @Override
    public void notifyNeighbor(TileEntityDynamicSignController base, TileEntityDynamicSignController controller) {

    }

    @Override
    public String displayName() {
        return "Change the controller speed limit";
    }
}