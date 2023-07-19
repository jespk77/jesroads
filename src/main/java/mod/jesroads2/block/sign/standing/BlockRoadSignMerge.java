package mod.jesroads2.block.sign.standing;

import mod.jesroads2.block.sign.BlockRoadSign;
import mod.jesroads2.util.IBlockSwitchable;
import net.minecraft.util.IStringSerializable;

public class BlockRoadSignMerge extends BlockRoadSign<BlockRoadSignMerge.EnumType> implements IBlockSwitchable {
    public enum EnumType implements IStringSerializable {
        LEFT_MERGE, RIGHT_MERGE, LEFT_WIDE, RIGHT_WIDE;

        @Override
        public String getName() {
            return name().toLowerCase();
        }
    }

    public BlockRoadSignMerge(int id) {
        super(id, "sign_merge");
    }

    @Override
    public Class<EnumType> getEnumClass() {
        return EnumType.class;
    }
}