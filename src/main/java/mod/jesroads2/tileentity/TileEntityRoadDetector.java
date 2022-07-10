package mod.jesroads2.tileentity;

import mod.jesroads2.block.system.BlockGateController;
import mod.jesroads2.block.system.BlockIntersectionController;
import mod.jesroads2.block.system.BlockTrafficlight;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityRoadDetector extends TileEntityAgeble implements ITileEntityBindable {
    private BlockPos controller, light;
    private boolean isBinding;

    public TileEntityRoadDetector() {
    }

    public TileEntityRoadDetector(World world) {
        super(world);
    }

    public TileEntityRoadDetector(NBTTagCompound nbt) {
        super(nbt);
    }

    @Override
    public void readFromNBT(NBTTagCompound t) {
        super.readFromNBT(t);
        readNBT(t);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound t) {
        t = super.writeToNBT(t);
        return getNBT(t);
    }

    public NBTTagCompound getNBT() {
        return getNBT(new NBTTagCompound());
    }

    private NBTTagCompound getNBT(NBTTagCompound nbt) {
        nbt.setBoolean("isBinding", isBinding);
        if (controller != null) {
            nbt.setInteger("controllerX", controller.getX());
            nbt.setInteger("controllerY", controller.getY());
            nbt.setInteger("controllerZ", controller.getZ());
        }
        if (light != null) {
            nbt.setInteger("lightX", light.getX());
            nbt.setInteger("lightY", light.getY());
            nbt.setInteger("lightZ", light.getZ());
        }
        return nbt;
    }

    private void readNBT(NBTTagCompound nbt) {
        isBinding = nbt.getBoolean("isBinding");
        if (nbt.hasKey("controllerX") && nbt.hasKey("controllerY") && nbt.hasKey("controllerZ"))
            controller = new BlockPos(nbt.getInteger("controllerX"), nbt.getInteger("controllerY"), nbt.getInteger("controllerZ")).toImmutable();
        else controller = null;
        if (nbt.hasKey("lightX") && nbt.hasKey("lightY") && nbt.hasKey("lightZ"))
            light = new BlockPos(nbt.getInteger("lightX"), nbt.getInteger("lightY"), nbt.getInteger("lightZ")).toImmutable();
        else light = null;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(super.getUpdateTag());
    }

    @Override
    @SideOnly(Side.SERVER)
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, writeToNBT(new NBTTagCompound()));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onDataPacket(NetworkManager network, SPacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
    }

    @Override
    public void onAging() {
        calendar = null;
        placetime = -1;
        markDirty();
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState old, IBlockState nw) {
        return old.getBlock() != nw.getBlock();
    }

    @Override
    public String displayBinds() {
        bindCheck();
        StringBuilder s = new StringBuilder();
        if (controller != null)
            s.append("Controller    [x=").append(controller.getX()).append(", y=").append(controller.getY()).append(", z=").append(controller.getZ()).append("]");
        if (light != null) {
            IBlockState ls = getWorld().getBlockState(light);
            s.append("\nTraffic Light [x= ").append(light.getX()).append(", y=").append(light.getY()).append(", z=").append(light.getZ()).append("] Direction=")
                    .append(ls.getValue(BlockTrafficlight.facing).getName().toLowerCase()).append(", Type=").append(((BlockTrafficlight) ls.getBlock()).type.getName().toLowerCase());
        }
        return s.toString();
    }

    @Override
    public void bindCheck() {
        if (controller != null) {
            Block b = getWorld().getBlockState(controller).getBlock();
            if (!(b instanceof BlockIntersectionController) && !(b instanceof BlockGateController)) {
                controller = null;
                markDirty();
            }
        }
        if (light != null)
            if (!(getWorld().getBlockState(light).getBlock() instanceof BlockTrafficlight)) {
                light = null;
                markDirty();
            }
    }

    public void setDetected(EntityPlayer detected, boolean simulated) {
        if (controller != null) {
            TileEntity tile = getWorld().getTileEntity(controller);
            if (light != null) {
                if (tile instanceof TileEntityIntersectionController)
                    ((TileEntityIntersectionController) tile).updateDetectedState(light, detected != null, simulated);
                else controller = null;
            } else if (!simulated) {
                if (tile instanceof TileEntityGateController) ((TileEntityGateController) tile).setUser(detected, true);
                else controller = null;
            }
        }
    }

    @Override
    public String addBind(BlockPos p) {
        if (p == null) return "-1";

        Block b = getWorld().getBlockState(p).getBlock();
        if (b instanceof BlockIntersectionController || b instanceof BlockGateController) {
            if (controller == null || !controller.equals(p)) {
                controller = p.toImmutable();
                return "1";
            } else {
                controller = null;
                return "0";
            }
        } else if (b instanceof BlockTrafficlight) {
            if (light == null || !light.equals(p)) {
                light = p.toImmutable();
                return "3";
            } else {
                light = null;
                return "2";
            }
        } else return "-1";
    }

    @Override
    public String onStartBind() {
        isBinding = true;
        return "Road Detector";
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
