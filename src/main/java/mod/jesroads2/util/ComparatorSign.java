package mod.jesroads2.util;

import java.util.Comparator;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;

public class ComparatorSign implements Comparator<BlockPos> {
    private EnumFacing facing;

    public ComparatorSign withFacing(EnumFacing facing) {
        this.facing = facing;
        return this;
    }

    @Override
    public int compare(BlockPos p1, BlockPos p2) {
        if (facing == null || facing.getHorizontalIndex() < 0) return 0;

        Axis axis = facing.getAxis();
        AxisDirection dir = facing.getAxisDirection();
        int v1 = axis == Axis.X ? p1.getX() : p1.getZ();
        int v2 = axis == Axis.X ? p2.getX() : p2.getZ();

        if (v1 == v2) return 0;
        switch (dir) {
            case POSITIVE:
                return v1 < v2 ? 1 : -1;
            case NEGATIVE:
                return v1 > v2 ? 1 : -1;
            default:
                return 0;
        }
    }
}