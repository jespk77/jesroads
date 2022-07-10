package mod.jesroads2.tileentity;

import mod.jesroads2.block.basic.BlockExtender;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityExtender extends TileEntityBase {
    public static final int maxSize = 10;
    private Block block;
    private ItemStack stack;
    private int size;
    private boolean isExtended;

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        if (nbt.hasKey("block")) {
            stack = new ItemStack(nbt.getCompoundTag("block"));
            block = Block.getBlockFromItem(stack.getItem());
            size = stack.getCount();
        }
        isExtended = nbt.getBoolean("extended");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt = super.writeToNBT(nbt);
        if (stack != null) nbt.setTag("block", stack.writeToNBT(new NBTTagCompound()));
        nbt.setBoolean("extended", isExtended);
        return nbt;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState old, IBlockState nw) {
        return old.getBlock() != nw.getBlock();
    }

    public ItemStack getExtendingBlock() {
        return stack;
    }

    public void setExtendingBlock(ItemStack stack) {
        if (stack == null) {
            this.stack = null;
            block = null;
            size = 0;
            return;
        }

        Block b = Block.getBlockFromItem(stack.getItem());
        if (b.isNormalCube(b.getStateFromMeta(stack.getMetadata()))) {
            this.stack = stack;
            block = b;
            int count = stack.getCount();
            size = count < maxSize ? (Math.max(count, 0)) : maxSize;
            stack.setCount(size);
            markDirty();
        }
    }

    public int getSize() {
        return size;
    }

    public int setSize(int size) {
        size = size > maxSize ? maxSize : Math.max(size, 0);
        return getSize();
    }

    public boolean isExtended() {
        return isExtended;
    }

    public void toggleExtend() {
        toggleExtend(!isExtended);
    }

    public void toggleExtend(boolean extended) {
        if (isExtended != extended && stack != null) {
            isExtended = extended;

            BlockPos p = getPos();
            EnumFacing dir = world.getBlockState(p).getValue(BlockExtender.direction);
            IBlockState s = block.getStateFromMeta(stack.getMetadata());
            for (int i = 0; i < size; i++) {
                p = p.offset(dir);
                Block b = world.getBlockState(p).getBlock();
                if (!extended && b.getClass().isInstance(block)) world.setBlockToAir(p);
                else if (extended && b == Blocks.AIR) world.setBlockState(p, s, 3);
                else break;
            }
            markDirty();
        }
    }
}