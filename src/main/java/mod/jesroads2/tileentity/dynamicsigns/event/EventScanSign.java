package mod.jesroads2.tileentity.dynamicsigns.event;

import mod.jesroads2.tileentity.dynamicsigns.TileEntityDynamicSignController;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class EventScanSign implements IFreewayEvent {
    public static final int id = 3;

    private final EnumFacing direction;

    public EventScanSign(EnumFacing dir){
        direction = dir;
    }

    public EventScanSign(NBTTagCompound nbt){
        this(EnumFacing.getHorizontal(nbt.getInteger("direction")));
    }

    @Override
    public NBTTagCompound getTag() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("type", id);
        nbt.setInteger("direction", direction.getHorizontalIndex());
        return nbt;
    }

    @Override
    public void handleEvent(TileEntityDynamicSignController controller) {
        controller.scanForSigns(direction);
    }

    @Override
    public void notifyNeighbor(TileEntityDynamicSignController base, TileEntityDynamicSignController controller) {}

    @Override
    public String displayName() {
        return "Scan for signs";
    }
}