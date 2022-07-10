package mod.jesroads2.tileentity.dynamicsigns.event;

import mod.jesroads2.tileentity.dynamicsigns.TileEntityDynamicSignController;
import net.minecraft.nbt.NBTTagCompound;

public interface IFreewayEvent {
    NBTTagCompound getTag();
    String displayName();

    void handleEvent(TileEntityDynamicSignController controller);
    void notifyNeighbor(TileEntityDynamicSignController base, TileEntityDynamicSignController controller);
}