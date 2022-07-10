package mod.jesroads2.tileentity;

import java.util.Map.Entry;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.system.BlockIntersectionController;
import mod.jesroads2.block.system.BlockTrafficlight.EnumTrafficLightState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class TileEntityDirectionController extends TileEntityIntersectionController {
    private int cycleDelay = 5;

    public TileEntityDirectionController() {
        super();
    }

    public TileEntityDirectionController(BlockIntersectionController controller, int meta) {
        super(controller, meta);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        cycleDelay = nbt.getInteger("cycle_delay");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt = super.writeToNBT(nbt);
        nbt.setInteger("cycle_delay", cycleDelay);
        return nbt;
    }

    @Override
    protected boolean updateController(boolean empty) {
        if (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            TrafficLightData data = lights.get(pos);


            for (Entry<BlockPos, TrafficLightData> entry : lights.entrySet()) {
                BlockPos p = entry.getKey();
                TrafficLightData d = entry.getValue();
                if (d.isEqual(data)) green.add(p);
                else if (d.getState() == EnumTrafficLightState.GREEN) red.add(p);
            }

            if (!red.isEmpty()) {
                for (BlockPos p : green) {
                    if (!update.contains(p)) {
                        TrafficLightData d = lights.get(p);
                        d.cycle = cycleDelay + JesRoads2.options.intersection_controller.cycle_yellow + 2;
                        update.add(p);
                    }
                }
                green.clear();
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void setDefaults() {
        for (Entry<BlockPos, TrafficLightData> entry : lights.entrySet()) {
            BlockPos p = entry.getKey();
            TrafficLightData d = entry.getValue();
            if (d.getState() == EnumTrafficLightState.GREEN && !update.contains(p)) {
                d.cycle = 0;
                update.add(p);
            }
        }
    }

    @Override
    public String getName() {
        return "Direction Controller";
    }

    public int getDelay() {
        return cycleDelay;
    }

    public int setDelay(int delay) {
        if (delay > 0 && cycleDelay != delay) {
            cycleDelay = delay;
            markDirty();
        }
        return getDelay();
    }
}