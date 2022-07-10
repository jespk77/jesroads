package mod.jesroads2.tileentity.dynamicsigns.event;

import mod.jesroads2.tileentity.dynamicsigns.TileEntityDynamicSignController;
import net.minecraft.nbt.NBTTagCompound;

public class EventCloseAll implements IFreewayEvent {
    public static final int id = -3;
    private static NBTTagCompound nbt;

    public static final IFreewayEvent instance = new EventCloseAll();
    private EventCloseAll() {}

    @Override
    public NBTTagCompound getTag(){
        if(nbt == null) {
            nbt = new NBTTagCompound();
            nbt.setInteger("type", id);
        } return nbt;
    }

    @Override
    public void handleEvent(TileEntityDynamicSignController controller) {
        controller.toggleRoadClosed();
        controller.updateSigns();
    }

    @Override
    public void notifyNeighbor(TileEntityDynamicSignController base, TileEntityDynamicSignController controller) {
        if(base.getRoadClosed() != controller.getRoadClosed() && controller.getClosedLanes() == 0 && controller.getControllers().length < 2)
            controller.notifyEvent(this);
    }

    @Override
    public String displayName() {
        return "Close entire road";
    }
}