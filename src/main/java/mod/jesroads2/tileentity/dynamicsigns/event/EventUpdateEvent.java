package mod.jesroads2.tileentity.dynamicsigns.event;

import mod.jesroads2.block.system.BlockEventSign;
import mod.jesroads2.tileentity.dynamicsigns.TileEntityDynamicSignController;
import net.minecraft.nbt.NBTTagCompound;

public class EventUpdateEvent implements IFreewayEvent {
    public static final int id = 6;

    private final BlockEventSign.EnumEventType type;

    private boolean enabled = false;

    public EventUpdateEvent(NBTTagCompound nbt) { this(BlockEventSign.EnumEventType.fromID(nbt.getInteger("event"))); }
    public EventUpdateEvent(BlockEventSign.EnumEventType event){ type = event; }

    @Override
    public NBTTagCompound getTag() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("type", id);
        nbt.setInteger("event", type.ordinal());
        return nbt;
    }

    @Override
    public String displayName() {
        return "Change controller event";
    }

    @Override
    public void handleEvent(TileEntityDynamicSignController controller) {
        enabled = controller.setEvent(type);
    }

    @Override
    public void notifyNeighbor(TileEntityDynamicSignController base, TileEntityDynamicSignController controller) {
        BlockEventSign.EnumEventType previousEvent = null;
        switch (type){
            case Closed:
                previousEvent = BlockEventSign.EnumEventType.ClosedWarning;
                break;
            case Accident:
                previousEvent = BlockEventSign.EnumEventType.AccidentWarning;
                break;
            case RoadWorks:
                previousEvent = BlockEventSign.EnumEventType.RoadWorksWarning;
                break;
            case None:
                previousEvent = BlockEventSign.EnumEventType.None;
                break;
        }

        if(previousEvent != null){
            controller.setEvent(previousEvent, enabled);
            controller.updateSigns();
        }
    }
}