package mod.jesroads2.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;

public class PlayerReachController extends PlayerControllerMP {
	public static final int minDistance = 5;

	private static final int focusedDistance = 5;

	private boolean isFocused;
	
	private float reachDistance;
	
	public PlayerReachController(Minecraft mc, NetHandlerPlayClient netHandler, PlayerControllerMP current) {
		super(mc, netHandler);

		isFocused = false;
		reachDistance = super.getBlockReachDistance();
		if(current != null) setGameType(current.getCurrentGameType());
	}
	
	@Override
	public float getBlockReachDistance(){
		return isFocused ? focusedDistance : reachDistance;
	}
	
	private void setBlockReachDistance(float distance){
		if(distance < minDistance) reachDistance = minDistance;
		else reachDistance = distance;
	}

	private static PlayerReachController GetReachController(Minecraft minecraft){
		if(!(minecraft.playerController instanceof PlayerReachController))
			minecraft.playerController = new PlayerReachController(minecraft, minecraft.player.connection, minecraft.playerController);
		return ((PlayerReachController) minecraft.playerController);
	}
	
	public static void setReachDistance(Minecraft minecraft, EntityPlayer player, float distance){
		if(minecraft.player == null || minecraft.player != player) return;
		GetReachController(minecraft).setBlockReachDistance(distance);
	}

	public static void setFocused(Minecraft minecraft, EntityPlayer player, boolean focused){
		if(minecraft.player == null || minecraft.player != player) return;
		GetReachController(minecraft).isFocused = focused;
	}
}