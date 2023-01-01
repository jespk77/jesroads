package mod.jesroads2.block.sign;

import java.util.List;

import mod.jesroads2.block.BlockBaseHorizontal;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockSignPost extends BlockBaseHorizontal {
    public enum EnumSize implements IStringSerializable {
        LARGE(0, "large"),
        SMALL(1, "small");

        public final int id;
        public final String name;

        public static final EnumSize[] list = new EnumSize[values().length];

        EnumSize(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        public static EnumSize fromID(int id) {
            if (id > 0 && id < list.length) return list[id];
            else return LARGE;
        }

        static {
            for (EnumSize s : values()) {
                list[s.id] = s;
            }
        }
    }

    public static final PropertyEnum<EnumSize> size = PropertyEnum.create("size", EnumSize.class);

    public BlockSignPost(int id) {
        super(id, new Material(MapColor.GRAY), "signpost", null, true);

        setHardness(0.7F).setResistance(20.F);
		setFullCube(false);
		setDefaultState(getDefaultState().withProperty(size, EnumSize.LARGE));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        int var = (meta & 12) >> 2;
        return super.getStateFromMeta(meta).withProperty(size, EnumSize.fromID(var));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return super.getMetaFromState(state) | (state.getValue(size).id << 2);
    }

    @Override
    protected void createProperties(List<IProperty<?>> properties) {
        super.createProperties(properties);
        properties.add(size);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing enfacing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase entity) {
        return super.getStateForPlacement(world, pos, enfacing, hitX, hitY, hitZ, meta, entity)
                .withProperty(size, EnumSize.fromID(entity.getHeldItemMainhand().getItemDamage()));
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighbor, BlockPos npos) {
        BlockPos next = pos.up();
        if (isAir(world.getBlockState(next), world, pos)) world.setBlockToAir(pos);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        float minX, minZ, maxX, maxZ;
        float xWidth, zWidth;
        switch (state.getValue(facing)) {
            case SOUTH: {
                if (state.getValue(size) == EnumSize.SMALL) {
                    minX = 0.4F;
                    xWidth = 0.2F;
                    minZ = 0.8F;
                    zWidth = 0.2F;
                } else {
                    minX = 0.4F;
                    xWidth = 0.2F;
                    minZ = 0.525F;
                    zWidth = 0.2F;
                }
                break;
            }
            case WEST: {
                if (state.getValue(size) == EnumSize.SMALL) {
                    minX = 0.0F;
                    xWidth = 0.2F;
                    minZ = 0.4F;
                    zWidth = 0.2F;
                } else {
                    minX = 0.275F;
                    xWidth = 0.2F;
                    minZ = 0.4F;
                    zWidth = 0.2F;
                }
                break;
            }
            case EAST: {
                if (state.getValue(size) == EnumSize.SMALL) {
                    minX = 0.8F;
                    xWidth = 0.2F;
                    minZ = 0.4F;
                    zWidth = 0.2F;
                } else {
                    minX = 0.525F;
                    xWidth = 0.2F;
                    minZ = 0.4F;
                    zWidth = 0.2F;
                }
                break;
            }
            default: {
                if (state.getValue(size) == EnumSize.SMALL) {
                    minX = 0.4F;
                    xWidth = 0.2F;
                    minZ = 0.0F;
                    zWidth = 0.2F;
                } else {
                    minX = 0.4F;
                    xWidth = 0.2F;
                    minZ = 0.275F;
                    zWidth = 0.2F;
                }
                break;
            }
        }
        maxX = minX + xWidth;
        maxZ = minZ + zWidth;
        return new AxisAlignedBB(minX, 0.F, minZ, maxX, 1.F, maxZ);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> stack) {
        for (EnumSize s : EnumSize.values()) {
            stack.add(new ItemStack(this, 1, s.id));
        }
    }

    @Override
    public int damageDropped(IBlockState state) {
        return getMetaFromState(state);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        state = world.getBlockState(pos.up());
        Block b = state.getBlock();
        if (b instanceof BlockRoadSign) {
            BlockRoadSign sign = (BlockRoadSign) b;
            return new ItemStack(sign, 1, sign.getMetaFromState(state));
        } else return new ItemStack(Items.AIR);
    }
}