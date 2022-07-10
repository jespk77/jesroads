package mod.jesroads2.tileentity.dynamicsigns;

import mod.jesroads2.tileentity.dynamicsigns.event.*;
import net.minecraft.nbt.NBTTagCompound;

public class FreewayEvents {
	private static final IFreewayEvent[] static_events = new IFreewayEvent[] {
			EventEnd.instance,
			EventTempOpen.instance,
			EventCloseAll.instance
	};

	private static final Class<?>[] dynamic_events = new Class<?>[]{
			EventLaneOffset.class,
			EventCloseLane.class,
			EventWarningLane.class,
			EventScanSign.class,
			EventCloseMultiLane.class,
			EventUpdateSpeed.class,
			EventUpdateEvent.class
	};

	public static IFreewayEvent fromNBT(NBTTagCompound nbt){
		int tp = nbt.getInteger("type");
		try {
			if(tp < 0) return static_events[Math.abs(tp) - 1];
			else return (IFreewayEvent) dynamic_events[tp].getConstructor(NBTTagCompound.class).newInstance(nbt); }
		catch (Exception e) {
			System.err.printf("Failed to get freeway event from id %d: %s\n", tp, e.getMessage());
			e.printStackTrace(System.err);
			return null;
		}
	}
}