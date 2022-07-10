package mod.jesroads2.tileentity.dynamicsigns.event;

import mod.jesroads2.tileentity.dynamicsigns.TileEntityDynamicSignController;
import net.minecraft.nbt.NBTTagCompound;

public class EventTempOpen implements IFreewayEvent {
    public static final int id = -2;
    public static final EventTempOpen instance = new EventTempOpen();

    private static NBTTagCompound nbt;

    private EventTempOpen(){}

    @Override
    public NBTTagCompound getTag(){
        if(nbt == null) {
            nbt = new NBTTagCompound();
            nbt.setInteger("type", id);
        } return nbt;
    }

    @Override
    public void handleEvent(TileEntityDynamicSignController controller) {
        if(controller.hasTempLanes()){
            controller.toggleTemporaryLaneOpen();
            controller.updateSigns();
        }
    }

    @Override
    public void notifyNeighbor(TileEntityDynamicSignController base, TileEntityDynamicSignController controller) {
        if(controller.hasTempLanes()){
            if(base.isTemporaryLaneOpen() != controller.isTemporaryLaneOpen()) controller.notifyEvent(this);
            controller.toggleOpenPrev(base.isTemporaryLaneOpen());
            controller.updateSigns();
        }
    }

    @Override
    public String displayName() {
        return "Toggle temporary lane";
    }
}