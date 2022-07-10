package mod.jesroads2.tileentity;

import mod.jesroads2.block.road.BlockRoadSlope;
import mod.jesroads2.block.road.BlockRoadSlope.EnumSlopeShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityRoadSlope extends TileEntityAgeble {
    private EnumSlopeShape type = EnumSlopeShape.UNKNOWN;

    public TileEntityRoadSlope() {
    }

    public TileEntityRoadSlope(World world) {
        super(world);
    }

    public TileEntityRoadSlope(NBTTagCompound nbt) {
        super(nbt);
    }

    @Override
    public void readFromNBT(NBTTagCompound t) {
        super.readFromNBT(t);
        readNBT(t);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound t) {
        t = super.writeToNBT(t);
        return getNBT(t);
    }

    public NBTTagCompound getNBT() {
        return getNBT(new NBTTagCompound());
    }

    private NBTTagCompound getNBT(NBTTagCompound nbt) {
        nbt.setInteger("type", type.id);
        return nbt;
    }

    private void readNBT(NBTTagCompound nbt) {
        type = EnumSlopeShape.fromMeta(nbt.getInteger("type"));
    }

    @Override
    public void onLoad() {
        if (type == EnumSlopeShape.UNKNOWN) type = updateType(true);
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return 30000;
    }

    @Override
    public void onAging() {
        calendar = null;
        placetime = -1;
        markDirty();
    }

    public EnumSlopeShape updateType() {
        EnumSlopeShape shape = updateType(false);
        type = shape;
        markDirty();
        return shape;
    }

    public IBlockState getState() {
        return getWorld().getBlockState(getPos());
    }

    public EnumSlopeShape updateType(boolean load) {
        World world = getWorld();
        IBlockState state = world.getBlockState(getPos());
        EnumFacing face = state.getValue(BlockRoadSlope.facing);

        BlockPos[] neighbors = getNeighborsFromFacing(face, getPos());
        if (neighbors.length == 2) {
            IBlockState n0 = world.getBlockState(neighbors[0]);
            IBlockState n1 = world.getBlockState(neighbors[1]);

            if (n0.getBlock() instanceof BlockRoadSlope
                    && n0.getValue(BlockRoadSlope.facing) == state.getValue(BlockRoadSlope.facing))
                return EnumSlopeShape.DOUBLE_TOP;
            else if (n1.getBlock() instanceof BlockRoadSlope
                    && n1.getValue(BlockRoadSlope.facing) == state.getValue(BlockRoadSlope.facing))
                return EnumSlopeShape.DOUBLE_BOTTOM;
        }
        return EnumSlopeShape.SINGLE;
    }

    public EnumSlopeShape getShape() {
        if (type == EnumSlopeShape.UNKNOWN) {
            type = updateType(true);
        }
        return type;
    }

    private BlockPos[] getNeighborsFromFacing(EnumFacing face, BlockPos pos) {
        switch (face) {
            case NORTH:
                return new BlockPos[]{pos.south(), pos.north()};
            case SOUTH:
                return new BlockPos[]{pos.north(), pos.south()};
            case EAST:
                return new BlockPos[]{pos.west(), pos.east()};
            case WEST:
                return new BlockPos[]{pos.east(), pos.west()};
            default:
                return new BlockPos[]{};
        }
    }
}