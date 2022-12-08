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
	
	public HandlerWeatherEffects(){
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void setGrassColor(BiomeEvent.GetGrassColor event){
		Biome biome = event.getBiome();
		if(biome.getTemperature(BlockPos.ORIGIN) > 1.1F) return;
		else if(biome.isSnowyBiome()){ event.setNewColor(0xFFFFFF); return; }
		
		switch(calendar.get(Calendar.MONTH)){
		case Calendar.JANUARY: case Calendar.FEBRUARY:{
			event.setNewColor((180 + random.nextInt(30) << 16) | (170 + random.nextInt(50) << 8) | 120);
			break;
		}
		case Calendar.MARCH: case Calendar.APRIL: case Calendar.JUNE: {
			event.setNewColor((50 + random.nextInt(30) << 16) | (150 + random.nextInt(70) << 8) | 55);
			break;
		}
		case Calendar.JULY: case Calendar.AUGUST: case Calendar.SEPTEMBER: {
			event.setNewColor((90 + random.nextInt(60) << 16) | (110 + random.nextInt(50) << 8) | 55);
			break;
		}
		case Calendar.OCTOBER: case Calendar.NOVEMBER: case Calendar.DECEMBER: {
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
			case Calendar.JANUARY: case Calendar.FEBRUARY:{
				event.setNewColor((180 + random.nextInt(30) << 16) | (170 + random.nextInt(50) << 8) | 120);
				break;
			}
			case Calendar.DECEMBER: case Calendar.OCTOBER: case Calendar.NOVEMBER:{
				event.setNewColor((220 + random.nextInt(30) << 16) | (100 + random.nextInt(150) << 8));
				break;
			}
		}
	}
}