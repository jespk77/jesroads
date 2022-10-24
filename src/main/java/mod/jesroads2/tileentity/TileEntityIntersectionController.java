package mod.jesroads2.tileentity;

import java.util.*;
import java.util.Map.Entry;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.system.BlockIntersectionController;
import mod.jesroads2.block.system.BlockTrafficlight;
import mod.jesroads2.block.system.BlockTrafficlight.EnumTrafficLightState;
import mod.jesroads2.block.system.BlockTrafficlight.EnumTrafficLightType;
import mod.jesroads2.util.IRemoteBinding;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityIntersectionController extends TileEntity implements ITickable, ITileEntityBindable, IRemoteBinding {
    public enum EnumControllerMode {
        OFF,
        ON,
        TIMED;

        public static EnumControllerMode fromBoolean(boolean enabled) {
            return enabled ? ON : OFF;
        }

        public static EnumControllerMode fromID(int id) {
            EnumControllerMode[] values = EnumControllerMode.values();
            if (id > 0 && id < values.length) return values[id];
            else return values[0];
        }
    }

    protected final Map<BlockPos, TrafficLightData> lights;
    protected final Queue<BlockPos> queue;
    protected final List<BlockPos> green, red;
    protected final List<BlockPos> update;
    private EnumFacing.Axis orientation;
    private EnumControllerMode mode;
    private boolean isBinding;

    private int updateTick = JesRoads2.options.intersection_controller.cycle_tick,
            emptyTick = JesRoads2.options.intersection_controller.cycle_empty;
    private boolean enabled;
    private boolean detected;

    private int testing;

    public TileEntityIntersectionController() {
        this(null, 0);
    }

    public TileEntityIntersectionController(BlockIntersectionController controller, int meta) {
        lights = new HashMap<>();
        queue = new LinkedList<>();
        green = new ArrayList<>(4);
        red = new ArrayList<>();
        update = new ArrayList<>();

        if (controller != null)
            orientation = controller.getStateFromMeta(meta).getValue(BlockIntersectionController.axis);
        else orientation = EnumFacing.Axis.Z;

        mode = EnumControllerMode.OFF;
        isBinding = false;
        enabled = false;
        detected = false;
        testing = -1;
    }

    public TileEntityIntersectionController(NBTTagCompound nbt) {
        this();
        readFromNBT(nbt);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        int index = 0;
        lights.clear();
        update.clear();
        while (tag.hasKey("light_" + index)) {
            NBTTagCompound nbt = tag.getCompoundTag("light_" + index);
            BlockPos pos = new BlockPos(nbt.getInteger("posX"), nbt.getInteger("posY"), nbt.getInteger("posZ"));
            TrafficLightData data = new TrafficLightData(nbt.getCompoundTag("tag"));

            lights.put(pos, data);
            index++;
            if (data.cycle >= 0) update.add(pos);
        }

        isBinding = tag.getBoolean("isBinding");
        detected = tag.getBoolean("detected");
        setEnabled(tag.getBoolean("enabled"));
        mode = tag.hasKey("mode") ? EnumControllerMode.fromID(tag.getInteger("mode")) : EnumControllerMode.fromBoolean(getEnabled());

        int o = tag.getInteger("orientation");
        setOrientation(o < 0 ? null : (o == 0 ? EnumFacing.Axis.Z : EnumFacing.Axis.X));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);
        int index = 0;
        for (Entry<BlockPos, TrafficLightData> light : lights.entrySet()) {
            NBTTagCompound nbt = new NBTTagCompound();
            BlockPos pos = light.getKey();
            nbt.setInteger("posX", pos.getX());
            nbt.setInteger("posY", pos.getY());
            nbt.setInteger("posZ", pos.getZ());
            nbt.setTag("tag", light.getValue().getTag());

            tag.setTag("light_" + index, nbt);
            index++;
        }

        tag.setBoolean("isBinding", isBinding);
        tag.setInteger("mode", mode.ordinal());
        tag.setBoolean("detected", detected);
        tag.setBoolean("enabled", enabled);
        tag.setInteger("orientation", orientation != null ? (orientation == EnumFacing.Axis.Z ? 0 : 1) : -1);
        return tag;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(super.getUpdateTag());
    }

    @Override
    @SideOnly(Side.SERVER)
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(), 0, writeToNBT(new NBTTagCompound()));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onDataPacket(NetworkManager network, SPacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
    }

    @Override
    public void update() {
        World world = getWorld();
        if (world.isRemote) return;

        if (updateTick > 0) updateTick--;
        else {
            updateTick = JesRoads2.options.intersection_controller.cycle_tick;
            updateControllerState(world);

            if (!getEnabled()) {
                if (isTesting()) {
                    if (!queue.isEmpty()) {
                        BlockPos p = queue.poll();
                        TrafficLightData data = lights.get(p);
                        if (data != null && data.state != EnumTrafficLightState.GREEN) {
                            setLight(p, data, EnumTrafficLightState.GREEN);
                            if (testing + 1 == lights.size()) setTesting(false);
                            else testing++;
                        }
                    }
                } else queue.clear();
                return;
            }

            boolean empty = update.isEmpty();
            for (Iterator<BlockPos> it = update.iterator(); it.hasNext(); ) {
                emptyTick = 0;
                BlockPos ps = it.next();
                IBlockState state = world.getBlockState(ps);
                if (!(state.getBlock() instanceof BlockTrafficlight)) {
                    it.remove();
                    continue;
                }

                TrafficLightData data = lights.get(ps);
                if (data.cycle == 0) {
                    EnumTrafficLightState s = data.updateState();
                    setLight(ps, data, s);
                    if (s == EnumTrafficLightState.YELLOW)
                        data.cycle = JesRoads2.options.intersection_controller.cycle_yellow;
                    else {
                        data.cycle = -1;
                        it.remove();
                    }
                } else data.cycle--;
            }

            red.clear();
            green.clear();
            if (updateController(empty)) {
                boolean instant = true;
                for (BlockPos r : red) {
                    TrafficLightData d = lights.get(r);
                    if (d.state != EnumTrafficLightState.RED && !update.contains(r)) {
                        instant = false;
                        d.cycle = 0;
                        update.add(r);
                        if (d.detected) queue.add(r);
                    }
                }

                for (BlockPos g : green) {
                    TrafficLightData d = lights.get(g);
                    boolean equal = true;
                    for (BlockPos ps : update)
                        if (!lights.get(ps).isEqual(d)) {
                            equal = false;
                            break;
                        }

                    if (equal) {
                        if (d.state != EnumTrafficLightState.GREEN && !update.contains(g)) {
                            d.wait = 0;
                            d.cycle = !instant ? JesRoads2.options.intersection_controller.cycle_yellow + 2 : 0;
                            update.add(g);
                        }
                    } else queue.add(g);
                }
            } else {
                if (!detected) {
                    if (emptyTick > JesRoads2.options.intersection_controller.cycle_empty) setDefaults();
                    else if (empty) emptyTick++;
                } else emptyTick = 0;
            }
        }
    }

    protected void updateControllerState(World world) {
        switch (mode) {
            case OFF: {
                if (!isTesting()) setEnabled(false);
                break;
            }
            case ON: {
                setEnabled(true);
                break;
            }
            case TIMED: {
                int time = (int) Math.floorMod(world.getWorldTime(), 24000);
                setEnabled(!(time > JesRoads2.options.intersection_controller.time_disabled && time < JesRoads2.options.intersection_controller.time_enabled));
                break;
            }
        }
    }

    protected boolean updateController(boolean empty) {
        if (!queue.isEmpty()) {
            emptyTick = 0;
            BlockPos pos = queue.poll();
            TrafficLightData data = lights.get(pos);

            for (Entry<BlockPos, TrafficLightData> entry : lights.entrySet()) {
                BlockPos p = entry.getKey();
                TrafficLightData d = entry.getValue();

                if (d.isEqual(data)) green.add(p);
                else if (d.checkCollision(data)) {
                    if ((!d.detected || data.wait > JesRoads2.options.intersection_controller.cycle_wait))
                        red.add(p);
                    else {
                        queue.add(pos);
                        data.wait++;
                        return false;
                    }
                }
            }
            return true;
        } else return false;
    }

    protected boolean setLight(BlockPos pos, TrafficLightData data, EnumTrafficLightState s) {
        World world = getWorld();
        emptyTick = 0;

        IBlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof BlockTrafficlight)) return false;

        if (data != null) data.state = s;
        return world.setBlockState(pos, state.withProperty(BlockTrafficlight.sign, s), 2);
    }

    protected void setDefaults() {
        if (orientation != null) {
            for (Entry<BlockPos, TrafficLightData> entry : lights.entrySet()) {
                TrafficLightData data = entry.getValue();
                boolean green = data.type != EnumTrafficLightType.LEFT && orientation == data.facing.getAxis();
                if ((green && data.state == EnumTrafficLightState.RED) || (!green && data.state == EnumTrafficLightState.GREEN)) {
                    data.cycle = green ? JesRoads2.options.intersection_controller.cycle_yellow + 2 : 0;
                    update.add(entry.getKey());
                }
            }
        } else {
            for (Entry<BlockPos, TrafficLightData> entry : lights.entrySet()) {
                TrafficLightData data = entry.getValue();
                if (data.detected) continue;

                if (data.state != EnumTrafficLightState.OFF && data.state != EnumTrafficLightState.RED) {
                    data.cycle = 0;
                    update.add(entry.getKey());
                }
            }
        }
    }

    public String getName() {
        return "Intersection Controller";
    }

    public int getLightCount() {
        return lights.size();
    }

    public BlockPos[] getLights() {
        return lights.keySet().toArray(new BlockPos[getLightCount()]);
    }

    public TrafficLightData[] getData() {
        return lights.values().toArray(new TrafficLightData[getLightCount()]);
    }

    public boolean updateDetectedState(BlockPos light, boolean detected, boolean simulated) {
        if (simulated && isTesting()) return false;

        TrafficLightData data = lights.get(light);
        if (data != null) {
            data.detected = detected;
            if (detected && !queue.contains(light)) queue.add(light);
            detected = updateDetected();
            return true;
        } else return false;
    }

    private boolean updateDetected() {
        for (TrafficLightData data : lights.values())
            if (data.detected) return true;
        return false;
    }

    @Override
    public String addBind(BlockPos light) {
        World world = getWorld();
        if (!lights.containsKey(light)) {
            IBlockState c = world.getBlockState(getPos()), s = world.getBlockState(light);
            EnumFacing facing = s.getValue(BlockTrafficlight.facing);
            BlockTrafficlight block = ((BlockTrafficlight) s.getBlock());
            TrafficLightData data = new TrafficLightData(facing, block.type, s.getValue(BlockTrafficlight.sign));
            lights.put(light, data);
            markDirty();
            return "[Intersection Controller] Trafficlight bound (" + getLightCount() + " bound)";
        } else {
            lights.remove(light);
            markDirty();
            return "[Intersection Controller] Trafficlight unbound (" + getLightCount() + " bound)";
        }
    }

    private boolean getEnabled() {
        return enabled;
    }

    private boolean setEnabled() {
        return setEnabled(!getEnabled());
    }

    private boolean setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            setAllLightsInstant(enabled ? EnumTrafficLightState.RED : EnumTrafficLightState.OFF);
            if (enabled) setDefaults();
            markDirty();
        }
        return getEnabled();
    }

    public EnumControllerMode getMode() {
        return mode;
    }

    public EnumControllerMode toggleMode() {
        if (mode == EnumControllerMode.OFF) return setMode(EnumControllerMode.ON);
        else if (mode == EnumControllerMode.ON) return setMode(EnumControllerMode.OFF);
        else return getMode();
    }

    public EnumControllerMode setMode() {
        return setMode(EnumControllerMode.fromID(mode.ordinal() + 1));
    }

    public EnumControllerMode setMode(EnumControllerMode mode) {
        if (this.mode != mode) {
            this.mode = mode;
            if (mode != EnumControllerMode.OFF) setTesting(false);
            markDirty();
        }
        return getMode();
    }

    public boolean isTesting() {
        return testing >= 0;
    }

    public boolean setTesting() {
        return setTesting(!isTesting());
    }

    public boolean setTesting(boolean test) {
        boolean testing = isTesting();
        if (testing != test) {
            if (test) {
                this.testing = 0;
                setAllLightsInstant(EnumTrafficLightState.RED);
            } else {
                this.testing = -1;
                setAllLightsInstant(EnumTrafficLightState.OFF);
            }
            markDirty();
        }
        return testing;
    }

    protected void setAllLightsInstant(EnumTrafficLightState state) {
        World world = getWorld();
        if (!world.isRemote)
            for (Entry<BlockPos, TrafficLightData> entry : lights.entrySet())
                setLight(entry.getKey(), entry.getValue(), state);
    }

    public EnumFacing.Axis getOrientation() {
        return orientation;
    }

    public EnumFacing.Axis setOrientation(EnumFacing.Axis orientation) {
        if (this.orientation != orientation) {
            this.orientation = orientation;
            markDirty();
        }
        return getOrientation();
    }

    @Override
    public String displayBinds() {
        bindCheck();
        StringBuilder build = new StringBuilder();
        build.append(getLightCount()).append(" lights connected");
        for (Entry<BlockPos, TrafficLightData> entry : lights.entrySet()) {
            BlockPos p = entry.getKey();
            TrafficLightData data = entry.getValue();
            build.append("\n[x=").append(p.getX()).append(", y=").append(p.getY()).append(", z=").append(p.getZ()).append("] Direction=").append(data.facing.getName().toLowerCase()).append(", Type=").append(data.type.getName().toLowerCase());
        }
        return build.toString();
    }

    @Override
    public void bindCheck() {
        World world = getWorld();

        ArrayList<BlockPos> removing = new ArrayList<>(3);
        for (Entry<BlockPos, TrafficLightData> entry : lights.entrySet()) {
            BlockPos lightPos = entry.getKey();
            TrafficLightData data = entry.getValue();
            IBlockState lightState = world.getBlockState(lightPos);
            Block block = lightState.getBlock();
            if (!(block instanceof BlockTrafficlight)
                || lightState.getValue(BlockTrafficlight.facing) != data.facing
                || ((BlockTrafficlight) block).type != data.type)
                    removing.add(lightPos);
        }

        for (BlockPos r : removing)
            lights.remove(r);
    }

    public void onBlockDestroyed() {
        for (BlockPos p : lights.keySet()) {
            setLight(p, null, EnumTrafficLightState.OFF);
        }
    }

    public static class TrafficLightData {
        public final EnumFacing facing;
        public final EnumTrafficLightType type;
        private EnumTrafficLightState state;

        public boolean detected;
        public int wait, cycle;

        private static final boolean[][] collisionMap = {
                //SOUTH - WEST - NORTH - EAST and LEFT - STRAIGHT - RIGHT
                {false, false, false, true, true, false, false, true, true, true, true, false},
                {false, false, false, true, true, false, true, false, false, true, true, true},
                {false, false, false, false, true, false, true, false, false, false, false, false},

                {true, true, false, false, false, false, true, true, false, false, true, true},
                {true, true, true, false, false, false, true, true, false, true, false, false},
                {false, false, false, false, false, false, false, true, false, true, false, false},

                {false, true, true, true, true, false, false, false, false, true, true, false},
                {true, false, false, true, true, true, false, false, false, true, true, false},
                {true, false, false, false, false, false, false, false, false, false, true, false},

                {true, true, false, false, true, true, true, true, false, false, false, false},
                {true, true, false, true, false, false, true, true, true, false, false, false},
                {false, true, false, true, false, false, false, false, false, false, false, false}
        };

        public TrafficLightData(EnumFacing facing, EnumTrafficLightType type, EnumTrafficLightState state) {
            this.facing = facing;
            this.type = type;
            this.state = state;

            detected = false;
            wait = 0;
            cycle = -1;
        }

        public TrafficLightData(NBTTagCompound tag) {
            facing = EnumFacing.getHorizontal(tag.getInteger("facing"));
            type = EnumTrafficLightType.fromID(tag.getInteger("tp"));
            state = EnumTrafficLightState.fromID(tag.getInteger("state"));

            detected = tag.getBoolean("detected");
            wait = tag.getInteger("counter");
            cycle = tag.hasKey("transition") ? tag.getInteger("transition") : -1;
        }

        public NBTTagCompound getTag() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("facing", facing.getHorizontalIndex());
            tag.setInteger("tp", type.id);
            tag.setInteger("state", state.id);

            tag.setBoolean("detected", detected);
            tag.setInteger("counter", wait);
            tag.setInteger("transition", cycle);
            return tag;
        }

        public EnumTrafficLightState getState() {
            return state;
        }

        public EnumTrafficLightState updateState() {
            switch (state) {
                case OFF:
                    break;
                case RED: {
                    state = EnumTrafficLightState.GREEN;
                    break;
                }
                case YELLOW: {
                    state = EnumTrafficLightState.RED;
                    break;
                }
                case GREEN: {
                    state = EnumTrafficLightState.YELLOW;
                    break;
                }
            }
            return getState();
        }

        public boolean isEqual(TrafficLightData data) {
            if (data != null) return facing == data.facing && type == data.type;
            else return false;
        }

        public boolean checkCollision(TrafficLightData data) {
            return checkCollision(data.facing, data.type);
        }

        public boolean checkCollision(EnumFacing face, EnumTrafficLightType tp) {
            return collisionMap[collisionIndex(facing, type)][collisionIndex(face, tp)];
        }

        private int collisionIndex(EnumFacing face, EnumTrafficLightType tp) {
            return (face.getHorizontalIndex() * 3) + tp.id;
        }

        @Override
        public String toString() {
            return "{TrafficLightData:facing=" + facing.name() + ",type=" + type.name() + ",state=" + state.name()
                    + ",detected=" + detected + ",wait=" + wait + ",cycle=" + cycle + "}";
        }

    }

    @Override
    public void execute() {
        toggleMode();
    }

    @Override
    public boolean isEnabled() {
        return getMode() == EnumControllerMode.ON;
    }

    @Override
    public String onStartBind() {
        isBinding = true;
        return getName();
    }

    @Override
    public boolean isBinding() {
        return isBinding;
    }

    @Override
    public void onStopBind() {
        isBinding = false;
    }
}