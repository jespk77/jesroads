package mod.jesroads2.block.sign;

import mod.jesroads2.util.IBlockSwitchable;
import net.minecraft.util.IStringSerializable;

public class BlockRoadSignDetour extends BlockRoadSign<BlockRoadSignDetour.EnumType> implements IBlockSwitchable {
    public enum EnumType implements IStringSerializable {
        DETOUR_LEFT, DETOUR_RIGHT, DETOUR_UP, DETOUR_END;

        @Override
        public String getName() {
            return name().toLowerCase();
        }
    }

    public BlockRoadSignDetour(int id) {
        super(id, "sign_detour");
    }

    @Override
    public Class<EnumType> getEnumClass() {
        return EnumType.class;
    }
}