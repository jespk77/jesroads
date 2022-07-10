package mod.jesroads2.tileentity.dynamicsigns.event;

import mod.jesroads2.tileentity.dynamicsigns.TileEntityDynamicSignController;
import net.minecraft.nbt.NBTTagCompound;

public class EventWarningLane implements IFreewayEvent {
    public static final int id = 2;

    private final int lane;
    private final boolean isRegular;
    private boolean isEnabled;

    public EventWarningLane(int laneIndex, boolean regular, boolean enabled){
        lane = laneIndex;
        isRegular = regular;
        isEnabled = enabled;
    }

    public EventWarningLane(NBTTagCompound nbt){
        this(nbt.getInteger("lane"), nbt.getBoolean("regular"), nbt.getBoolean("enabled"));
    }

    @Override
    public NBTTagCompound getTag() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("type", id);
        nbt.setInteger("lane", lane);
        nbt.setBoolean("regular", isRegular);
        nbt.setBoolean("enabled", isEnabled);
        return nbt;
    }

    @Override
    public void handleEvent(TileEntityDynamicSignController controller) {
        isEnabled = controller.warningLane(lane, isRegular, isEnabled);
        controller.updateSigns();
    }

    @Override
    public void notifyNeighbor(TileEntityDynamicSignController base, TileEntityDynamicSignController controller) {
        if(!isRegular) {
            controller.closedAheadLane(lane + base.getLaneOffsetIn() + base.getLaneOffsetOut(), isEnabled);
            controller.updateSigns();
        }
    }

    @Override
    public String displayName() {
        return "Merge lane";
    }
}