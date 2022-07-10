package mod.jesroads2.handler;

import mod.jesroads2.item.ItemRoadBuilder;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HandlerEntity {
	public HandlerEntity(){
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	/*@SubscribeEvent
	public void onEntityDismount(EntityMountEvent event){
		if(event.isDismounting()){
			Entity mount = event.getEntityBeingMounted();
			if(mount instanceof EntityDrivable){
				EntityDrivable vehicle = (EntityDrivable) mount;
				if(vehicle.motionX == 0D && vehicle.motionZ == 0D){
					Entity driver = event.getEntityMounting();
					if(driver instanceof EntityPlayer)
						driver.setPosition(vehicle.posX - vehicle.driverX, vehicle.posY - vehicle.driverY, vehicle.posZ - vehicle.driverZ);
				} //else event.setCanceled(true);
			}
		} else if(event.isMounting()){
			Entity mount = event.getEntityBeingMounted();
			if(mount instanceof EntityDrivable){
				EntityDrivable vehicle = (EntityDrivable) mount;
				Entity driver = event.getEntityMounting();
				if(driver instanceof EntityPlayer) vehicle.setExitPos(driver.posX, driver.posY, driver.posZ);
			}
		}
	}*/
	
	@SubscribeEvent
	public void onBlockInteract(PlayerInteractEvent event){
		EntityPlayer player = event.getEntityPlayer();
		if(GuiScreen.isCtrlKeyDown() && player.getHeldItemMainhand().getItem() == Items.AIR)
			ItemRoadBuilder.teleportPlayer(player);
	}
}