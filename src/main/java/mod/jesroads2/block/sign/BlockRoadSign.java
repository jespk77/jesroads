package mod.jesroads2.block.sign;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.BlockBaseHorizontal;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public abstract class BlockRoadSign extends BlockBaseHorizontal {
    public enum ESupportType implements IStringSerializable {
        None, Post, Back;

        @Override
        public String getName() {
            return toString().toLowerCase();
        }
    }

    public static final PropertyBool upSign = PropertyBool.create("up_sign");
    public static final PropertyEnum<ESupportType> supportType = PropertyEnum.create("support_type", ESupportType.class);

    public BlockRoadSign(int id, String name) {
        super(id, Material.IRON, name, JesRoads2.tabs.sign, true);

        setHardness(0.5F);
        setFullCube(false);
        setDefaultState(getDefaultState().withProperty(upSign, false).withProperty(supportType, ESupportType.None));
    }

    @Override
    protected void createProperties(List<IProperty<?>> properties) {
        super.createProperties(properties);
        properties.add(upSign);
        properties.add(supportType);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos){
        EnumFacing face = world.getBlockState(pos).getValue(facing);
        boolean isSignUp = world.getBlockState(pos.up()).getBlock() instanceof BlockRoadSign;

        BlockPos signpostPosition = pos;
        while(world.getBlockState(signpostPosition).getBlock() instanceof BlockRoadSign){
            signpostPosition = signpostPosition.down();
        }

        ESupportType type = ESupportType.None;
        if(world.getBlockState(signpostPosition).getBlock() instanceof BlockSignPost){
            type = ESupportType.Post;
        } else if(world.getBlockState(pos.offset(face)).getBlock() instanceof BlockRoadSign){
            type = ESupportType.Back;
            isSignUp = false;
        }
        return super.getActualState(state, world, pos).withProperty(upSign, isSignUp).withProperty(supportType, type);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        float minX, maxX, minZ, maxZ;
        switch (state.getValue(facing)) {
            case SOUTH: {
                minX = 0.F;
                maxX = 1.F;
                minZ = 0.7F;
                maxZ = 1.F;
                break;
            }
            case WEST: {
                minX = 0.F;
                maxX = 0.3F;
                minZ = 0.F;
                maxZ = 1.F;
                break;
            }
            case EAST: {
                minX = 0.79F;
                maxX = 1.F;
                minZ = 0.F;
                maxZ = 1.F;
                break;
            }
            default: {
                minX = 0.F;
                maxX = 1.F;
                minZ = 0.F;
                maxZ = 0.3F;
                break;
            }
        }
        return new AxisAlignedBB(minX, 0.F, minZ, maxX, 1.F, maxZ);
    }

    protected static boolean shouldPlacePost(World world, BlockPos pos, EnumFacing face) {
        return !(world.getBlockState(pos.down()).getBlock() instanceof BlockRoadSign) &&
                !(world.getBlockState(pos.up().offset(face)).getBlock() instanceof BlockRoadSign) &&
                !(world.getBlockState(pos.offset(face)).getBlock() instanceof BlockRoadSign);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighbor, BlockPos npos) {
        EnumFacing face = state.getValue(facing);
        if (!isSupported(world, pos, face)) world.setBlockToAir(pos);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return getMetaFromState(state);
    }

    protected boolean isSupported(World world, BlockPos pos, EnumFacing face) {
        return isSolid(world, pos.down()) || isSolid(world, pos.offset(face));
    }

    protected boolean isSolid(IBlockAccess world, BlockPos pos) {
        return world.getBlockState(pos).getMaterial().isSolid();
    }
}