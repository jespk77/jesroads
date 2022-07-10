package mod.jesroads2.block.streetlight;

import java.util.List;

import mod.jesroads2.JesRoads2;
import mod.jesroads2.block.BlockBaseHorizontal;
import mod.jesroads2.util.IBlockSwitchable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockRoadLight extends BlockBaseHorizontal implements IBlockSwitchable {
    public enum EnumRoadLightType {
        SIDE(0),
        CENTERED(1);

        public final int id;

        EnumRoadLightType(int id) {
            this.id = id;
        }

        public static EnumRoadLightType fromID(int id) {
            EnumRoadLightType[] values = EnumRoadLightType.values();
            if (id > 0 && id < values.length) return values[id];
            else return values[0];
        }
    }

    public static final Material material = new Material(MapColor.BLACK);
    public static final AxisAlignedBB[] box_side = new AxisAlignedBB[]{
            new AxisAlignedBB(0.F, 0.F, 0.3F, 0.3F, 0.1F, 0.7F),
            new AxisAlignedBB(0.3F, 0.F, 0.F, 0.7F, 0.1F, 0.3F),
            new AxisAlignedBB(0.7F, 0.F, 0.3F, 1.F, 0.1F, 0.7F),
            new AxisAlignedBB(0.3F, 0.F, 0.7F, 0.7F, 0.1F, 1.F)};
    public static final AxisAlignedBB box_centered = new AxisAlignedBB(0.3F, 0.F, 0.3F, 0.7F, 0.1F, 0.7F);

    public final EnumRoadLightType type;

    public BlockRoadLight(int id, EnumRoadLightType type) {
        super(id, material, "roadlight_" + type.name().toLowerCase(), JesRoads2.tabs.road_extra);

        this.type = type;
        setHardness(0.3F);
        setLightLevel(1.F);
		setFullCube(false);
	}

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (true) return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
        if (type == EnumRoadLightType.CENTERED) return box_centered;

        int id = state.getValue(facing).getHorizontalIndex();
        if (id > 0 && id < box_side.length) return box_side[id];
        else return box_side[0];
    }

    @Override
    public boolean isPassable(IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public boolean isCollidable() {
        return false;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean unkown) {

    }

    @Override
    public ItemStack getSwitchBlock(ItemStack current) {
        if (type == null) return null;

        switch (type) {
            case SIDE:
                return new ItemStack(JesRoads2.blocks.road_light[1]);
            case CENTERED:
                return new ItemStack(JesRoads2.blocks.road_light[0]);
            default:
                return null;
        }
    }
}