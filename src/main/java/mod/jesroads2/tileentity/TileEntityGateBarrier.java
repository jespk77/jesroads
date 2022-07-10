package mod.jesroads2.tileentity;

import mod.jesroads2.block.system.BlockGateBarrier;
import mod.jesroads2.block.system.BlockGateBarrier.EnumPart;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityGateBarrier extends TileEntity {
    private static final AxisAlignedBB BARRIER_BOX = new AxisAlignedBB(0.D, 0.D, 0.D, 3.D, 3.D, 3.D);
    private static final int MIN_ANGLE = 0, MAX_ANGLE = 86, ANGLE_STEP = 1;

    private EnumFacing direction;
    private BlockGateBarrier.EnumGateType type;

    private boolean locked, opened;
    private float rotation;
    private int length;

    public TileEntityGateBarrier() {
        this(EnumFacing.SOUTH, BlockGateBarrier.EnumGateType.ONE);
    }

    public TileEntityGateBarrier(EnumFacing dir, BlockGateBarrier.EnumGateType type) {
        direction = dir;
		this.type = type;

        locked = false;
        opened = false;
        rotation = 0;
        length = 3;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        if (nbt.hasKey("dir")) direction = EnumFacing.getHorizontal(nbt.getInteger("dir"));
        type = BlockGateBarrier.EnumGateType.values()[nbt.getInteger("type")];

        locked = nbt.getBoolean("locked");
        opened = nbt.getBoolean("opened");
        rotation = opened ? 86 : 0;
        length = nbt.hasKey("length") ? nbt.getInteger("length") : 3;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt = super.writeToNBT(nbt);

        if (direction != null) nbt.setInteger("dir", direction.getHorizontalIndex());
        nbt.setInteger("type", type.ordinal());

        nbt.setBoolean("locked", locked);
        nbt.setBoolean("opened", opened);
        nbt.setInteger("length", length);
        return nbt;
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

    public float getRotation() {
        if (getOpened()) {
            if (rotation < MAX_ANGLE) rotation += ANGLE_STEP;
        } else if (rotation > MIN_ANGLE && canCloseSafely()) rotation -= ANGLE_STEP;
        return rotation;
    }

    public void setRotation() {
        if (getOpened()) rotation = MAX_ANGLE;
        else rotation = MIN_ANGLE;
    }

    public boolean canCloseSafely() {
        if (rotation < MAX_ANGLE) return true;

        BlockPos pos = getPos();
        switch (direction) {
            case SOUTH: {
                pos = pos.west(length).north();
                break;
            }
            case NORTH: {
                pos = pos.north();
                break;
            }
            case EAST: {
                pos = pos.west();
                break;
            }
            case WEST: {
                pos = pos.north(length).west();
                break;
            }
            default: {
            }
        }
        return getWorld().getEntitiesWithinAABB(EntityPlayer.class, BARRIER_BOX.offset(pos)).isEmpty();
    }

    public boolean setOpened(boolean opened) {
        if (locked) return opened;

        World world = getWorld();
        BlockPos pos = getPos();
        boolean old = this.opened;
		this.opened = opened;
        if (old != opened) {
            world.scheduleUpdate(pos, world.getBlockState(pos).getBlock(), 100);
            markDirty();
        }
        IBlockState light = world.getBlockState(pos.up());
        if (light.getBlock() instanceof BlockGateBarrier && light.getValue(BlockGateBarrier.part) == EnumPart.LIGHT)
            world.setBlockState(pos.up(), light.withProperty(BlockGateBarrier.open, opened), 3);
        return opened;
    }

    public boolean toggleOpened() {
        return setOpened(!getOpened());
    }

    public boolean getOpened() {
        return opened;
    }

    public int setLength(int length) {
        if (this.length != length) {
			this.length = length;
            markDirty();
        }
        return length;
    }

    public int getLength() {
        return length;
    }

    public boolean toggleLock() {
        setOpened(!locked);
        locked = !locked;
        markDirty();
        return getLocked();
    }

    public boolean getLocked() {
        return locked;
    }

    public EnumFacing getDirection() {
        return direction;
    }

    public BlockGateBarrier.EnumGateType getType() {
        return type;
    }
}