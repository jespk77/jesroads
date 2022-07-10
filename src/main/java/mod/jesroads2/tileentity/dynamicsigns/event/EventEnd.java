package mod.jesroads2.tileentity.dynamicsigns.event;

import mod.jesroads2.tileentity.dynamicsigns.TileEntityDynamicSignController;
import net.minecraft.nbt.NBTTagCompound;

public class EventEnd implements IFreewayEvent {
    public static final int id = -1;
    public static final EventEnd instance = new EventEnd();
    private static NBTTagCompound nbt;

    private EventEnd(){}

    @Override
    public NBTTagCompound getTag(){
        if(nbt == null) {
            nbt = new NBTTagCompound();
            nbt.setInteger("type", id);
        } return nbt;
    }

    @Override
    public void handleEvent(TileEntityDynamicSignController controller) {
        controller.toggleEnded();
    }

    @Override
    public void notifyNeighbor(TileEntityDynamicSignController base, TileEntityDynamicSignController controller) {}

    @Override
    public String displayName() {
        return "End previous events";
    }
}