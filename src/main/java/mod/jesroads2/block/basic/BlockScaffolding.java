package mod.jesroads2.block.basic;

import mod.jesroads2.JesRoads2Tab;
import mod.jesroads2.block.BlockBase;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockScaffolding extends BlockBase {
    private static final AxisAlignedBB blockCollision = new AxisAlignedBB(0.1, 0, 0.1, 0.9, 1, 0.9);

    public BlockScaffolding(int id){
        super(id, Material.WOOD, "scaffolding", JesRoads2Tab.getInstance().basic);

        setFullCube(false);
        setSoundType(SoundType.WOOD);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entity, ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ) {
        if(Block.getBlockFromItem(stack.getItem()) instanceof BlockScaffolding){
            while(world.getBlockState(pos).getBlock() instanceof BlockScaffolding){
                pos = pos.up();
            }

            if(world.mayPlace(this, pos, false, EnumFacing.UP, entity)) {
                world.setBlockState(pos, getDefaultState());
                SoundType sound = getSoundType(state, world, pos, null);
                world.playSound(null, pos, sound.getBreakSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
                return true;
            }

            return false;
        }

        return super.onBlockActivated(world, pos, state, entity, stack, side, hitX, hitY, hitZ);
    }

    private static boolean isValidPlacement(World world, BlockPos pos){
        IBlockState state = world.getBlockState(pos);
        return state.getBlock() instanceof BlockScaffolding || state.isSideSolid(world, pos, EnumFacing.UP);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighbor, BlockPos npos) {
        super.neighborChanged(state, world, pos, neighbor, npos);

        if(!world.isRemote){
            world.scheduleBlockUpdate(pos, this, 0, 0);
        }
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state){
        super.onBlockAdded(world, pos, state);

        if(!world.isRemote) {
            world.scheduleBlockUpdate(pos, this, 0, 0);
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        BlockPos supportPos = pos.down();
        if(!isValidPlacement(world, pos.down())){
            if(!world.isRemote && world.isAirBlock(supportPos)) {
                EntityFallingBlock fallingBlock = new EntityFallingBlock(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, world.getBlockState(pos));
                world.spawnEntity(fallingBlock);
            }
        }
    }

    @Override
    public void onBlockDestroyedByPlayer(World world, BlockPos pos, IBlockState state){
        pos = pos.up();
        while(world.getBlockState(pos).getBlock() instanceof BlockScaffolding){
            world.destroyBlock(pos, false);
            pos = pos.up();
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos){
        return blockCollision;
    }

    @Override
    public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
        return true;
    }
}