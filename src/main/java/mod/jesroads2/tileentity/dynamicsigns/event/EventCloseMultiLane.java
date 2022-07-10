package mod.jesroads2.tileentity.dynamicsigns.event;

import mod.jesroads2.tileentity.dynamicsigns.TileEntityDynamicSignController;
import net.minecraft.nbt.NBTTagCompound;

public class EventCloseMultiLane implements IFreewayEvent {
    public static final int id = 4;
    private int lanes;

    public EventCloseMultiLane(int laneIndices) {
        lanes = laneIndices;
    }

    public EventCloseMultiLane(NBTTagCompound nbt) {
        this(nbt.getInteger("lanes"));
    }

    @Override
    public NBTTagCompound getTag() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("type", id);
        nbt.setInteger("lanes", lanes);
        return nbt;
    }

    @Override
    public void handleEvent(TileEntityDynamicSignController controller) {
        if(lanes > 0) {
            int lane = 0;
            while(lane < lanes) controller.closeLane(lane++, false);
            lanes--;
        } else if(lanes < 0) {
            int lane = 0;
            while(lane > lanes) controller.closeLane(lane--, false);
            lanes++;
        }
    }

    @Override
    public void notifyNeighbor(TileEntityDynamicSignController base, TileEntityDynamicSignController controller) {
        if(lanes != 0) controller.notifyEvent(this);
    }

    @Override
    public String displayName() {
        return "Close down lane(s)";
    }
}