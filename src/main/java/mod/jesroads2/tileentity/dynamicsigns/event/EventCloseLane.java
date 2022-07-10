package mod.jesroads2.tileentity.dynamicsigns.event;

import mod.jesroads2.tileentity.dynamicsigns.TileEntityDynamicSignController;
import net.minecraft.nbt.NBTTagCompound;

public class EventCloseLane implements IFreewayEvent {
    public static final int id = 1;

    protected final int lane;
    protected final boolean def;

    private boolean enabled;

    public EventCloseLane(int laneIndex, boolean regular){
        lane = laneIndex;
        def = regular;
    }

    public EventCloseLane(NBTTagCompound nbt){
        lane = nbt.getInteger("lane");
        def = nbt.getBoolean("def");
    }

    @Override
    public NBTTagCompound getTag(){
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("type", id);
        nbt.setInteger("lane", lane);
        nbt.setBoolean("def", def);
        return nbt;
    }

    @Override
    public void handleEvent(TileEntityDynamicSignController controller) {
        enabled = controller.closeLane(lane, def);
    }

    @Override
    public void notifyNeighbor(TileEntityDynamicSignController base, TileEntityDynamicSignController controller) {
        controller.notifyEvent(new EventWarningLane(lane + base.getLaneOffsetOut(), def, enabled));
    }

    @Override
    public String displayName() {
        return "Close lane";
    }
}