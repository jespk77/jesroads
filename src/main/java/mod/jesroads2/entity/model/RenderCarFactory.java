package mod.jesroads2.entity.model;

import mod.jesroads2.entity.EntityCar;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderCarFactory implements IRenderFactory<EntityCar> {

	@Override
	public Render<EntityCar> createRenderFor(RenderManager manager) {
		return new RenderCar(manager);
	}

}
