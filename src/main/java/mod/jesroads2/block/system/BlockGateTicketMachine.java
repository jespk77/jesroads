package mod.jesroads2.block.system;

import java.util.List;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.BlockBaseHorizontal;
import mod.jesroads2.item.ItemGateTicket;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockGateTicketMachine extends BlockBaseHorizontal {

    public static final PropertyEnum<BlockGateBarrier.EnumPart> part = PropertyEnum.create("part", BlockGateBarrier.EnumPart.class);

    public BlockGateTicketMachine(int id) {
        super(id, new Material(MapColor.GRAY), "ticket_machine", JesRoads2.tabs.system);

        setHardness(1.F).setResistance(3.F);
        setFullCube(false);
        setDefaultState(getDefaultState().withProperty(part, BlockGateBarrier.EnumPart.BOTTOM));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return super.getStateFromMeta(meta).withProperty(part, BlockGateBarrier.EnumPart.fromID((meta & 12) >> 2));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return super.getMetaFromState(state) | (state.getValue(part).id << 2);
    }

    @Override
    protected void createProperties(List<IProperty<?>> properties) {
        super.createProperties(properties);
        properties.add(part);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing enfacing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase entity) {
        IBlockState state = super.getStateForPlacement(world, pos, enfacing, hitX, hitY, hitZ, meta, entity);
        world.setBlockState(pos.up(), state.withProperty(part, BlockGateBarrier.EnumPart.TOP), 2);
        return state.withProperty(part, BlockGateBarrier.EnumPart.BOTTOM);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos neighbor) {
        if (!isSupported(world, pos, state)) world.setBlockToAir(pos);
    }

    private boolean isSupported(World world, BlockPos pos, IBlockState state) {
        switch (state.getValue(part)) {
            case BOTTOM:
                return world.getBlockState(pos.up()).getBlock() instanceof BlockGateTicketMachine;
            case TOP:
            case LIGHT:
                return world.getBlockState(pos.down()).getBlock() instanceof BlockGateTicketMachine;
            default:
                return false;
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getValue(part).box;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entity, ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ) {
        String msg;
        if (stack.getItem() instanceof ItemGateTicket) {
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt != null && !nbt.getBoolean("accepted") && !ItemGateTicket.isReusable(nbt) && !ItemGateTicket.isToll(nbt)) {
                nbt.setUniqueId("owner", entity.getPersistentID());
                nbt.setBoolean("accepted", true);
                msg = "Ticket activated";
            } else msg = null;
        } else msg = null;
        if (world.isRemote && msg != null) JesRoads2.handlerOverlay.getMessage().addMessage(msg);
        return msg != null;
    }
}