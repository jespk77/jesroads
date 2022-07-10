package mod.jesroads2.block.system;

import java.util.List;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.BlockBaseHorizontal;
import mod.jesroads2.util.IBlockSwitchable;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockTrafficlight extends BlockBaseHorizontal implements IBlockSwitchable {
	public enum EnumTrafficLightType implements IStringSerializable{
		LEFT(0, "left"),
		STRAIGHT(1, "straight"),
		RIGHT(2, "right");

		public final int id;
		public final String name;

		private static final EnumTrafficLightType[] list = new EnumTrafficLightType[values().length];

		EnumTrafficLightType(int id, String name){
			this.id = id;
			this.name = name;
		}

		@Override
		public String getName(){
			return name;
		}

		public static EnumTrafficLightType fromID(int id){
			if( id > 0 && id < list.length) return list[id];
			else return list[0];
		}

		static{
			for(EnumTrafficLightType t: values())
				list[t.id] = t;
		}
	}

	public enum EnumTrafficLightState implements IStringSerializable{
		OFF(0, "off"),
		RED(1, "red"),
		YELLOW(2, "yellow"),
		GREEN(3, "green");

		public final int id;
		public final String name;

		public static final EnumTrafficLightState[] list = new EnumTrafficLightState[values().length];

		EnumTrafficLightState(int id, String name){
			this.id = id;
			this.name = name;
		}

		@Override
		public String getName(){
			return name;
		}

		public boolean isClear(){
			return this == OFF || this == GREEN;
		}

		public static EnumTrafficLightState fromID(int id){
			if( id > 0 && id < list.length ) return list[id];
			else return OFF;
		}

		static{
			for(EnumTrafficLightState s: values())
				list[s.id] = s;
		}
	}

	public static final PropertyEnum<EnumTrafficLightState> sign = PropertyEnum.create("state", EnumTrafficLightState.class);
	public static final PropertyBool left = PropertyBool.create("left"), right = PropertyBool.create("right");

	public final EnumTrafficLightType type;

	private static final AxisAlignedBB BLOCK_AABB = new AxisAlignedBB(0.2F, 0.F, 0.2F, 0.8F, 1.F, 0.8F);

	public BlockTrafficlight(int id, EnumTrafficLightType type){
		super(id, new Material(MapColor.BLACK), "trafficlight_" + type.name, JesRoads2.tabs.system);

		this.type = type;
		setHardness(1.5F).setResistance(5.F);
		setFullCube(false);
		setDefaultState(getDefaultState().withProperty(sign, EnumTrafficLightState.OFF)
				.withProperty(left, false).withProperty(right, false));
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos){
		float minX = 0.25F, minY = 0.F, minZ = minX, maxX = 0.75F, maxY = 1.F, maxZ = maxX;
		state = getActualState(state, world, pos);
		EnumFacing face = state.getValue(facing);
		if(face == EnumFacing.NORTH || face == EnumFacing.SOUTH){
			if(state.getValue(right)) minX = 0.F;
			if(state.getValue(left)) maxX = 1.F;
		} else if(face == EnumFacing.EAST || face == EnumFacing.WEST){
			if(state.getValue(right)) minZ = 0.F;
			if(state.getValue(left)) maxZ = 1.F;
		}
		return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing enfacing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase entity){
		if(entity instanceof EntityPlayer) ((EntityPlayer) entity).addStat(StatList.getObjectUseStats(item));
		return getDefaultState().withProperty(facing, entity.getHorizontalFacing().getOpposite());
	}

	@Override
	public int tickRate(World world){
		return 60;
	}

	@Override
	public IBlockState getStateFromMeta(int meta){
		IBlockState st = super.getStateFromMeta(meta);
		st = st.withProperty(sign, EnumTrafficLightState.fromID((meta & 12) >> 2));
		return st;
	}

	@Override
	public int getMetaFromState(IBlockState state){
		return super.getMetaFromState(state) | (state.getValue(sign).id << 2);
	}

	@Override
	protected void createProperties(List<IProperty<?>> properties){
		super.createProperties(properties);
		properties.add(sign);
		properties.add(left);
		properties.add(right);
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos){
		EnumFacing face = state.getValue(facing);
		EnumFacing rside = getRight(face), lside = rside.getOpposite();
		return state.withProperty(right, canConnect(world, pos.offset(rside))).withProperty(left, canConnect(world, pos.offset(lside)));
	}

	private EnumFacing getRight(EnumFacing facing){
		return facing.rotateY();
	}

	private boolean canConnect(IBlockAccess world, BlockPos pos){
		Block block = world.getBlockState(pos).getBlock();
		return block instanceof BlockTrafficlight || block instanceof BlockTrafficlightSupport;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entity, ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ){
		Block block = stack != null ? Block.getBlockFromItem(stack.getItem()) : null;
		if(block instanceof BlockTrafficlightSupport && !world.isRemote){
			world.setBlockState(pos, JesRoads2.blocks.traffic_light_support.getDefaultState(), 3);
			entity.addStat(StatList.getBlockStats(this));
		}return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getPackedLightmapCoords(IBlockState state, IBlockAccess source, BlockPos pos){
		return JesRoads2.options.other.glowing_textures? super.getPackedLightmapCoords(state, source, pos): 15728880;
	}

	@Override
	public ItemStack getSwitchBlock(ItemStack current) {
		if(type == null) return null;
		else return new ItemStack(JesRoads2.blocks.traffic_light[EnumTrafficLightType.fromID(type.id + 1).id]);
	}
}