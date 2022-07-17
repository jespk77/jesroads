package mod.jesroads2.tileentity.dynamicsigns;

import java.util.ArrayList;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.BlockBase;
import mod.jesroads2.block.system.BlockDynamicSignController;
import mod.jesroads2.block.system.BlockDynamicSign;
import mod.jesroads2.block.system.BlockDynamicSign.EnumFreewaySignType;
import mod.jesroads2.block.system.BlockEventSign;
import mod.jesroads2.tileentity.ITileEntityBindable;
import mod.jesroads2.tileentity.TileEntityBase;
import mod.jesroads2.tileentity.dynamicsigns.event.IFreewayEvent;
import mod.jesroads2.util.ComparatorSign;
import mod.jesroads2.util.IRemoteBinding;
import mod.jesroads2.util.NBTUtils;
import mod.jesroads2.util.OtherUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityDynamicSignController extends TileEntityBase implements ITileEntityBindable, IRemoteBinding {
    public enum ESpeedLimit {
        Regular(EnumFreewaySignType.None),
        RegularReduced(EnumFreewaySignType.ReducedRegular),
        Reduced(EnumFreewaySignType.Reduced),
        Slow(EnumFreewaySignType.Slow);

        public final EnumFreewaySignType signType;

        ESpeedLimit(EnumFreewaySignType type){
            signType = type;
        }
    }

    public static final String name = "dynamic_sign_controller";
    private static final ComparatorSign comparator = new ComparatorSign();

    private ArrayList<BlockPos> laneSignList, eventSignList, controllerList;
    private final ArrayList<BlockPos> invalidSigns;
    private boolean isBinding;

    private int laneOffsetIn, laneOffsetOut;
    private int currentEvents;
    private ESpeedLimit speedLimit;
    private boolean ended, isRoadClosed;
    public boolean tempFront, isTemporaryLaneOpen, tempPrevOpen;

    private int tempLane;
    private int closedLane, warningClosedLane, closedAheadLane;

    public TileEntityDynamicSignController() {
        laneSignList = new ArrayList<>(3);
        eventSignList = new ArrayList<>(2);
        invalidSigns = new ArrayList<>(3);
        controllerList = new ArrayList<>(1);

        laneOffsetIn = laneOffsetOut = 0;
        currentEvents = 0;
        speedLimit = ESpeedLimit.Regular;
        ended = isRoadClosed = false;
        tempFront = false;
        isTemporaryLaneOpen = false;
        tempPrevOpen = true;

        tempLane = 0;
        closedLane = warningClosedLane = closedAheadLane = 0;
    }

    public TileEntityDynamicSignController(NBTTagCompound nbt) {
        this();
        readFromNBT(nbt);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        laneSignList = NBTUtils.readBlockPosList(nbt.getCompoundTag("laneSigns"));
        eventSignList = NBTUtils.readBlockPosList(nbt.getCompoundTag("eventSigns"));
        controllerList = NBTUtils.readBlockPosList(nbt.getCompoundTag("controllers"));
        BlockPos controller = NBTUtils.readBlockPos(nbt.getCompoundTag("controller"));
        if(controller != null) controllerList.add(controller);

        BlockPos controller2 = NBTUtils.readBlockPos(nbt.getCompoundTag("secondaryController"));
        if(controller2 != null) controllerList.add(controller2);

        laneOffsetIn = nbt.getInteger("laneOffsetIn");
        laneOffsetOut = nbt.getInteger("laneOffsetOut");
        isBinding = nbt.getBoolean("isBinding");
        currentEvents = nbt.getInteger("currentEvents");
        speedLimit = ESpeedLimit.values()[nbt.getInteger("speedLimit")];
        ended = nbt.getBoolean("ended");
        isRoadClosed = nbt.getBoolean("isRoadClosed");
        tempFront = nbt.getBoolean("tempFront");
        isTemporaryLaneOpen = nbt.getBoolean("isTemporaryLaneOpen");
        tempPrevOpen = nbt.getBoolean("tempPrevOpen");

        tempLane = nbt.getInteger("tempLane");
        closedLane = nbt.getInteger("closedLane");
        warningClosedLane = nbt.getInteger("warningClosedLane");
        closedAheadLane = nbt.getInteger("closedAheadLane");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt = super.writeToNBT(nbt);
        nbt.setTag("laneSigns", NBTUtils.writeBlockPosList(laneSignList));
        nbt.setTag("eventSigns", NBTUtils.writeBlockPosList(eventSignList));
        nbt.setTag("controllers", NBTUtils.writeBlockPosList(controllerList));

        nbt.setInteger("laneOffsetIn", laneOffsetIn);
        nbt.setInteger("laneOffsetOut", laneOffsetOut);
        nbt.setBoolean("isBinding", isBinding);
        nbt.setInteger("currentEvents", currentEvents);
        nbt.setInteger("speedLimit", speedLimit.ordinal());
        nbt.setBoolean("ended", ended);
        nbt.setBoolean("isRoadClosed", isRoadClosed);
        nbt.setBoolean("tempFront", tempFront);
        nbt.setBoolean("tempOpen", isTemporaryLaneOpen);
        nbt.setBoolean("tempPrevOpen", tempPrevOpen);

        nbt.setInteger("tempLane", tempLane);
        nbt.setInteger("closedLane", closedLane);
        nbt.setInteger("warningClosedLane", warningClosedLane);
        nbt.setInteger("closedAheadLane", closedAheadLane);
        return nbt;
    }

    public void notifyEvent(IFreewayEvent event) {
        event.handleEvent(this);

        World world = getWorld();
        for(BlockPos controller : controllerList){
            TileEntity tile = world.getTileEntity(controller);
            if(tile instanceof TileEntityDynamicSignController)
                event.notifyNeighbor(this, (TileEntityDynamicSignController) tile);
            else invalidSigns.add(controller);
        }

        updateSigns();
    }

    public int getLaneOffsetIn(){ return laneOffsetIn; }
    public int setLaneOffsetIn(int offset){
        if(laneOffsetIn != offset){
            laneOffsetIn = offset;
            markDirty();
        } return getLaneOffsetIn();
    }

    public int getLaneOffsetOut(){ return laneOffsetOut; }
    public int setLaneOffsetOut(int offset){
        if(laneOffsetOut != offset){
            laneOffsetOut = offset;
            markDirty();
        } return getLaneOffsetOut();
    }

    public ESpeedLimit getSpeedLimit(){ return speedLimit; }

    public ESpeedLimit setSpeedLimit(ESpeedLimit limit) {
        if(speedLimit != limit) {
            speedLimit = limit;
            markDirty();
        } return getSpeedLimit();
    }

    public boolean isEnded() { return ended; }
    public boolean toggleEnded() { return setEnded(!ended); }
    public boolean setEnded(boolean end) {
        if (ended != end) {
            ended = end;
            markDirty();
        } return isEnded();
    }

    public boolean getEvent(BlockEventSign.EnumEventType type){ return isFlag(currentEvents, type.flag); }
    public boolean setEvent(BlockEventSign.EnumEventType type){ return setEvent(type, !isFlag(currentEvents, type.flag));}
    public boolean setEvent(BlockEventSign.EnumEventType type, boolean set){
        currentEvents = setFlag(currentEvents, type.flag, set);
        return getEvent(type);
    }

    public boolean getRoadClosed() { return isRoadClosed; }
    public boolean toggleRoadClosed() { return setRoadClosed(!isRoadClosed); }
    public boolean setRoadClosed(boolean roadClosed) {
        if (isRoadClosed != roadClosed) {
            isRoadClosed = roadClosed;
            markDirty();
        } return getRoadClosed();
    }

    public boolean isTemporaryLaneOpen() { return isTemporaryLaneOpen; }
    public boolean toggleTemporaryLaneOpen() { return setTemporaryLaneOpen(!isTemporaryLaneOpen); }
    public boolean setTemporaryLaneOpen(boolean temporaryLaneOpen) {
		if(isTemporaryLaneOpen != temporaryLaneOpen) {
			isTemporaryLaneOpen = temporaryLaneOpen;
			markDirty();
		} return isTemporaryLaneOpen();
    }

    public boolean toggleOpenPrev(boolean open) {
        if (tempPrevOpen != open) {
            tempPrevOpen = open;
            markDirty();
        } return tempPrevOpen;
    }

    public int getTempLanes() { return tempLane; }
    public boolean hasTempLanes() { return getTempLanes() != 0; }

	public int getClosedLanes() { return closedLane; }
	public boolean hasClosedLanes(){ return getClosedLanes() != 0; }

	public int getWarningLanes() { return warningClosedLane; }
	public boolean hasWarningLanes(){ return getWarningLanes() != 0; }

    public int getClosedAheadLanes(){ return closedAheadLane; }
    public boolean hasClosedAheadLanes(){ return getClosedAheadLanes() != 0; }

    public boolean hasClosingLanes() { return hasClosedLanes() || hasWarningLanes() || hasClosedAheadLanes(); }

    public boolean closeLane(int lane, boolean isTemporaryLane){
        boolean enabled = isEnabled(isTemporaryLane? tempLane : closedLane, lane);
        return closeLane(lane, isTemporaryLane, !enabled);
    }

    public boolean closeLane(int lane, boolean isTemporaryLane, boolean enabled) {
        if (isTemporaryLane) {
            if (tempFront) tempFront = false;
            else tempLane = setIndex(tempLane, lane, enabled);
        } else closedLane = setIndex(closedLane, lane, enabled);

        markDirty();
        return lane >= 0 && lane < laneSignList.size() && enabled;
    }

    public boolean warningLane(int lane, boolean isTemporaryLane){
        boolean enabled = isEnabled(isTemporaryLane? tempLane : warningClosedLane, lane);
        return warningLane(lane, isTemporaryLane, !enabled);
    }

    public boolean warningLane(int lane, boolean isTemporaryLane, boolean enabled) {
        lane += laneOffsetIn;

        if (isTemporaryLane) {
            tempLane = setIndex(tempLane, lane, enabled);
            tempFront = enabled;
            tempPrevOpen = false;
        } else warningClosedLane = setIndex(warningClosedLane, lane, enabled);

        markDirty();
        return lane >= 0 && lane < laneSignList.size() && enabled;
    }

    public boolean closedAheadLane(int lane){ return closedAheadLane(lane, !isEnabled(closedAheadLane, lane)); }

    public boolean closedAheadLane(int lane, boolean enabled){
        lane += laneOffsetIn;

        closedAheadLane = setIndex(closedAheadLane, lane, enabled);
        markDirty();
        return lane >= 0 && lane < laneSignList.size() && enabled;
    }

    private static int setIndex(int control, int index, boolean set){
        return setFlag(control, 2 << index, set);
    }

    private static int setFlag(int control, int flag, boolean set) {
        if (!set) return control & ~flag;
        else return control | flag;
    }

    private static boolean isEnabled(int control, int pos) {
        if (control == 0) return false;

        pos = 2 << pos;
        return (control & pos) == pos;
    }

    private static boolean isFlag(int control, int flag){
        return (control & flag) != 0;
    }

    public void updateSigns() {
       BlockEventSign.EnumEventType currentEvent = BlockEventSign.EnumEventType.fromFlag(currentEvents);
        for(int i = 0; i < eventSignList.size(); i++)
            setEvent(i, currentEvent);

        boolean isNormalSituation = !hasClosingLanes();
        for (int i = 0; i < laneSignList.size(); i++) {
            if (getRoadClosed() || isEnabled(closedLane, i)) {
                setLane(i, EnumFreewaySignType.Closed);
                continue;
            } else if (isEnabled(tempLane, i)) {
                if ((tempFront && !tempPrevOpen) || (isTemporaryLaneOpen() && !tempPrevOpen)) {
                    EnumFreewaySignType type = getWarningType(i);
                    setLane(i, type);
                    setEvent(i, type == EnumFreewaySignType.MergeLeft? BlockEventSign.EnumEventType.RightMerge: BlockEventSign.EnumEventType.LeftMerge);
                    continue;
                } else if (!tempFront && !isTemporaryLaneOpen()) {
                    setLane(i, EnumFreewaySignType.Closed);
                    continue;
                }
            }

            if (isEnabled(warningClosedLane, i)) {
                EnumFreewaySignType laneType = getWarningType(i);
                setLane(i, laneType);
                setEvent(i, laneType == EnumFreewaySignType.MergeLeft? BlockEventSign.EnumEventType.RightMerge: BlockEventSign.EnumEventType.LeftMerge);
                continue;
            } else if(isEnabled(closedAheadLane, i))
                setEvent(i, getWarningType(i) == EnumFreewaySignType.MergeLeft? BlockEventSign.EnumEventType.RightMergeWarning: BlockEventSign.EnumEventType.LeftMergeWarning);

            ESpeedLimit speed = getSpeedLimit();
            if (!isNormalSituation){
                setLane(i, speed.signType.ordinal() < EnumFreewaySignType.Reduced.ordinal()? EnumFreewaySignType.Reduced : speed.signType);
                continue;
            }

            if(speed != ESpeedLimit.Regular){
                setLane(i, speed.signType);
                continue;
            }

            if (isEnded()) setLane(i, EnumFreewaySignType.End);
            else if (isTemporaryLaneOpen() || (tempFront && tempPrevOpen)) setLane(i, EnumFreewaySignType.Open);
            else setLane(i, EnumFreewaySignType.None);
        }

        removeAllInvalid();
    }

    private EnumFreewaySignType getWarningType(int pos) {
        EnumFreewaySignType left = getSign(pos - 1);
        EnumFreewaySignType right = getSign(pos + 1);
        if (!EnumFreewaySignType.isClosed(right)) return EnumFreewaySignType.MergeRight;
        else if (!EnumFreewaySignType.isClosed(left)) return EnumFreewaySignType.MergeLeft;
        else return EnumFreewaySignType.Slow;
    }

    private void setLane(int index, EnumFreewaySignType type) {
        if (index >= 0 && index < laneSignList.size()) {
            BlockPos pos = laneSignList.get(index);
            IBlockState state = getWorld().getBlockState(pos);
            if (state.getBlock() instanceof BlockDynamicSign)
                world.setBlockState(pos, state.withProperty(BlockDynamicSign.type, type), 3);
            else invalidSigns.add(pos);
        }
    }

    private void setEvent(int index, BlockEventSign.EnumEventType type){
        if(eventSignList.size() > 0) {
            index = Math.max(Math.min(index, eventSignList.size() - 1), 0);
            BlockPos pos = eventSignList.get(index);
            IBlockState state = getWorld().getBlockState(pos);
            if (state.getBlock() instanceof BlockEventSign)
                world.setBlockState(pos, state.withProperty(BlockEventSign.event, type), 3);
            else invalidSigns.add(pos);
        }
    }

    public EnumFreewaySignType getSign(int pos) {
        if (pos >= 0 && pos < laneSignList.size()) return getSign(laneSignList.get(pos));
        else return null;
    }

    private EnumFreewaySignType getSign(BlockPos pos) {
        IBlockState s = getWorld().getBlockState(pos);
        if (s.getBlock() instanceof BlockDynamicSign) return s.getValue(BlockDynamicSign.type);
        else return null;
    }


    public int getLaneSignCount() { return laneSignList.size(); }
    public BlockPos[] getLaneSigns() { return laneSignList.toArray(new BlockPos[0]); }

    public int getEventSignCount() { return eventSignList.size(); }

    public BlockPos[] getEventSigns(){ return eventSignList.toArray(new BlockPos[0]); }

    public int getControllerCount(){ return controllerList.size(); }

    public BlockPos[] getControllers() { return controllerList.toArray(new BlockPos[0]); }

    @Override
    public void bindCheck() {
        World world = getWorld();
        for (BlockPos p : laneSignList)
            if (!(world.getBlockState(p).getBlock() instanceof BlockDynamicSign)) invalidSigns.add(p);
		for(BlockPos p : eventSignList)
			if(!(world.getBlockState(p).getBlock() instanceof BlockEventSign)) invalidSigns.add(p);
        for(BlockPos c: controllerList)
            if(!(world.getBlockState(c).getBlock() instanceof BlockDynamicSignController)) invalidSigns.add(c);
        removeAllInvalid();
    }

    public void reset(){
        closedLane = closedAheadLane = warningClosedLane = 0;
        currentEvents = 0;
        markDirty();
        updateSigns();
    }

    private void removeAllInvalid() {
        for (BlockPos r : invalidSigns){
			if(laneSignList.remove(r)) continue;
			if(eventSignList.remove(r)) continue;
            controllerList.remove(r);
		} invalidSigns.clear();
    }

    public void scanForSigns(EnumFacing direction) {
        bindCheck();
        BlockPos current = getPos(),
                from = current.offset(direction.rotateYCCW(), JesRoads2.options.freeway_controller.range_left).offset(direction.getOpposite(), JesRoads2.options.freeway_controller.range_back),
                to = current.offset(direction).up(JesRoads2.options.freeway_controller.range_up);
        World world = getWorld();
        boolean dirty = false;
        for (BlockPos p : BlockPos.getAllInBox(from, to)) {
            IBlockState state = world.getBlockState(p);
			Block block = state.getBlock();
            if (block instanceof BlockDynamicSign) {
                BlockDynamicSign sign = (BlockDynamicSign) block;
                if (sign.facing.isEqual(direction) && !laneSignList.contains(p)) {
                    laneSignList.add(p);
                    dirty = true;
                }
            } else if(block instanceof BlockEventSign){
				BlockEventSign sign = (BlockEventSign) block;
				if(sign.direction.isEqual(direction) && !eventSignList.contains(p)){
					eventSignList.add(p);
					dirty = true;
				}
			}
        }

        if (dirty) {
            laneSignList.sort(comparator.withFacing(direction.rotateYCCW()));
            eventSignList.sort(comparator.withFacing(direction.rotateYCCW()));
            markDirty();
        }
    }

    @Override
    public String addBind(BlockPos pos) {
        Block block = getWorld().getBlockState(pos).getBlock();
        if (block instanceof BlockDynamicSign) {
            if (laneSignList.contains(pos)) {
                laneSignList.remove(pos);
                markDirty();
                return "[Dynamic Sign Controller] Sign has been unbound (" + getLaneSignCount() + " bound)";
            } else {
                laneSignList.add(pos);
                markDirty();
                return "[Dynamic Sign Controller] Sign has been bound (" + getLaneSignCount() + " bound)";
            }
        } else if(block instanceof BlockEventSign) {
            if(eventSignList.contains(pos)){
                eventSignList.remove(pos);
                markDirty();
                return "[Dynamic Sign Controller] Event sign has been unbound (" + getEventSignCount() + " bound)";
            } else {
                eventSignList.add(pos);
                markDirty();
                return "[Dynamic Sign Controller] Event sign has been bound (" + getEventSignCount() + " bound)";
            }
        } else if (block instanceof BlockDynamicSignController) {
            if(controllerList.contains(pos)){
                controllerList.remove(pos);
                markDirty();
                return "[Dynamic Sign Controller] Sign controller has been unbound (" + getControllerCount() + " bound)";
            } else {
                controllerList.add(pos);
                markDirty();
                return "[Dynamic Sign Controller] Sign controller has been bound (distance=" + BlockBase.displayDistance(getPos(), pos) + " blocks, " + getControllerCount() + " bound)";
            }
        }
        return null;
    }

    @Override
    public String displayBinds() {
        bindCheck();
        StringBuilder build = new StringBuilder();
        build.append(laneSignList.size()).append(" lane signs bound\n");
        build.append(eventSignList.size()).append(" event signs bound\n");

        if(getControllerCount() > 0) {
            build.append("\nBound controllers:\n");
            BlockPos pos = getPos();
            for (BlockPos controller : controllerList)
                build.append(" - ").append(OtherUtils.formatString(controller)).append(" (distance=").append(BlockBase.displayDistance(pos, controller)).append(" blocks)\n");
        }

        if (hasTempLanes()) build.append(isTemporaryLaneOpen() ? "\nTemporary lane open" : "\nTemporary lane closed");
        return build.toString();
    }

    public void onBlockDestroyed() {
        World world = getWorld();
        if (world.isRemote) return;

        for (BlockPos sign : laneSignList) {
            IBlockState s = world.getBlockState(sign);
            if (s.getBlock() instanceof BlockDynamicSign)
                world.setBlockState(sign, s.withProperty(BlockDynamicSign.type, BlockDynamicSign.EnumFreewaySignType.None), 2);
        }

		for(BlockPos sign: eventSignList) {
			IBlockState s = world.getBlockState(sign);
			if(s.getBlock() instanceof BlockEventSign)
				world.setBlockState(sign, s.withProperty(BlockEventSign.event, BlockEventSign.EnumEventType.None), 2);
		}
    }

    @Override
    public void execute() {

    }

    @Override
    public boolean isEnabled() {
        return isTemporaryLaneOpen();
    }

    @Override
    public String onStartBind() {
        isBinding = true;
        return "Freeway Controller";
    }

    @Override
    public void onStopBind() {
        isBinding = false;
    }

    @Override
    public boolean isBinding() {
        return isBinding;
    }
}