package mod.jesroads2.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;

public class PlayerReachController extends PlayerControllerMP {
	public static final int minDistance = 5;
	
	private float reachDistance;
	
	public PlayerReachController(Minecraft mc, NetHandlerPlayClient netHandler, PlayerControllerMP current) {
		super(mc, netHandler);
		
		reachDistance = super.getBlockReachDistance();
		if(current != null) setGameType(current.getCurrentGameType());
	}
	
	@Override
	public float getBlockReachDistance(){
		return reachDistance;
	}
	
	private void setBlockReachDistance(float distance){
		if(distance < minDistance) reachDistance = minDistance;
		else reachDistance = distance;
	}
	
	public static void setReachDistance(Minecraft minecraft, EntityPlayer player, float distance){
		if(minecraft.player != player) return;
		
		if(!(minecraft.playerController instanceof PlayerReachController)) minecraft.playerController = new PlayerReachController(minecraft, minecraft.player.connection, minecraft.playerController);
		((PlayerReachController) minecraft.playerController).setBlockReachDistance(distance);
	}
}