package mod.jesroads2.block.basic;

import java.util.List;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.BlockBase;
import mod.jesroads2.tileentity.TileEntityFloodlightController.IFloodlightBind;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockFloodlight extends BlockBase implements IFloodlightBind {
    public enum EnumFloodlightType implements IStringSerializable {
        ZERO, ONE, TWO, THREE;

        @Override
        public String getName() {
            return name().toLowerCase();
        }
    }

    public static final PropertyEnum<EnumFloodlightType> type = PropertyEnum.create("type", EnumFloodlightType.class);
    public static final PropertyBool enabled = PropertyBool.create("enabled");

    public BlockFloodlight(int id) {
        super(id, new Material(MapColor.SILVER), "floodlight", JesRoads2.tabs.basic);

        setHardness(0.2F).setResistance(1.F);
        setDefaultState(getDefaultState().withProperty(type, EnumFloodlightType.ZERO).withProperty(enabled, false));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(type, EnumFloodlightType.values()[meta & 3]).withProperty(enabled, (meta >> 2) == 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(type).ordinal() | ((state.getValue(enabled) ? 1 : 0) << 2);
    }

    @Override
    protected void createProperties(List<IProperty<?>> properties) {
        super.createProperties(properties);
        properties.add(BlockFloodlight.type);
        properties.add(BlockFloodlight.enabled);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        BlockPos down = pos.down();
        if (world.isAirBlock(down)) world.setBlockState(down, JesRoads2.blocks.floodlight_beam.getDefaultState(), 3);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighbor, BlockPos npos) {
        if (npos.equals(pos.down()) && neighbor != JesRoads2.blocks.floodlight_beam)
            world.setBlockState(npos, JesRoads2.blocks.floodlight_beam.getDefaultState(), 3);
    }

    @Override
    public void set(World world, BlockPos pos, IBlockState state, boolean on) {
        world.setBlockState(pos, state.withProperty(enabled, on), 3);
    }
}