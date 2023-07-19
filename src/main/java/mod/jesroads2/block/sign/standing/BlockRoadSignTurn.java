package mod.jesroads2.block.sign.standing;

import mod.jesroads2.block.sign.BlockRoadSign;
import mod.jesroads2.util.IBlockSwitchable;
import net.minecraft.util.IStringSerializable;

public class BlockRoadSignTurn extends BlockRoadSign<BlockRoadSignTurn.EnumType> implements IBlockSwitchable {
    public enum EnumType implements IStringSerializable {
        LEFT, RIGHT, NO_LEFT, NO_RIGHT;

        @Override
        public String getName() {
            return name().toLowerCase();
        }
    }

    public BlockRoadSignTurn(int id) {
        super(id, "sign_turning");
    }

    @Override
    public Class<EnumType> getEnumClass() {
        return EnumType.class;
    }
}