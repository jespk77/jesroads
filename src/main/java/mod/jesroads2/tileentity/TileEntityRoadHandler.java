package mod.jesroads2.tileentity;

import java.util.ArrayList;
import java.util.List;

import mod.jesroads2.tileentity.dynamicsigns.TileEntityDynamicSignController;
import mod.jesroads2.util.NBTUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class TileEntityRoadHandler extends TileEntityBase {
    private final RoadBlock[] blocks;

    public TileEntityRoadHandler() {
        blocks = new RoadBlock[10];
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        String tag;
        for (int i = 0; i < 10; i++) {
            tag = "block_" + i;
            if (nbt.hasKey(tag)) blocks[i] = new RoadBlock(nbt.getCompoundTag(tag));
            else blocks[i] = null;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt = super.writeToNBT(nbt);

        String tag;
        for (int i = 0; i < 10; i++) {
            tag = "block_" + i;
            if (blocks[i] != null) nbt.setTag(tag, blocks[i].writeTag());
        }
        return nbt;
    }

    public boolean addControllerToBlock(int block, BlockPos p) {
        if (block < blocks.length) {
            if (blocks[block] == null) blocks[block] = new RoadBlock();
            markDirty();
            return blocks[block].addController(p);
        } else return false;
    }

    public void removeControllerFromBlock(int block, BlockPos p) {
        if (block < blocks.length) {
            if (blocks[block] == null) return;

            blocks[block].removeController(p);
            if (blocks[block].controllerCount() == 0) blocks[block] = null;
            markDirty();
        }
    }

    public int prepareEvent(IRoadEvent event, int block, int controller) {
        if (block < blocks.length && controller < blocks[block].controllers.size()) {
            TileEntity t = world.getTileEntity(blocks[block].controllers.get(controller));
            if (t instanceof TileEntityDynamicSignController)
                return event.prepareController((TileEntityDynamicSignController) t) ? 1 : 0;
        }
        return -1;
    }

    public int handleEvent(IRoadEvent event, int block, int controller) {
        if (block < blocks.length && controller < blocks[block].controllers.size()) {
            TileEntity t = world.getTileEntity(blocks[block].controllers.get(controller));
            if (t instanceof TileEntityDynamicSignController)
                return event.setController((TileEntityDynamicSignController) t) ? 1 : 0;
        }
        return -1;
    }

    public int finishEvent(IRoadEvent event, int block, int controller) {
        if (block < blocks.length && controller < blocks[block].controllers.size()) {
            TileEntity t = world.getTileEntity(blocks[block].controllers.get(controller));
            if (t instanceof TileEntityDynamicSignController)
                return event.finishController((TileEntityDynamicSignController) t) ? 1 : 0;
        }
        return -1;
    }

    public interface IRoadEvent {
        boolean prepareController(TileEntityDynamicSignController c);

        boolean setController(TileEntityDynamicSignController c);

        boolean finishController(TileEntityDynamicSignController c);
        //public NBTTagCompound getTag();
    }

    protected static class RoadBlock {
        private final List<BlockPos> controllers;
        private static final int size = 5;

        public RoadBlock() {
            controllers = new ArrayList<>(5);
        }

        public RoadBlock(NBTTagCompound nbt) {
            this();
            int id = 0;
            String tag = "pos_" + id;
            while (nbt.hasKey(tag)) {
                addController(NBTUtils.readBlockPos(nbt.getCompoundTag(tag)));
                tag = "pos_" + (++id);
            }
        }

        public boolean addController(BlockPos p) {
            if (controllers.size() < size) {
                controllers.add(p);
                return true;
            } else return false;
        }

        public void removeController(BlockPos p) {
            controllers.remove(p);
        }

        public NBTTagCompound writeTag() {
            NBTTagCompound nbt = new NBTTagCompound();
            int id = 0;
            String tag = "pos_" + id;
            for (BlockPos p : controllers) {
                nbt.setTag(tag, NBTUtils.writeBlockPos(p));
                tag = "pos_" + (++id);
            }
            return null;
        }

        public int controllerCount() {
            return controllers.size();
        }
    }
}
