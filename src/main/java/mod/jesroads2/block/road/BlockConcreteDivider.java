package mod.jesroads2.block.road;

import java.util.List;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.BlockBase;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockConcreteDivider extends BlockBase {
    public static final PropertyEnum<EnumFacing.Axis> axis = PropertyEnum.create("axis", EnumFacing.Axis.class);
    public static final PropertyBool northwest = PropertyBool.create("northwest"), northeast = PropertyBool.create("northeast"),
            southwest = PropertyBool.create("southwest"), southeast = PropertyBool.create("southeast"),
            north = PropertyBool.create("north"), south = PropertyBool.create("south");

    public BlockConcreteDivider(int id) {
        super(id, new Material(MapColor.GRAY), "concrete_divider", JesRoads2.tabs.road_extra);

        setHardness(0.1F).setResistance(1.F);
        setFullCube(false);
        setDefaultState(getDefaultState().withProperty(axis, EnumFacing.Axis.Z)
                .withProperty(north, true).withProperty(south, true)
                .withProperty(northwest, false).withProperty(northeast, false)
                .withProperty(southwest, false).withProperty(southeast, false));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(axis) == EnumFacing.Axis.Z ? 0 : 1;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(axis, meta == 0 ? EnumFacing.Axis.Z : EnumFacing.Axis.X);
    }

    @Override
    protected void createProperties(List<IProperty<?>> properties) {
        super.createProperties(properties);
        properties.add(axis);
        properties.add(northwest);
        properties.add(northeast);
        properties.add(southwest);
        properties.add(southeast);
        properties.add(north);
        properties.add(south);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        boolean nw, ne, sw, se, n, s;
        switch (state.getValue(axis)) {
            case Z: {
                nw = canConnect(world, pos.north().west());
                ne = canConnect(world, pos.north().east());
                sw = canConnect(world, pos.south().west());
                se = canConnect(world, pos.south().east());
                n = (!nw && !ne) || canConnect(world, pos.north());
                s = (!sw && !se) || canConnect(world, pos.south());
                break;
            }
            case X: {
                nw = canConnect(world, pos.north().east());
                ne = canConnect(world, pos.south().east());
                sw = canConnect(world, pos.north().west());
                se = canConnect(world, pos.south().west());
                n = (!ne && !nw) || canConnect(world, pos.east());
                s = (!se && !sw) || canConnect(world, pos.west());
                break;
            }
            default:
                return state;
        }

        return state.withProperty(north, n).withProperty(south, s)
                .withProperty(northeast, ne).withProperty(northwest, nw)
                .withProperty(southwest, sw).withProperty(southeast, se);
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos.down());
        return state.isBlockNormalCube() || state.getBlock() instanceof BlockRoadSlope;
    }

    private static boolean canConnect(IBlockAccess world, BlockPos pos) {
        return world.getBlockState(pos).getBlock() instanceof BlockConcreteDivider;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing enfacing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase entity) {
        return this.getDefaultState().withProperty(axis, entity.getHorizontalFacing().getAxis());
    }
}