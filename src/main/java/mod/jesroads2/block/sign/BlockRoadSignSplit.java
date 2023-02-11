package mod.jesroads2.block.sign;

import mod.jesroads2.util.IBlockSwitchable;
import net.minecraft.util.IStringSerializable;

public class BlockRoadSignSplit extends BlockRoadSign<BlockRoadSignSplit.EnumType> implements IBlockSwitchable {
    public enum EnumType implements IStringSerializable {
        KEEP_RIGHT, SPLIT, MERGE_RIGHT, MERGE;

        @Override
        public String getName() {
            return name().toLowerCase();
        }
    }

    public BlockRoadSignSplit(int id){
        super(id, "sign_split");
    }

    @Override
    public Class<EnumType> getEnumClass() {
        return EnumType.class;
    }
}