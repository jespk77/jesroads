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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockDynamicSign extends BlockBase implements IFreewaySupportable {
    public enum EnumFreewaySignType implements IStringSerializable {
        None(0, "none", "."),
        Reduced(1, "reduced", "R"),
        Slow(2, "slow", "S"),
        MergeRight(3, "merge_right", ">"),
        MergeLeft(4, "merge_left", "<"),
        Closed(5, "closed", "X"),
        Open(6, "open", "V"),
        End(7, "end", "/"),
        ReducedRegular(8, "reduced_regular", "r");

        public final int id;
        public final String name, display;

        private static final EnumFreewaySignType[] list = new EnumFreewaySignType[values().length];

        EnumFreewaySignType(int index, String codeName, String displayName) {
            id = index;
            name = codeName;
            display = displayName;
        }

        @Override
        public String getName() {
            return name;
        }

        public static EnumFreewaySignType fromID(int id) {
            if (id > 0 && id < list.length) return list[id];
            else return None;
        }

        public static boolean isClosed(EnumFreewaySignType type) {
            return type == null || type == Closed || type == MergeLeft || type == MergeRight;
        }

        static {
            for (EnumFreewaySignType type : values())
                list[type.id] = type;
        }
    }

    private static final AxisAlignedBB northSouthBox = new AxisAlignedBB(0.F, 0.F, 0.1F, 1.F, 1.F, 0.9F),
            eastWestBox = new AxisAlignedBB(0.1F, 0.F, 0.F, 0.9F, 1.F, 1.F);

    public static final PropertyEnum<EnumFreewaySignType> type = PropertyEnum.create("type", EnumFreewaySignType.class);
    public final EnumFacingDiagonal facing;

    public BlockDynamicSign(int id) {
        super(id, new Material(MapColor.BLACK), "dynamic_sign", JesRoads2.tabs.system);

        facing = null;
        setHardness(0.3F).setResistance(1.F);
        setFullCube(false);
        setDefaultState(getDefaultState().withProperty(type, EnumFreewaySignType.None));
    }

    public BlockDynamicSign(EnumFacingDiagonal direction) {
        super(-1, new Material(MapColor.BLACK), "dynamic_sign_" + direction.getName(), null);

        facing = direction;
        setHardness(0.3F).setResistance(1.F);
        setFullCube(false);
        setDefaultState(getDefaultState().withProperty(type, EnumFreewaySignType.None));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (facing == EnumFacingDiagonal.NORTH || facing == EnumFacingDiagonal.SOUTH) return northSouthBox;
        else if (facing == EnumFacingDiagonal.EAST || facing == EnumFacingDiagonal.WEST) return eastWestBox;
        else return super.getBoundingBox(state, world, pos);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(type, EnumFreewaySignType.fromID(meta));
    }

    @Override
    public int getMetaFromState(IBlockState st) {
        return st.getValue(type).id;
    }

    @Override
    protected void createProperties(List<IProperty<?>> properties) {
        super.createProperties(properties);
        properties.add(type);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getPackedLightmapCoords(IBlockState state, IBlockAccess source, BlockPos pos) {
        return JesRoads2.options.other.glowing_textures ? super.getPackedLightmapCoords(state, source, pos) : 15728880;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing enfacing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase entity) {
        if (entity instanceof EntityPlayer) ((EntityPlayer) entity).addStat(StatList.getObjectUseStats(item));

        if (facing == null) {
            EnumFacingDiagonal face = EnumFacingDiagonal.fromEntityF(entity);
            return JesRoads2.blocks.freeway_sign_directional[face.getIndex()].getDefaultState();
        } else return JesRoads2.blocks.freeway_sign_directional[facing.getIndex()].getDefaultState();
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
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(JesRoads2.blocks.freeway_sign);
    }

    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        return new ItemStack(JesRoads2.blocks.freeway_sign, 1);
    }

    @Override
    public BlockBase[] getDirectionalBlocks() {
        return JesRoads2.blocks.freeway_sign_directional;
    }
}