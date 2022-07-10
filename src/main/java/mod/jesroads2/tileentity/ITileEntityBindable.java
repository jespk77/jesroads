package mod.jesroads2.tileentity;

import net.minecraft.util.math.BlockPos;

public interface ITileEntityBindable {
	String onStartBind();
	String addBind(BlockPos pos);
	String displayBinds();
	void bindCheck();
	boolean isBinding();
	void onStopBind();
}