package mod.jesroads2.block.sign.standing;

import mod.jesroads2.block.sign.BlockRoadSign;
import mod.jesroads2.util.IBlockSwitchable;
import net.minecraft.util.IStringSerializable;

public class BlockRoadSignMove extends BlockRoadSign<BlockRoadSignMove.EnumType> implements IBlockSwitchable {
    public enum EnumType implements IStringSerializable {
        LEFT, RIGHT, ROADWORKS_LEFT, ROADWORKS_RIGHT;

        @Override
        public String getName() {
            return name().toLowerCase();
        }
    }

    public BlockRoadSignMove(int id) {
        super(id, "sign_turn");
    }

    @Override
    public Class<EnumType> getEnumClass() {
        return EnumType.class;
    }
}