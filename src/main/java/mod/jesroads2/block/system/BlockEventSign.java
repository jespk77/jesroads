package mod.jesroads2.block.system;

import java.util.List;
import java.util.Random;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.BlockBase;
import mod.jesroads2.util.EnumFacingDiagonal;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockEventSign extends BlockBase implements IFreewaySupportable {
    public enum EnumEventType implements IStringSerializable {
        None(0), RoadWorksWarning(1), RoadWorks(2), AccidentWarning(4), Accident(8),
        LeftMergeWarning(64), LeftMerge(128), RightMergeWarning(64), RightMerge(128),
        ClosedWarning(16), Closed(32);

        public static EnumEventType fromID(int id) {
            EnumEventType[] types = EnumEventType.values();
            if (id > 0 && id < types.length) return types[id];
            else return types[0];
        }

        public static EnumEventType fromFlag(int flag) {
            if (flag >= Closed.flag) return Closed;
            else if (flag >= ClosedWarning.flag) return ClosedWarning;
            else if (flag >= Accident.flag) return Accident;
            else if (flag >= AccidentWarning.flag) return AccidentWarning;
            else if (flag >= RoadWorks.flag) return RoadWorks;
            else if (flag >= RoadWorksWarning.flag) return RoadWorksWarning;
            else return None;
        }

        public final int flag;

        EnumEventType(int flag) {
            this.flag = flag;
        }

        @Override
        public String getName() {
            return name().toLowerCase();
        }
    }

    private static final Material blockMaterial = new Material(MapColor.BLACK);
    public static final PropertyEnum<EnumEventType> event = PropertyEnum.create("event", EnumEventType.class);

    private static final AxisAlignedBB northSouthBox = new AxisAlignedBB(0.F, 0.F, 0.1F, 1.F, 1.F, 0.9F),
            eastWestBox = new AxisAlignedBB(0.1F, 0.F, 0.F, 0.9F, 1.F, 1.F);

    public final EnumFacingDiagonal direction;

    public BlockEventSign(int id) {
        super(id, blockMaterial, "event_sign", JesRoads2.tabs.system);

        direction = null;
        setHardness(0.3F).setResistance(1.F);
        setFullCube(false);
        setDefaultState(getDefaultState().withProperty(event, EnumEventType.None));
    }

    public BlockEventSign(EnumFacingDiagonal dir) {
        super(-1, blockMaterial, "event_sign_" + dir.getName(), null);

        direction = dir;
        setHardness(0.3F).setResistance(1.F);
        setFullCube(false);
        setDefaultState(getDefaultState().withProperty(event, EnumEventType.None));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (direction == EnumFacingDiagonal.NORTH || direction == EnumFacingDiagonal.SOUTH) return northSouthBox;
        else if (direction == EnumFacingDiagonal.EAST || direction == EnumFacingDiagonal.WEST) return eastWestBox;
        else return super.getBoundingBox(state, world, pos);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(event).ordinal();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(event, EnumEventType.fromID(meta));
    }

    @Override
    protected void createProperties(List<IProperty<?>> properties) {
        super.createProperties(properties);
        properties.add(event);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing enfacing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase entity) {
        if (entity instanceof EntityPlayer) ((EntityPlayer) entity).addStat(StatList.getObjectUseStats(item));

        if (direction == null) {
            EnumFacingDiagonal face = EnumFacingDiagonal.fromEntityF(entity);
            return JesRoads2.blocks.event_sign_directional[face.getIndex()].getDefaultState();
        } else return JesRoads2.blocks.event_sign_directional[direction.getIndex()].getDefaultState();
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(JesRoads2.blocks.event_sign);
    }

    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        return new ItemStack(JesRoads2.blocks.event_sign, 1);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entity, ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (stack != null && Block.getBlockFromItem(stack.getItem()) instanceof BlockFreewaySupport) {
            world.setBlockState(pos, JesRoads2.blocks.freewaysupport.getDefaultState(), 2);
            entity.addStat(StatList.getBlockStats(this));
            return true;
        } else return false;
    }

    @Override
    public BlockBase[] getDirectionalBlocks() {
        return JesRoads2.blocks.event_sign_directional;
    }
}