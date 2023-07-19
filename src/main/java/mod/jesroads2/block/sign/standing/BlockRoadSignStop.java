package mod.jesroads2.block.sign.standing;

import mod.jesroads2.block.sign.BlockRoadSign;
import mod.jesroads2.util.IBlockSwitchable;
import net.minecraft.util.IStringSerializable;

public class BlockRoadSignStop extends BlockRoadSign<BlockRoadSignStop.EnumType> implements IBlockSwitchable {
    public enum EnumType implements IStringSerializable {
        STOP, GIVEWAY, NO_ENTRY, ROAD_CLOSED;

        @Override
        public String getName() {
            return name().toLowerCase();
        }
    }

    public BlockRoadSignStop(int id) {
        super(id, "sign_stop");
    }

    @Override
    public Class<EnumType> getEnumClass() {
        return EnumType.class;
    }
}