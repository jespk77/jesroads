package mod.jesroads2.handler;

import java.util.Calendar;
import java.util.Random;

import net.minecraft.util.math.BlockPos;

import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.BiomeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HandlerWeatherEffects {
	private final static Calendar calendar = Calendar.getInstance();
	private final static Random random = new Random();
	
	//private float fogDensity = 0, nextDensity = 0;
	
	public HandlerWeatherEffects(){
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void setGrassColor(BiomeEvent.GetGrassColor event){
		Biome biome = event.getBiome();
		if(biome.getTemperature(BlockPos.ORIGIN) > 1.1F) return;
		else if(biome.isSnowyBiome()){ event.setNewColor(0xFFFFFF); return; }
		
		switch(calendar.get(Calendar.MONTH)){
		case Calendar.DECEMBER: case Calendar.JANUARY: case Calendar.FEBRUARY:{
			int white = 220 + random.nextInt(35);
			event.setNewColor((white << 16) | (white << 8) | white);
			break;
		}
		case Calendar.MARCH: case Calendar.APRIL: case Calendar.JUNE:{
			event.setNewColor((50 + random.nextInt(30) << 16) | (150 + random.nextInt(70) << 8) | 55);
			break;
		}
		case Calendar.JULY: case Calendar.AUGUST:{
			event.setNewColor((90 + random.nextInt(60) << 16) | (110 + random.nextInt(50) << 8) | 55);
			break;
		}
		case Calendar.SEPTEMBER: case Calendar.OCTOBER: case Calendar.NOVEMBER:{
			event.setNewColor((150 + random.nextInt(50) << 16) | (130 + random.nextInt(50) << 8) | 50);
			break;
		}
		}
	}
	
	@SubscribeEvent
	public void setFoliageColor(BiomeEvent.GetFoliageColor event){
		Biome biome = event.getBiome();
		if(biome.getTemperature(BlockPos.ORIGIN) > 1.1F) return;
		else if(biome.isSnowyBiome()){ event.setNewColor(0xFFFFFF); return; }
		
		switch(calendar.get(Calendar.MONTH)){
		case Calendar.DECEMBER: case Calendar.JANUARY: case Calendar.FEBRUARY:{
			int white = 180 + random.nextInt(75);
			event.setNewColor((white << 16) | (white << 8) | white);
			break;
		}
		case Calendar.SEPTEMBER: case Calendar.OCTOBER: case Calendar.NOVEMBER:{
			event.setNewColor((220 + random.nextInt(30) << 16) | (100 + random.nextInt(150) << 8));
			break;
		}
		}
	}
	
	/*@SubscribeEvent
	public void setFogDensity(EntityViewRenderEvent.FogDensity event){
		JesRoads2.ensureDevelopmentMode("fog_density");
		if(this.fogDensity > this.nextDensity){ System.out.println("fog decreasing..."); this.fogDensity -= 0.005F; }
		else if(this.fogDensity < this.nextDensity){ System.out.println("fog increasing..."); this.fogDensity += 0.005F; }
		else{
			int rnd = random.nextInt(100);
			if(rnd == 0){
				int r = random.nextInt(100);
				if(r < 60) this.nextDensity = 0.01F;
				else if(r < 75) this.nextDensity = 0.02F;
				else if(r < 85) this.nextDensity = 0.03F;
				else if(r < 97) this.nextDensity = 0.04F;
				else this.nextDensity = 0.05F;
			} else if(rnd == 1) this.nextDensity = 0.F;
		}
		
		event.setCanceled(true);
		event.setDensity(fogDensity);
		GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
	}*/
}