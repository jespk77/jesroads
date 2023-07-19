package mod.jesroads2.block.sign.standing;

import mod.jesroads2.block.sign.BlockRoadSign;
import mod.jesroads2.util.IBlockSwitchable;
import net.minecraft.util.IStringSerializable;

public class BlockRoadSignInfo extends BlockRoadSign<BlockRoadSignInfo.EnumType> implements IBlockSwitchable {
	public enum EnumType implements IStringSerializable{
		REST_AREA, EXIT, ROADWORKS, ROADWORKS_END;

		@Override
		public String getName() {
			return name().toLowerCase();
		}
	}

	public BlockRoadSignInfo(int id){
		super(id, "sign_info");
	}

    @Override
    public Class<EnumType> getEnumClass() {
        return EnumType.class;
    }
}