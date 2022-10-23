package mod.jesroads2.block.road;

import java.util.List;
import java.util.Random;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.BlockBase;
import mod.jesroads2.block.BlockBaseHorizontal;
import mod.jesroads2.tileentity.TileEntityAgeble;
import mod.jesroads2.util.IBlockSwitchable;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockRoad extends BlockBaseHorizontal implements ITileEntityProvider, IBlockSwitchable {
    public enum EnumRoadType implements IRoadType {
        BLANK(0, "road"),
        WHITE_SIDE(1, "road_whiteside"),
        WHITE_MIDDLE(2, "road_whitemiddle"),
        WHITE_DIAGONAL(3, "road_whitediagonal"),
        WHITE_DIAGONAL_MIDDLE(4, "road_whitediagonalmiddle"),
        YELLOW_SIDE(5, "road_yellowside"),
        YELLOW_DIAGONAL(6, "road_yellowdiagonal"),
        ARROW_LEFT(7, "road_arrowleft"),
        ARROW_UP(8, "road_arrowup"),
        ARROW_RIGHT(9, "road_arrowright");

        public final int id;
        public final String name;

        EnumRoadType(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public static EnumRoadType fromID(int id) {
            EnumRoadType[] values = EnumRoadType.values();
            if (id > 0 && id < values.length) return values[id];
            else return values[0];
        }

        @Override
        public int getID() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    public final IRoadType roadType;

    public static final PropertyBool shiny = PropertyBool.create("road_aging"), checked = PropertyBool.create("check"), base = PropertyBool.create("foundation");

    public BlockRoad(int id, IRoadType type, String name) {
        super(id, new Material(MapColor.BLACK), name, JesRoads2.tabs.road);

        roadType = type;
        setHardness(0.5F).setResistance(1.F).setLightOpacity(255);
        setSoundType(SoundType.STONE);
        setTickRandomly(true);
        setDefaultState(getDefaultState().withProperty(shiny, false)
                .withProperty(base, true).withProperty(checked, false));
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityAgeble(world);
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        if (state.getValue(shiny)) return createNewTileEntity(world, 0);
        else return null;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.withProperty(base, !BlockBase.isNatural(world.getBlockState(pos.down()).getBlock()));
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        super.updateTick(world, pos, state, rand);
        if (!world.isRemote && !state.getValue(checked) && state.getValue(shiny)) {
            TileEntityAgeble age = getTileEntity(world, pos, TileEntityAgeble.class);
            if (age != null) {
                boolean game = JesRoads2.options.roads.use_gametime;
                if (age.shouldAge(game, game ? JesRoads2.options.roads.gametime : JesRoads2.options.roads.realtime)) {
                    age.onAging();
                    world.removeTileEntity(pos);
                    world.setBlockState(pos, state.withProperty(shiny, false).withProperty(checked, true), 2);
                    return;
                }
            }
        }
        world.setBlockState(pos, state.withProperty(checked, true), 2);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return super.getMetaFromState(state) | ((state.getValue(shiny) ? 1 : 0) << 2);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return super.getStateFromMeta(meta).withProperty(shiny, ((meta & 4) >> 2) != 0);
    }

    @Override
    protected void createProperties(List<IProperty<?>> properties) {
        super.createProperties(properties);
        properties.add(checked);
        properties.add(shiny);
        properties.add(base);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighbor, BlockPos npos) {
        if (world.getBlockState(npos).getBlock() == Blocks.SNOW_LAYER) world.setBlockToAir(npos);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        if (target.sideHit == EnumFacing.DOWN) return new ItemStack(JesRoads2.blocks.concrete);
        else return new ItemStack(JesRoads2.blocks.road[roadType.getID()]);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entity, ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!(hitY == 1.F || hitY == 0.5F)) return false;

        if (!world.isRemote && !isEmpty(stack)) {
            Block b = Block.getBlockFromItem(stack.getItem());
            if (canReplace(b) && b instanceof BlockRoad) {
                TileEntity tile = hasTileEntity ? world.getTileEntity(pos) : null;
                EnumFacing face = this instanceof BlockRoadSlope ? state.getValue(facing) : entity.getHorizontalFacing();
                world.setBlockState(pos, getReplacementBlock(b).getDefaultState().withProperty(facing, face).withProperty(shiny, state.getValue(shiny)), 2);
                setTileEntity(world, pos, tile);
                return true;
            } else if (state.getValue(shiny) && b == Blocks.DIAMOND_BLOCK) {
                TileEntityAgeble age = getTileEntity(world, pos, TileEntityAgeble.class);
                if (age != null) {
                    age.onAging();
                    world.removeTileEntity(pos);
                    world.setBlockState(pos, state.withProperty(shiny, false), 2);
                    return true;
                }
            }
        } else if (shouldSetBase(world, pos)) {
            if (!world.isRemote) world.setBlockState(pos.down(), JesRoads2.blocks.roadbase.getDefaultState(), 3);
            return true;
        } else if (state.getValue(shiny)) {
            if (!world.isRemote) {
                TileEntityAgeble age = getTileEntity(world, pos, TileEntityAgeble.class);
                if (age != null)
                    JesRoads2.handlerOverlay.getMessage().addMessage(age.getAgeDisplay(JesRoads2.options.roads.use_gametime));
            }
            return true;
        }
        return false;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing enfacing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase entity) {
        if (needsBaseBlock(world, pos))
            world.setBlockState(pos.down(), JesRoads2.blocks.roadbase.getDefaultState(), 3);
        return super.getStateForPlacement(world, pos, enfacing, hitX, hitY, hitZ, meta, entity).withProperty(shiny, !entity.isSneaking());
    }

    public static boolean needsBaseBlock(World world, BlockPos pos) {
        return world.isBlockFullCube(pos.down(2)) && shouldSetBase(world, pos);
    }

    private static boolean shouldSetBase(IBlockAccess world, BlockPos p) {
        Block b = world.getBlockState(p.down()).getBlock();
        return b != JesRoads2.blocks.concrete && b != JesRoads2.blocks.roadbase && b != Blocks.AIR;
    }

    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {
        if (entity instanceof EntityPlayer) {
            JesRoads2.handlerOverlay.getOverlay().enableSpeedDisplay(entity);
            EntityPlayer player = (EntityPlayer) entity;
            ItemStack stack = player.getHeldItemMainhand();
            if (Block.getBlockFromItem(stack.getItem()) == Blocks.AIR) {
                double boost;
                if (player.isSprinting()) boost = JesRoads2.options.roads.speed_boost1;
                else boost = JesRoads2.options.roads.speed_boost0;

                player.motionX *= boost;
                player.motionZ *= boost;
            } else {
                if (player.isSprinting()) {
                    player.motionX *= JesRoads2.options.roads.speed_boost2;
                    player.motionZ *= JesRoads2.options.roads.speed_boost2;
                }
            }
        }
    }

    protected BlockBase getReplacementBlock(Block b) {
        if (!(b instanceof BlockRoad)) return JesRoads2.blocks.road[0];
        int id = ((BlockRoad) b).roadType.getID();

        if (id < 0) id = 0;
        else if (id > JesRoads2.blocks.road.length) id = JesRoads2.blocks.road.length;

        return JesRoads2.blocks.road[id];
    }

    protected boolean canReplace(Block b) {
        return !(b instanceof BlockRoadDetector || b instanceof BlockRoadSlope);
    }

    protected void setTileEntity(World world, BlockPos pos, TileEntity old) {
    }

    @Override
    public void onBlockDestroyedByExplosion(World world, BlockPos pos, Explosion explosion){
        dropBlockAsItem(world, pos, world.getBlockState(pos), 0);
    }

    @Override
    public ItemStack getSwitchBlock(ItemStack current) {
        if (roadType == null) return null;

        switch (roadType.getID()) {
            case 0:
            case 1:
            case 2:
                return new ItemStack(JesRoads2.blocks.roadslope[roadType.getID()]);
            case 5:
                return new ItemStack(JesRoads2.blocks.roadslope[3]);
            default:
                return null;
        }
    }
}