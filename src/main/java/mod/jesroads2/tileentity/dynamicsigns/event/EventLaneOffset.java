package mod.jesroads2.tileentity.dynamicsigns.event;

import mod.jesroads2.tileentity.dynamicsigns.TileEntityDynamicSignController;
import net.minecraft.nbt.NBTTagCompound;

public class EventLaneOffset implements IFreewayEvent {
    public static final int id = 0;

    private final int laneOffsetIn, laneOffsetOut;

    public EventLaneOffset(int offsetIn, int offsetOut){
        laneOffsetIn = offsetIn;
        laneOffsetOut = offsetOut;
    }

    public EventLaneOffset(NBTTagCompound nbt){ this(nbt.getInteger("laneOffsetIn"), nbt.getInteger("laneOffsetOut")); }

    @Override
    public NBTTagCompound getTag() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("type", id);
        nbt.setInteger("laneOffsetIn", laneOffsetIn);
        nbt.setInteger("laneOffsetOut", laneOffsetOut);
        return nbt;
    }

    @Override
    public String displayName() {
        return "Change controller lane offset";
    }

    @Override
    public void handleEvent(TileEntityDynamicSignController controller) {
        controller.setLaneOffsetIn(laneOffsetIn);
        controller.setLaneOffsetOut(laneOffsetOut);
    }

    @Override
    public void notifyNeighbor(TileEntityDynamicSignController base, TileEntityDynamicSignController controller) {

    }
}