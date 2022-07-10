package mod.jesroads2.tileentity;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.system.BlockDynamicSign;
import mod.jesroads2.block.system.BlockDynamicSign.EnumFreewaySignType;
import mod.jesroads2.block.system.BlockGateBarrier;
import mod.jesroads2.item.ItemGateTicket;
import mod.jesroads2.network.MessageGateControllerUser;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityGateController extends TileEntity implements ITileEntityBindable {
    public enum EnumControllerMode {
        IN(true, true, false),
        EXCLUSIVE_IN(true, false, false),
        OUT(false, true, false),
        EXCLUSIVE_OUT(false, false, false),
        CLOSED(false, false, false),
        TOLL_START(true, true, true),
        TOLL_END(false, true, true),
        SINGLE_USE(false, true, false);

        public final boolean in, guests, toll;
        private final TextComponentTranslation welcome;
        private final TextComponentTranslation denied;

        EnumControllerMode(boolean in, boolean guests, boolean toll) {
            this.in = in;
            this.guests = guests;
            this.toll = toll;
            String name = name().toLowerCase();
            welcome = new TextComponentTranslation("chat.gatecontroller." + name + ".welcome");
            denied = new TextComponentTranslation("chat.gatecontroller." + name + ".denied");
        }

        public boolean isClosed() {
            return this == EnumControllerMode.CLOSED;
        }

        public String getWelcomeMessage() {
            return welcome.getFormattedText();
        }

        public String getAcceptMessage() {
            return null;
        }

        public String getDeniedMessage() {
            return denied.getFormattedText();
        }

        public EnumFreewaySignType getType() {
            if (this == CLOSED) return EnumFreewaySignType.Closed;
            else if (toll) return EnumFreewaySignType.Open;//TODO add toll sign(s)
            else return EnumFreewaySignType.Open;
        }

        public static EnumControllerMode fromID(int id) {
            EnumControllerMode[] values = EnumControllerMode.values();
            if (id > 0 && id < values.length) return values[id];
            else return values[0];
        }

        public static EnumControllerMode fromProperties(boolean in, boolean guests, boolean toll) {
            if (toll) return in ? EnumControllerMode.TOLL_START : EnumControllerMode.TOLL_END;
            else if (guests) return in ? EnumControllerMode.IN : EnumControllerMode.OUT;
            else return in ? EnumControllerMode.EXCLUSIVE_IN : EnumControllerMode.EXCLUSIVE_OUT;
        }
    }

    private BlockPos barrier, sign;
    private EntityPlayer user;
    private boolean isBinding;

    private EnumControllerMode mode = EnumControllerMode.IN;
    private boolean gateOpen, closed;
    private String id;

    public TileEntityGateController() {
        gateOpen = false;
        closed = false;
        isBinding = false;
        id = "";
    }

    public TileEntityGateController(NBTTagCompound nbt) {
        this();
        readFromNBT(nbt);
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
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        if (nbt.hasKey("barrierX") && nbt.hasKey("barrierY") && nbt.hasKey("barrierZ"))
            barrier = new BlockPos(nbt.getInteger("barrierX"), nbt.getInteger("barrierY"), nbt.getInteger("barrierZ"));
        else barrier = null;

        if (nbt.hasKey("signX") && nbt.hasKey("signY") && nbt.hasKey("signZ"))
            sign = new BlockPos(nbt.getInteger("signX"), nbt.getInteger("signY"), nbt.getInteger("signZ"));
        else sign = null;

        isBinding = nbt.getBoolean("isBinding");
        mode = EnumControllerMode.fromID(nbt.getInteger("mode"));
        closed = nbt.hasKey("closed") ? nbt.getBoolean("closed") : mode == EnumControllerMode.CLOSED;
        id = nbt.hasKey("id") ? nbt.getString("name") : "";
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt = super.writeToNBT(nbt);

        if (barrier != null) {
            nbt.setInteger("barrierX", barrier.getX());
            nbt.setInteger("barrierY", barrier.getY());
            nbt.setInteger("barrierZ", barrier.getZ());
        }

        if (sign != null) {
            nbt.setInteger("signX", sign.getX());
            nbt.setInteger("signY", sign.getY());
            nbt.setInteger("signZ", sign.getZ());
        }

        nbt.setBoolean("isBinding", isBinding);
        if (mode != null) nbt.setInteger("mode", mode.ordinal());
        nbt.setBoolean("closed", closed);
        nbt.setString("name", id);
        return nbt;
    }

    public boolean onAction(ItemStack stack) {
        if (gateOpen || isBarrierLocked()) return false;
        if (closed) {
            if (user != null && world.isRemote)
                JesRoads2.handlerOverlay.getMessage().addMessage(mode.getDeniedMessage());
            return false;
        }

        if (user != null) {
            BlockPos pos = getPos();
            if (stack != null && stack.getItem() instanceof ItemGateTicket) {
                NBTTagCompound nbt = stack.getSubCompound(ItemGateTicket.nbt_name);
                if (nbt != null) {
                    if (nbt.getString("name").equals(id) && nbt.getUniqueId("owner").equals(user.getUniqueID())) {
                        if (nbt.hasKey("reusable")) {
                            if ((!mode.in && nbt.getBoolean("activated")) || (mode.in && nbt.getString("id").equals(id) && !nbt.getBoolean("activated"))) {
                                setBarrier(true);
                                nbt.setBoolean("activated", !nbt.getBoolean("activated"));
                                return true;
                            }
                        } else if (!mode.in) {
                            stack.splitStack(1);
                            setBarrier(true);
                            return true;
                        }
                    }
                }
                if (world.isRemote) JesRoads2.handlerOverlay.getMessage().addMessage(mode.getDeniedMessage());
            } else if (mode.in && stack != null && stack.getItem() == Items.DIAMOND) {
                stack.splitStack(1);
                ItemStack res = new ItemStack(JesRoads2.items.gate_ticket, 1, 1);
                NBTTagCompound nbt = res.getOrCreateSubCompound(ItemGateTicket.nbt_name);
                nbt.setUniqueId("owner", user.getPersistentID());
                nbt.setString("ownerName", user.getDisplayNameString());
                nbt.setString("name", id);
                nbt.setBoolean("reusable", true);

                nbt.setInteger("originX", pos.getX());
                nbt.setInteger("originY", pos.getY());
                nbt.setInteger("originZ", pos.getZ());
                nbt.setBoolean("activated", true);

                if (user.inventory.addItemStackToInventory(res)) {
                    setBarrier(true);
                    return true;
                }
            } else {
                if (mode == EnumControllerMode.SINGLE_USE) {
                    if (world.isRemote) setBarrier(true);
                    return true;
                }

                if (mode.in && mode.guests) {
                    ItemStack res = new ItemStack(JesRoads2.items.gate_ticket, 1, mode.toll ? 2 : 0);
                    NBTTagCompound nbt = res.getOrCreateSubCompound(ItemGateTicket.nbt_name);
                    nbt.setUniqueId("owner", user.getPersistentID());
                    nbt.setString("ownerName", user.getDisplayNameString());
                    nbt.setString("name", id);
                    if (mode.toll) {
                        nbt.setInteger("tollX", pos.getX());
                        nbt.setInteger("tollY", pos.getY());
                        nbt.setInteger("tollZ", pos.getZ());
                    }

                    if (user.inventory.addItemStackToInventory(res)) {
                        setBarrier(true);
                        return true;
                    }
                }
                if (world.isRemote) JesRoads2.handlerOverlay.getMessage().addMessage(mode.getDeniedMessage());
            }
            return true;
        } else return false;
    }

    private void setBarrier(boolean open) {
        if (barrier != null) {
            gateOpen = open;
            TileEntity tile = world.getTileEntity(barrier);
            if (tile instanceof TileEntityGateBarrier) ((TileEntityGateBarrier) tile).setOpened(open);
            else {
                barrier = null;
                markDirty();
            }
        }
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
        markDirty();
    }

    @Override
    public String addBind(BlockPos pos) {
        Block b = world.getBlockState(pos).getBlock();
        if (b instanceof BlockGateBarrier) {
            if (barrier != null && barrier.equals(pos)) barrier = null;
            else barrier = pos;
            markDirty();
            return barrier != null ? "[Gate Controller] Barrier bound" : "[Gate Controller] Barrier unbound";
        } else if (b instanceof BlockDynamicSign) {
            if (sign != null && sign.equals(sign)) sign = null;
            else sign = pos;
            setSign(mode.getType());
            markDirty();
            return sign != null ? "[Gate Controller] Sign bound" : "[Gate Controller] Sign unbound";
        } else return null;
    }

    public EntityPlayer getUser() {
        return user;
    }

    public EntityPlayer setUser(EntityPlayer user, boolean sendPacket) {
		this.user = user;
        if (user != null) {
            if (isBarrierLocked()) return user;
            if (world.isRemote) JesRoads2.handlerOverlay.getMessage().addMessage(mode.getWelcomeMessage());
        } else setBarrier(false);

        if (sendPacket)
            JesRoads2.channel.sendToAll(new MessageGateControllerUser(user != null ? user.getUniqueID() : null, getPos()));
        return user;
    }

    private boolean isBarrierLocked() {
        if (barrier != null) {
            TileEntity tile = getWorld().getTileEntity(barrier);
            if (tile instanceof TileEntityGateBarrier) return ((TileEntityGateBarrier) tile).getLocked();
        }
        return false;
    }

    public EnumControllerMode getMode() {
        return mode;
    }

    public EnumControllerMode setMode(EnumControllerMode mode) {
        if (mode == EnumControllerMode.CLOSED) closed = true;
        else {
			this.mode = mode;
            closed = false;
        }
        setSign(mode.getType());
        markDirty();
        return getMode();
    }

    private void setSign(EnumFreewaySignType type) {
        if (sign != null) {
            IBlockState s = world.getBlockState(sign);
            if (!(s.getBlock() instanceof BlockDynamicSign)) {
                sign = null;
                markDirty();
            } else if (s.getValue(BlockDynamicSign.type) != type)
                getWorld().setBlockState(sign, s.withProperty(BlockDynamicSign.type, type), 2);
        }
    }

    @Override
    public String displayBinds() {
        bindCheck();
        StringBuilder build = new StringBuilder();
        if (id.length() > 0) build.append("Name: ").append(id).append("\n");
        if (barrier != null)
            build.append("Barrier [x=").append(barrier.getX()).append(", y=").append(barrier.getY()).append(", z=").append(barrier.getZ()).append("]\n");
        if (sign != null)
            build.append("Sign [x=").append(sign.getX()).append(", y=").append(sign.getY()).append(", z=").append(sign.getZ()).append("]\n");

        build.append("Mode: ").append(mode.name().toLowerCase());
        return build.toString();
    }

    @Override
    public void bindCheck() {
        boolean dirty = false;
        if (barrier != null && !(getWorld().getBlockState(barrier).getBlock() instanceof BlockGateBarrier)) {
            barrier = null;
            dirty = true;
        }
        if (sign != null && !(getWorld().getBlockState(sign).getBlock() instanceof BlockDynamicSign)) {
            sign = null;
            dirty = true;
        }
        if (dirty) markDirty();
    }

    @Override
    public String onStartBind() {
        isBinding = true;
        return "Gate Controller";
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