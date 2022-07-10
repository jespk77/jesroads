package mod.jesroads2.block.road;

import java.util.List;
import java.util.Random;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.system.BlockGateController;
import mod.jesroads2.block.system.BlockIntersectionController;
import mod.jesroads2.block.system.BlockTrafficlight;
import mod.jesroads2.block.BlockBase;
import mod.jesroads2.tileentity.TileEntityRoadDetector;
import mod.jesroads2.util.IBindable;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class BlockRoadDetector extends BlockRoad implements ITileEntityProvider, IBindable {
    public static final PropertyBool detected = PropertyBool.create("detected");

    public BlockRoadDetector(int id) {
        super(id, null, "road_detector");

        setDefaultState(getDefaultState().withProperty(detected, false));
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        world.scheduleUpdate(pos, state.getBlock(), tickRate(world));
    }

    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {
        super.onEntityWalk(world, pos, entity);
        if (world.isRemote) return;

        IBlockState state = world.getBlockState(pos);
        if (!state.getValue(detected)) {
            AxisAlignedBB box = BlockRoadDetector.FULL_BLOCK_AABB.offset(pos.up());
            List<EntityPlayer> detect = world.getEntitiesWithinAABB(EntityPlayer.class, box);
            if (!detect.isEmpty()) {
                TileEntityRoadDetector detector = getTileEntity(world, pos, TileEntityRoadDetector.class);
                if (detector != null) detector.setDetected(detect.get(0), false);
                world.setBlockState(pos, state.withProperty(detected, true), 2);
            }
        }
    }

    @Override
    protected void createProperties(List<IProperty<?>> properties) {
        super.createProperties(properties);
        properties.add(detected);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityRoadDetector(world);
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return createNewTileEntity(world, getMetaFromState(state));
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(JesRoads2.blocks.road_detector);
    }

    @Override
    public int tickRate(World world) {
        return 50;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        world.scheduleUpdate(pos, this, tickRate(world));
        super.updateTick(world, pos, state, rand);
        if (world.isRemote) return;

        if (state.getValue(detected)) {
            AxisAlignedBB box = BlockRoadDetector.FULL_BLOCK_AABB.offset(pos.up());
            List<EntityPlayer> detect = world.getEntitiesWithinAABB(EntityPlayer.class, box);
            if (detect.isEmpty()) {
                TileEntityRoadDetector detector = getTileEntity(world, pos, TileEntityRoadDetector.class);
                if (detector != null) detector.setDetected(null, false);
                world.setBlockState(pos, state.withProperty(detected, false), 2);
            }
        } else if (rand.nextInt(100) < JesRoads2.options.other.traffic_random_detection && world.isAirBlock(pos.up())) {
            TileEntityRoadDetector detector = getTileEntity(world, pos, TileEntityRoadDetector.class);
            if (detector != null) detector.setDetected(Minecraft.getMinecraft().player, true);
            world.setBlockState(pos, state.withProperty(detected, true), 2);
        }
    }

    @Override
    public boolean onBind(World world, BlockPos binder, BlockPos pos, EntityPlayer player) {
        TileEntityRoadDetector detect = getTileEntity(world, binder, TileEntityRoadDetector.class);
        if (detect != null) {
            int result = Integer.parseInt(detect.addBind(pos));
            String message;
            switch (result) {
                case 0: {
                    message = "[Road Detector] Controller unbound";
                    break;
                }
                case 1: {
                    message = "[Road Detector] Controller bound";
                    break;
                }
                case 2: {
                    message = "[Road Detector] Light unbound";
                    break;
                }
                case 3: {
                    message = "[Road Detector] Light bound";
                    break;
                }
                default: {
                    message = "";
                    break;
                }
            }
            if (!world.isRemote) JesRoads2.handlerOverlay.getMessage().addMessage(message);
            return true;
        }
        return false;
    }

    @Override
    public boolean isCompatibleBlock(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        return block instanceof BlockIntersectionController || block instanceof BlockTrafficlight || block instanceof BlockGateController;
    }

    @Override
    protected BlockBase getReplacementBlock(Block b) {
        return JesRoads2.blocks.road_detector;
    }

    @Override
    public boolean canReplace(Block b) {
        return false;
    }

    @Override
    protected void setTileEntity(World world, BlockPos pos, TileEntity tile) {
        if (tile instanceof TileEntityRoadDetector)
            world.setTileEntity(pos, new TileEntityRoadDetector(((TileEntityRoadDetector) tile).getNBT()));
    }
}