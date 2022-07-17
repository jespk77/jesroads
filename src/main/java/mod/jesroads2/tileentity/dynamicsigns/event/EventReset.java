package mod.jesroads2.tileentity.dynamicsigns.event;

import mod.jesroads2.tileentity.dynamicsigns.TileEntityDynamicSignController;
import net.minecraft.nbt.NBTTagCompound;

public class EventReset implements IFreewayEvent {
    private static final int id = -4;

    public static final EventReset instance = new EventReset();

    private EventReset(){ }

    @Override
    public NBTTagCompound getTag() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("type", id);
        return nbt;
    }

    @Override
    public String displayName() {
        return "Reset controller";
    }

    @Override
    public void handleEvent(TileEntityDynamicSignController controller) {
        controller.reset();
    }

    @Override
    public void notifyNeighbor(TileEntityDynamicSignController base, TileEntityDynamicSignController controller) {

    }
}