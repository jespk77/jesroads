package mod.jesroads2.item;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.BlockBase;
import mod.jesroads2.block.road.BlockRoad;
import mod.jesroads2.block.road.BlockRoad.EnumRoadType;
import mod.jesroads2.client.PlayerReachController;
import mod.jesroads2.client.gui.GuiRoadBuilder;
import mod.jesroads2.util.EnumFacingDiagonal;
import mod.jesroads2.util.IBlockSwitchable;
import mod.jesroads2.util.LimitedStack;
import mod.jesroads2.util.NBTUtils;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemRoadBuilder extends ItemBase implements IItemCustomHighlightRenderer, IBlockSwitchable, IItemVariant {
    public enum EnumRoadBuilderMode {
        AUTOMATIC(0, true, false),
        OFF(1, false, true),
        NO_SLOPE(2, true, false),
        REMOVE(3, false, true);

        public final int id;
        public final boolean shapeTerrain, locked;

        private static final EnumRoadBuilderMode[] values = new EnumRoadBuilderMode[values().length];

        static {
            for (EnumRoadBuilderMode mode : values())
                values[mode.id] = mode;
        }

        public static EnumRoadBuilderMode fromID(int id) {
            if (id > 0 && id < values.length) return values[id];
            else if (id == -1) return values[values.length - 1];
            else return values[0];
        }

        EnumRoadBuilderMode(int id, boolean shapeTerrain, boolean locked) {
            this.id = id;
            this.shapeTerrain = shapeTerrain;
            this.locked = locked;
        }
    }

    private static final BlockBase[]
            LANE_DIAGONAL_LINE = new BlockBase[]{
            getBlocksFromType(EnumRoadType.BLANK), getBlocksFromType(EnumRoadType.BLANK),
            getBlocksFromType(EnumRoadType.BLANK), getBlocksFromType(EnumRoadType.WHITE_DIAGONAL_MIDDLE)
    },
            LANE_DIAGONAL = new BlockBase[]{
                    getBlocksFromType(EnumRoadType.BLANK), getBlocksFromType(EnumRoadType.BLANK),
                    getBlocksFromType(EnumRoadType.BLANK), getBlocksFromType(EnumRoadType.BLANK)
            },
            LANE_LINE = new BlockBase[]{
                    getBlocksFromType(EnumRoadType.BLANK), getBlocksFromType(EnumRoadType.BLANK),
                    getBlocksFromType(EnumRoadType.WHITE_MIDDLE)
            },
            LANE = new BlockBase[]{
                    getBlocksFromType(EnumRoadType.BLANK), getBlocksFromType(EnumRoadType.BLANK),
                    getBlocksFromType(EnumRoadType.BLANK)
            };

    private final LimitedStack<BuilderAction> actionStack;
    private static final int teleport_distance = 3;

    public static final String nbt_name = "roadbuilder_data";

    public ItemRoadBuilder(int id) {
        super(id, "road_builder", JesRoads2.tabs.road_extra);

        actionStack = new LimitedStack<>(55);
        setMaxDamage(0).setMaxStackSize(1);
        addPropertyOverride(new ResourceLocation("type"), (stack, worldIn, entityIn) -> {
            NBTTagCompound nbt = stack.getSubCompound(nbt_name);
            if (nbt != null) return nbt.getInteger("placemode") > 0 ? 1 : 0;
            else return 0;
        });
    }

    public void clearHistory() {
        actionStack.clear();
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        if (entity instanceof EntityAnimal) entity.setDead();
        return true;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (selected) {
            boolean shouldFocus = true;
            if (stack.hasTagCompound()){
                NBTTagCompound nbt = stack.getSubCompound(nbt_name);
                if(nbt != null && nbt.getInteger("placemode") != 0) shouldFocus = false;
            }

            //System.out.println(shouldFocus);
            PlayerReachController.setFocused(Minecraft.getMinecraft(), (EntityPlayer) entity, shouldFocus);
            if (!world.isRemote && entity.isSneaking()) {
                BlockPos pos = entity.getPosition().down();
                speedGrowth(world, pos);
            }
        } else {
            NBTTagCompound nbt = stack.getSubCompound(nbt_name);
            if (nbt != null && (nbt.getInteger("right_length") != 0 || nbt.getInteger("road_length") != 0 || nbt.getInteger("left_length") != 0))
                nbt.setInteger("placemode", 0);
            PlayerReachController.setFocused(Minecraft.getMinecraft(), (EntityPlayer) entity, false);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean drawBlockHighlight(EntityPlayer player, ItemStack stack, RayTraceResult res, float partialTicks) {
        if (GuiScreen.isShiftKeyDown()) return true;

        double offsetX = (player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTicks),
                offsetY = (player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTicks),
                offsetZ = (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTicks);

        BlockPos pos = res.getBlockPos();
        World world = Minecraft.getMinecraft().world;
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof BlockLog) {
            BlockPos down = pos.down();
            RenderGlobal.drawSelectionBoundingBox(state.getBoundingBox(world, down).grow(0.01D).offset(down).offset(-offsetX, -offsetY, -offsetZ), 0.5F, 0.3F, 0.F, 1.F);
            return true;
        }

        IBlockState x = world.getBlockState(pos);
        while (!x.isBlockNormalCube() && !(x.getBlock() instanceof BlockRoad)) {
            pos = pos.down();
            x = world.getBlockState(pos);
        }

        if (JesRoads2.EnumKeyBindings.BUILDER_REPLACE.getBind().isKeyDown()) {
            Iterator<BlockPos> blocks = getBlocksInRange(pos);
            while (blocks.hasNext()) {
                BlockPos p = world.getTopSolidOrLiquidBlock(blocks.next());
                while (!world.isBlockFullCube(p)) p = p.down();
                IBlockState s = world.getBlockState(p);
                if (canReplace(s.getBlock()))
                    RenderGlobal.drawSelectionBoundingBox(s.getBoundingBox(world, p).grow(0.01D).offset(p).offset(-offsetX, -offsetY, -offsetZ), 0.F, 0.F, 0.F, 1.F);
            }
            return true;
        }

        offsetX = pos.getX() - offsetX + 0.01D;
        offsetY = pos.getY() - offsetY + 0.01D;
        offsetZ = pos.getZ() - offsetZ + 0.01D;

        NBTTagCompound nbt = stack.getSubCompound(nbt_name);
        if (nbt == null || nbt.getInteger("builder_mode") == EnumRoadBuilderMode.REMOVE.id || nbt.getInteger("placemode") > 0)
            return false;

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GlStateManager.glLineWidth(5.F);

        EnumFacingDiagonal direction = EnumFacingDiagonal.fromEntityF(player);
        boolean diagonal = direction.isDiagonal();
        EnumFacing facing = diagonal ? direction.getFacing().rotateY() : direction.getFacing();

        int terrain_left = nbt.getInteger("terrain_left"), right_length = nbt.getInteger("right_length"), lanes = nbt.getInteger("road_length"),
                road_length = nbt.getInteger("left_length") + (diagonal ? (lanes > 0 ? 5 : 0) : 3 * lanes) + right_length,
                terrain_right = nbt.getInteger("terrain_right");
        if (diagonal) {
            if (right_length > 1) road_length += 1;
            if (terrain_right > 1) terrain_right += 1;
        }

        AxisAlignedBB left = null, road = null;
        double minX = 0.D, Y = 1.D, minZ = 0.D, maxX = 1.D, maxZ = 1.D;
        if (terrain_left > 0) {
            left = createBoundingBox(null, facing, terrain_left, 0, offsetX, offsetY, offsetZ);
            RenderGlobal.drawSelectionBoundingBox(left, 0.F, 1.F, 0.F, 1.F);
        }

        if (road_length > 0) {
            road = createBoundingBox(left, facing, road_length, terrain_left, offsetX, offsetY, offsetZ);
            RenderGlobal.drawSelectionBoundingBox(road, 0.2F, 0.2F, 0.2F, 1.F);
        }

        if (terrain_right > 0)
            RenderGlobal.drawSelectionBoundingBox(createBoundingBox(road, facing, terrain_right, road_length, offsetX, offsetY, offsetZ), 0.F, 1.F, 0.F, 1.F);

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        return true;
    }

    private static AxisAlignedBB createBoundingBox(AxisAlignedBB base, EnumFacing facing, int length, int prevLength, double offsetX, double offsetY, double offsetZ) {
        double minX, minY, minZ, maxX, maxY, maxZ;
        if (base != null) {
            if (facing.getAxis() == EnumFacing.Axis.X) {
                minX = base.minX;
                maxX = base.maxX;
                if (facing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) minZ = base.maxZ - prevLength - 1;
                else minZ = base.maxZ;
                maxZ = minZ + 1.D;
            } else {
                if (facing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE) minX = base.maxX - prevLength - 1;
                else minX = base.maxX;
                maxX = minX + 1.D;
                minZ = base.minZ;
                maxZ = base.maxZ;
            }
            minY = base.minY;
            maxY = base.maxY;
            offsetX = 0;
            offsetY = 0;
            offsetZ = 0;
        } else {
            minX = 0.D;
            maxX = 1.D;
            minY = 0.D;
            maxY = 1.D;
            minZ = 0.D;
            maxZ = 1.D;
        }

        switch (facing) {
            case NORTH: {
                maxX += length - 1;
                break;
            }
            case SOUTH: {
                minX += 1.D;
                maxX += -length;
                break;
            }
            case EAST: {
                maxZ += length - 1;
                break;
            }
            case WEST: {
                minZ += 1.D;
                maxZ += -length;
                break;
            }
            default:
                break;
        }
        return new AxisAlignedBB(minX + offsetX, minY + offsetY, minZ + offsetZ, maxX + offsetX, maxY + offsetY, maxZ + offsetZ);
    }

    private static void speedGrowth(World world, BlockPos pos) {
        if (world.getGameRules().getInt("randomTickSpeed") > 0) {
            Iterator<BlockPos> neighbors = getBlocksInRange(pos);
            while (neighbors.hasNext()) {
                BlockPos top = world.getTopSolidOrLiquidBlock(neighbors.next()), down = top.down();
                Block btop = world.getBlockState(top).getBlock(), bdown = world.getBlockState(down).getBlock();
                if (btop == Blocks.WATER) continue;
                world.scheduleUpdate(top, btop, 1);
                world.scheduleUpdate(down, bdown, 1);
            }
        }
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World world, IBlockState state, BlockPos pos, EntityLivingBase entity) {
        if (state.getBlock() instanceof BlockRoad) return false;
        else if (world.isRemote) return true;

        if (!shouldRemove(state)) {
            NBTTagCompound nbt = stack.getSubCompound(nbt_name);
            if (nbt != null) {
                BuilderAction action = new BuilderAction(stack);
                switch (nbt.getInteger("placemode")) {
                    case 0: {
                        levelTerrain(action, world, pos, Blocks.AIR.getDefaultState(), true);
                        break;
                    }

                    case 1:
                    case 2: {
                        setAroundPos(action, world, Blocks.AIR.getDefaultState(), pos, null, state.getBlock(), EnumFacing.getDirectionFromEntityLiving(pos, entity), 0, 2);
                        break;
                    }
                }
                actionStack.push(action);
            }
            return true;
        } else return removeBlocks(world, pos, 0);
    }

    private static boolean removeBlocks(World world, BlockPos pos, int count) {
        if (count > JesRoads2.options.road_builder.limit_log_remove) return false;

        if (shouldRemove(world.getBlockState(pos))) {
            world.destroyBlock(pos, false);

            removeSurrounding(world, pos, count);
            removeSurrounding(world, pos.up(), count);
            removeSurrounding(world, pos.down(), count);

            removeBlocks(world, pos.east(), count + 1);
            removeBlocks(world, pos.west(), count + 1);
            return true;
        } else return false;
    }

    private static void removeSurrounding(World world, BlockPos pos, int count) {
        removeBlocks(world, pos, count + 1);
        BlockPos north = pos.north(), south = pos.south();
        removeBlocks(world, north, count + 1);
        removeBlocks(world, north.west(), count + 1);
        removeBlocks(world, north.east(), count + 1);

        removeBlocks(world, south, count + 1);
        removeBlocks(world, south.west(), count + 1);
        removeBlocks(world, south.east(), count + 1);
    }

    private static boolean shouldRemove(IBlockState state) {
        Block block = state.getBlock();
        return block instanceof BlockLog
                || block instanceof BlockHugeMushroom;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
        NBTTagCompound nbt = getTagOrSetDefault(stack);

        switch (nbt.getInteger("placemode")) {
            case 1: {
                tooltip.add("Placement mode");
                if (nbt.getBoolean("use_relative")) tooltip.add("Using relative block");
                else tooltip.add("Using hotbar slot " + nbt.getInteger("selected_block"));
                break;
            }

            case 2: {
                tooltip.add("Replacement mode");
                tooltip.add("Using hotbar slot " + nbt.getInteger("selected_block"));
                break;
            }

            default: {
                if (GuiScreen.isShiftKeyDown()) {
                    EnumRoadBuilderMode mode = EnumRoadBuilderMode.fromID(nbt.getInteger("builder_mode"));
                    tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("iteminfo.road_builder.mode_" + mode.name().toLowerCase()));
                    if (mode != EnumRoadBuilderMode.REMOVE) {
                        tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("iteminfo.road_builder.road_data"));
                        tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("iteminfo.road_builder.left_length") + ": " + nbt.getInteger("left_length") + " blocks");
                        tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("iteminfo.road_builder.road_length") + ": " + nbt.getInteger("road_length") + " blocks");
                        tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("iteminfo.road_builder.right_length") + ": " + nbt.getInteger("right_length") + " blocks");

                        tooltip.add("");
                        tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("iteminfo.road_builder.terrain_data"));
                        tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("iteminfo.road_builder.terrain_left") + ": " + nbt.getInteger("terrain_left") + " blocks");
                        tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("iteminfo.road_builder.terrain_right") + ": " + nbt.getInteger("terrain_right") + " blocks");
                    } else
                        tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("iteminfo.road_builder.remove_length") + nbt.getInteger("remove_length") + " blocks");
                } else tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("iteminfo.road_builder.view_data"));
                break;
            }
        }
    }

    public void undoLastAction(World world, ItemStack stack) {
        if (!world.isRemote && !actionStack.isEmpty()) {
            actionStack.pop().undoAction(world);
        }
    }

    public static NBTTagCompound getTagOrSetDefault(ItemStack stack) {
        NBTTagCompound nbt = stack.getOrCreateSubCompound(nbt_name);
        if (!nbt.hasKey("remove_length")) nbt.setInteger("remove_length", JesRoads2.options.road_builder.max_remove);
        if (!nbt.hasKey("use_relative")) nbt.setBoolean("use_relative", true);
        return nbt;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking()) {
            if (world.isRemote) player.openGui(JesRoads2.instance, GuiRoadBuilder.ID, world, -1, -1, -1);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        } else if (GuiScreen.isAltKeyDown()) {
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        if (stack.hasTagCompound()) {
            NBTTagCompound nbt = stack.getSubCompound(nbt_name);
            BlockPos lastPlacementPos = nbt != null ? NBTUtils.readBlockPos(nbt.getCompoundTag("place_location")) : null;

            if (lastPlacementPos != null && world.isBlockLoaded(lastPlacementPos)) {
                EnumFacingDiagonal facing = EnumFacingDiagonal.fromEntityF(player);
                if (player.rotationPitch > 65.f) {
                    if (!world.isRemote) nbt.setTag("place_location", NBTUtils.writeBlockPos(lastPlacementPos.down()));
                    return new ActionResult<>(EnumActionResult.SUCCESS, stack);
                } else if (player.rotationPitch < -65.f) {
                    if (!world.isRemote) nbt.setTag("place_location", NBTUtils.writeBlockPos(lastPlacementPos.up()));
                    return new ActionResult<>(EnumActionResult.SUCCESS, stack);
                } else if (player.isSneaking()) {
                    if (!world.isRemote) nbt.setTag("place_location", NBTUtils.writeBlockPos(lastPlacementPos.offset(player.getHorizontalFacing())));
                    return new ActionResult<>(EnumActionResult.SUCCESS, stack);
                }

                EnumFacing f = facing.getFacing();
                BlockPos newPlacementPos = lastPlacementPos.offset(GuiScreen.isCtrlKeyDown() ? f.getOpposite() : f);
                if (facing.isDiagonal()) newPlacementPos = newPlacementPos.offset(GuiScreen.isCtrlKeyDown() ? f.rotateY().getOpposite() : f.rotateY());
                nbt.setTag("place_location", NBTUtils.writeBlockPos(newPlacementPos));
                EnumActionResult res = onItemUse(player, world, newPlacementPos, hand, facing.getFacing(), -1, -1, -1);
                return new ActionResult<>(res, stack);
            }
        }
        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    public static void teleportPlayer(EntityPlayer player) {
        Vec3d look = player.getLookVec();
        EnumFacing direction = EnumFacing.getFacingFromVector((float) look.x, (float) look.y, (float) look.z);
        double x = player.posX, y = player.posY, z = player.posZ;
        switch (direction.getAxis()) {
            case X: {
                x += direction.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? teleport_distance : -teleport_distance;
                break;
            }
            case Y: {
                y += direction.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? teleport_distance : -teleport_distance;
                break;
            }
            case Z: {
                z += direction.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? teleport_distance : -teleport_distance;
                break;
            }
        }
        player.setPositionAndUpdate(x, y, z);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer entity, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (GuiScreen.isShiftKeyDown()) {
            if (world.isRemote)
                entity.openGui(JesRoads2.instance, GuiRoadBuilder.ID, world, pos.getX(), pos.getY(), pos.getZ());
            return EnumActionResult.SUCCESS;
        }

        boolean manual = hitX >= 0;
        IBlockState state = world.getBlockState(pos);
        if (manual) {
            while (!state.isBlockNormalCube() && !(state.getBlock() instanceof BlockRoad)) {
                pos = pos.down();
                state = world.getBlockState(pos);
            }
        }

        ItemStack stack = entity.getHeldItem(hand);
        if (!stack.hasTagCompound()) return EnumActionResult.FAIL;
        else if (world.isRemote) return EnumActionResult.SUCCESS;

        if (state.getBlock() instanceof BlockLog) {
            setBlockState(null, world, pos.down(), state, 3);
            return EnumActionResult.SUCCESS;
        }

        NBTTagCompound nbt = stack.getSubCompound(nbt_name);
        int slot = nbt.getInteger("selected_slot");
        ItemStack item;
        IBlockState set;
        int placement = nbt.getInteger("placemode");
        if (placement != 2 && nbt.getBoolean("use_relative")) {
            item = null;
            set = null;
        } else {
            item = ItemRoadBuilder.getPlayerInventorySlot(entity, slot);
            set = Block.getBlockFromItem(item.getItem()).getStateFromMeta(item.getItemDamage());
        }

        int index = nbt.getInteger("index");
        BuilderAction action = new BuilderAction(stack, index);
        if (manual && JesRoads2.EnumKeyBindings.BUILDER_REPLACE.getBind().isKeyDown()) {
            Iterator<BlockPos> replacing = getBlocksInRange(pos);
            if (item == null) item = getPlayerInventorySlot(entity, entity.inventory.currentItem + 1);
            Block b = Block.getBlockFromItem(item.getItem());
            IBlockState bottom = b.getDefaultState(), top = b.getStateFromMeta(item.getItemDamage());
            while (replacing.hasNext()) {
                BlockPos p = world.getTopSolidOrLiquidBlock(replacing.next());
                while (!world.isBlockNormalCube(p, true)) p = p.down();
                BlockPos pd = p.down();

                Block bt = world.getBlockState(p).getBlock(), bb = world.getBlockState(pd).getBlock();
                if (canReplace(bt) && bt != b) setBlockState(action, world, p, top, 3);
                if (canReplace(bb) && bb != b) setBlockState(action, world, pd, bottom, 3);
            }
            actionStack.push(action);
            return EnumActionResult.SUCCESS;
        }

        if (manual && placement > 0) {
            Block b = state.getBlock();
            if (placement == 2) {
                if (set != null) state = set;
                else if (item != null)
                    state = Block.getBlockFromItem(item.getItem()).getStateFromMeta(item.getItemDamage());
                else state = Blocks.DIRT.getDefaultState();
            } else if (set != null) state = set;

            if (state != null) {
                if (state.getBlock() == Blocks.GRASS) state = Blocks.DIRT.getDefaultState();
                setAroundPos(action, world, state, pos, null, b, side, 0, placement);
                actionStack.push(action);
                return EnumActionResult.SUCCESS;
            }
            return EnumActionResult.PASS;
        } else nbt.setInteger("placemode", 0);

        MutableBlockPos ps = new MutableBlockPos(pos);
        EnumFacingDiagonal face = EnumFacingDiagonal.fromEntityF(entity);
        EnumRoadBuilderMode mode = EnumRoadBuilderMode.fromID(nbt.getInteger("builder_mode"));
        boolean terrain = mode.shapeTerrain && state.getBlock() != JesRoads2.blocks.concrete && !GuiScreen.isAltKeyDown();
        boolean last = false;

        if (mode == EnumRoadBuilderMode.REMOVE) {
            int remove = nbt.getInteger("remove_length");
            for (int i = 0; i < remove; i++) {
                if (world.getBlockState(ps).getBlock() instanceof BlockRoad) destroyBlock(null, world, ps);
                else break;
                ps.move(getRight(face.getFacing()));
            }
            return EnumActionResult.SUCCESS;
        }

        if (manual) nbt.setTag("place_location", NBTUtils.writeBlockPos(pos));
        int left = nbt.getInteger("left_length"), road = nbt.getInteger("road_length"), right = nbt.getInteger("right_length"),
                t_left = nbt.getInteger("terrain_left"), t_right = nbt.getInteger("terrain_right");
        BlockBase[] blocks = getBlocksFromNumberLanes(road, face.isDiagonal(), index, !terrain || right > 0);
        left = Math.min(left, JesRoads2.options.road_builder.max_shoulder);
        right = right >= JesRoads2.options.road_builder.max_shoulder ? JesRoads2.options.road_builder.max_shoulder : face.isDiagonal() && right > 1 ? right + 1 : right;
        t_left = Math.min(t_left, JesRoads2.options.road_builder.max_terrain);
        t_right = t_right >= JesRoads2.options.road_builder.max_terrain ? JesRoads2.options.road_builder.max_terrain : face.isDiagonal() && t_right > 1 ? t_right + 1 : t_right;

        for (int i = 0; i < t_left; i++) {
            if (levelTerrain(action, world, ps, set, terrain) && !last && !mode.locked) {
                terrain = !terrain;
                last = true;
            } else last = false;

            updatePos(face, ps);
        }

        EnumFacing blockFace = face.getFacing();
        int rd = 0;
        for (int i = 0; i < (left + blocks.length + right); i++) {
            boolean isrd = world.getBlockState(ps.offset(face.getFacing().rotateYCCW())).getBlock() instanceof BlockRoad;
            if (isrd) rd++;
            else if (rd > 1) {
                if (t_right > 1) t_right--;
                break;
            }

            if (levelTerrain(action, world, ps, set, terrain) && !last && !mode.locked) {
                terrain = !terrain;
                last = true;
            } else last = false;

            world.setBlockToAir(ps.up());
            if (BlockRoad.needsBaseBlock(world, ps))
                setBlockState(action, world, ps.down(), JesRoads2.blocks.roadbase.getDefaultState(), 3, false);

            if (i < left || i >= (left + blocks.length))
                setBlockState(action, world, ps, JesRoads2.blocks.road[0].getDefaultState().withProperty(BlockRoad.facing, blockFace), 3);
            else
                setBlockState(action, world, ps, blocks[i - left].getDefaultState().withProperty(BlockRoad.facing, blockFace), 3);
            updatePos(face, ps);
        }

        for (int i = 0; i < t_right; i++) {
            if (levelTerrain(action, world, ps.toImmutable(), set, terrain) && !last && !mode.locked) {
                terrain = !terrain;
                last = true;
            } else last = false;

            if (!terrain && i == t_right - 1)
                setBlockState(action, world, ps.up(), Blocks.IRON_BARS.getDefaultState(), 3, false);
            updatePos(face, ps);
        }

        if (mode != EnumRoadBuilderMode.NO_SLOPE && t_right > 0 && terrain)
            slopeTerrain(action, world, face, pos, ps, set, terrain);

        nbt.setInteger("index", updateIndex(index));
        actionStack.push(action);
        return EnumActionResult.SUCCESS;
    }

    private static int setAroundPos(BuilderAction action, World world, IBlockState set, BlockPos pos, EnumFacing dir, Block b, EnumFacing side, int count, int placemode) {
        if (count >= JesRoads2.options.road_builder.limit_place) return count;

        if (dir != null) {
            pos = pos.offset(dir);
            dir = dir.getOpposite();
        }
        BlockPos place = pos.offset(side);
        if (!areBlocksEqual(b, world.getBlockState(pos).getBlock()) || world.isBlockFullCube(place)) return count;

        setBlockState(action, world, placemode == 1 ? place : pos, set, 3);
        count++;
        EnumFacing left, right, up, down;
        switch (side.getAxis()) {
            case Z: {
                left = EnumFacing.WEST;
                right = EnumFacing.EAST;
                up = EnumFacing.UP;
                down = EnumFacing.DOWN;
                break;
            }
            case X: {
                left = EnumFacing.NORTH;
                right = EnumFacing.SOUTH;
                up = EnumFacing.UP;
                down = EnumFacing.DOWN;
                break;
            }
            default: {
                left = EnumFacing.NORTH;
                right = EnumFacing.SOUTH;
                up = EnumFacing.WEST;
                down = EnumFacing.EAST;
                break;
            }
        }

        if (!GuiScreen.isCtrlKeyDown() && left != dir)
            count = setAroundPos(action, world, set, pos, left, b, side, count, placemode);
        if (!GuiScreen.isCtrlKeyDown() && right != dir)
            count = setAroundPos(action, world, set, pos, right, b, side, count, placemode);
        if (!GuiScreen.isAltKeyDown() && up != dir)
            count = setAroundPos(action, world, set, pos, up, b, side, count, placemode);
        if (!GuiScreen.isAltKeyDown() && down != dir)
            count = setAroundPos(action, world, set, pos, down, b, side, count, placemode);
        return count;
    }

    private static boolean areBlocksEqual(Block b1, Block b2) {
        if ((b1 == Blocks.DIRT || b1 == Blocks.GRASS) && (b2 == Blocks.DIRT || b2 == Blocks.GRASS)) return true;
        else return b1 == b2;
    }

    private static boolean canReplace(Block b) {
        return (!(b instanceof ITileEntityProvider)) && !(b instanceof BlockRoad) && !(b instanceof BlockLog) && b != Blocks.GRASS && b != JesRoads2.blocks.concrete;
    }


    private static boolean setBlockState(BuilderAction action, World world, BlockPos pos, IBlockState state, int flags) {
        return setBlockState(action, world, pos, state, flags, true);
    }

    private static boolean setBlockState(BuilderAction action, World world, BlockPos pos, IBlockState state, int flags, boolean playSound) {
        pos = pos.toImmutable();
        if (action != null) action.addAction(pos, world.getBlockState(pos), world.getTileEntity(pos));
        Block block = state.getBlock();
        if (playSound) {
            SoundType sound = block.getSoundType(state, world, pos, null);
            world.playSound(null, pos, sound.getPlaceSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
        }
        return world.setBlockState(pos, state, flags);
    }

    private static boolean destroyBlock(BuilderAction action, World world, BlockPos pos) {
        pos = pos.toImmutable();
        if (action != null) action.addAction(pos, world.getBlockState(pos), world.getTileEntity(pos));
        return world.destroyBlock(pos, false);
    }

    private static Iterator<BlockPos> getBlocksInRange(BlockPos pos) {
        return BlockPos.getAllInBox(new BlockPos(pos.getX() - JesRoads2.options.road_builder.limit_replace, 0, pos.getZ() - JesRoads2.options.road_builder.limit_replace),
                new BlockPos(pos.getX() + JesRoads2.options.road_builder.limit_replace, 0, pos.getZ() + JesRoads2.options.road_builder.limit_replace)).iterator();
    }

    public static ItemStack getPlayerInventorySlot(EntityPlayer player, int slot) {
        slot = Math.floorMod(slot, 9);
        ItemStack stack = player.inventory.getStackInSlot(slot);
        IBlockState s = Block.getBlockFromItem(stack.getItem()).getStateFromMeta(stack.getItemDamage());
        if (s.getBlock() == Blocks.AIR) return new ItemStack(Blocks.DIRT);
        else return stack;
    }

    public static boolean isRemovalMode(ItemStack stack) {
        NBTTagCompound nbt = stack.getSubCompound(nbt_name);
        return nbt != null ? nbt.getInteger("remove_length") > 0 : false;
    }

    protected static int updateIndex(int index) {
        return (index + 1) % 5;
    }

    protected static MutableBlockPos updatePos(EnumFacingDiagonal face, MutableBlockPos pos) {
        return face.isDiagonal() ? pos.move(face.getFacing().getOpposite()) : pos.move(getRight(face.getFacing()));
    }

    protected static EnumFacing getRight(EnumFacing facing) {
        return facing.rotateY();
    }

    private static BlockBase[] getBlocksFromNumberLanes(int lanes, boolean diagonal, int index, boolean side) {
        if (lanes <= 0) return new BlockBase[]{};
        else if (lanes > JesRoads2.options.road_builder.max_lane) lanes = JesRoads2.options.road_builder.max_lane;

        BlockBase[] res;
        int pos;
        if (diagonal) {
            pos = 1;
            res = new BlockBase[(lanes * 5) - (lanes - 1)];
        } else {
            pos = 0;
            res = new BlockBase[lanes * 3];
        }

        for (int lane = 0; lane < lanes; lane++) {
            int lindex = diagonal ? (GuiScreen.isCtrlKeyDown() ? index + (2 * lane) : index - (2 * lane)) : index;
            BlockBase[] base = getBaseFromIndex(diagonal, Math.floorMod(lindex, 5));
            for (BlockBase b : base) {
                res[pos] = b;
                pos++;
            }
        }

        if (diagonal) {
            res[0] = getBlocksFromType(EnumRoadType.YELLOW_DIAGONAL);
            res[res.length - 1] = side ? getBlocksFromType(EnumRoadType.WHITE_DIAGONAL) : getBlocksFromType(EnumRoadType.BLANK);
        } else {
            res[0] = getBlocksFromType(EnumRoadType.YELLOW_SIDE);
            res[res.length - 1] = side ? getBlocksFromType(EnumRoadType.WHITE_SIDE) : getBlocksFromType(EnumRoadType.BLANK);
        }
        return res;
    }

    private static BlockBase[] getBaseFromIndex(boolean diagonal, int index) {
        if (diagonal) return index < 2 ? LANE_DIAGONAL_LINE : LANE_DIAGONAL;
        else return index < 2 ? LANE_LINE : LANE;
    }

    private static BlockBase getBlocksFromType(EnumRoadType type) {
        return JesRoads2.blocks.road[type.id];
    }

    private static void slopeTerrain(BuilderAction action, World world, EnumFacingDiagonal face, BlockPos pos, BlockPos level, IBlockState state, boolean terrain) {
        MutableBlockPos down = new MutableBlockPos(level).move(EnumFacing.DOWN), up = new MutableBlockPos(level).move(EnumFacing.UP);
        int count = 0;
        while (canFill(world, down) && count < JesRoads2.options.road_builder.limit_slope) {
            levelTerrain(action, world, down, state, terrain);
            updatePos(face, down).move(EnumFacing.DOWN);
            count++;
        }

        count = 0;
        while (canClear(world, up) && count < JesRoads2.options.road_builder.limit_slope) {
            levelTerrain(action, world, up, state, terrain);
            updatePos(face, up).move(EnumFacing.UP);
            count++;
        }
    }

    private static boolean levelTerrain(BuilderAction action, World world, BlockPos level, IBlockState fill, boolean terrain) {
        MutableBlockPos down = new MutableBlockPos(level), up = new MutableBlockPos(level).move(EnumFacing.UP);
        if (terrain) {
            if (world.isBlockNormalCube(level, false)) down.move(EnumFacing.DOWN);
            Block block;
            if (fill == null) {
                BlockPos source = world.getHeight(down).down();
                while (!world.isBlockNormalCube(source, false) || (world.getBlockState(source).getBlock() instanceof BlockLog))
                    source = source.down();
                fill = world.getBlockState(source);
                block = fill.getBlock();
                if (block == Blocks.GRASS) fill = Blocks.DIRT.getDefaultState();
            } else block = fill.getBlock();

            IBlockState base = block.getMetaFromState(fill) != 0 ? block.getStateFromMeta(0) : fill;
            int count = 0;
            while (canFill(world, down) && count < JesRoads2.options.road_builder.limit_level) {
                setBlockState(action, world, down, base, 3, false);
                down.move(EnumFacing.DOWN);
                count++;
            }

            count = 0;
            while (canClear(world, up) && count < JesRoads2.options.road_builder.limit_level) {
                destroyBlock(action, world, up);
                up.move(EnumFacing.UP);
                count++;
            }
            if (replaceSurface(world, level)) setBlockState(action, world, level, fill, 3, false);
        } else if (world.isAirBlock(level))
            setBlockState(action, world, level, JesRoads2.blocks.concrete.getDefaultState(), 3);
        return world.getBlockState(down.down()).getBlock() == JesRoads2.blocks.concrete;
    }

    private static boolean replaceSurface(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Block b = state.getBlock();
        if (b instanceof BlockRoad) return false;
        else if (b instanceof BlockGrass) return false;
        return true;
    }

    private static boolean canFill(World world, BlockPos pos) {
        return !world.getBlockState(pos).isBlockNormalCube();
    }

    private static boolean canClear(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof BlockLog || state.getBlock() instanceof BlockLeaves) return false;
        else return state.isBlockNormalCube();
    }

    private static class BuilderAction {
        private static class BuilderActionData {
            public final IBlockState state;
            public final NBTTagCompound nbt;

            public BuilderActionData(IBlockState state, NBTTagCompound nbt) {
                this.state = state;
                this.nbt = nbt;
            }
        }

        private final Map<BlockPos, BuilderActionData> actionMap;
        private final ItemStack itemStack;
        private final int itemIndex;

        public BuilderAction(ItemStack stack){
            this(stack, -1);
        }

        public BuilderAction(ItemStack stack, int index) {
            actionMap = new HashMap<>();
            itemStack = stack;
            itemIndex = index;
        }

        public boolean addAction(BlockPos pos, IBlockState state, TileEntity tile) {
            if (!actionMap.containsKey(pos)) {
                actionMap.put(pos, new BuilderActionData(state, tile != null ? tile.writeToNBT(new NBTTagCompound()) : null));
                return true;
            } else return false;
        }

        public void undoAction(World world) {
            for (Entry<BlockPos, BuilderActionData> entry : actionMap.entrySet()) {
                BlockPos pos = entry.getKey();
                BuilderActionData data = entry.getValue();
                world.setBlockState(pos, data.state, 2);
                if (data.nbt != null) world.setTileEntity(pos, TileEntity.create(world, data.nbt));
            }

            if (itemStack != null) {
                NBTTagCompound nbt = itemStack.getSubCompound(nbt_name);
                if(nbt != null && itemIndex >= 0) nbt.setInteger("index", itemIndex);
            }
        }
    }

    @Override
    public ItemStack getSwitchBlock(ItemStack current) {
        return null;
    }

    @Override
    public boolean updateStack(EntityPlayer player, ItemStack stack, int dwheel) {
        NBTTagCompound nbt = stack.getSubCompound(nbt_name);
        if (nbt == null || (nbt.getBoolean("use_relative") && nbt.getInteger("placemode") != 2)) return false;

        int slot = nbt.getInteger("selected_slot");
        final int oslot = slot;
        slot = Math.floorMod(dwheel < 0 ? slot + 1 : slot - 1, 9);
        while (Block.getBlockFromItem(player.inventory.getStackInSlot(slot).getItem()) == Blocks.AIR && slot != oslot)
            slot = Math.floorMod(dwheel < 0 ? slot + 1 : slot - 1, 9);
        nbt.setInteger("selected_slot", slot);
        return true;
    }
}