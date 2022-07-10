package mod.jesroads2.entity.model;

import mod.jesroads2.entity.EntityCar;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderCar extends Render<EntityCar>{

	private static final ResourceLocation[] textures = {
			new ResourceLocation("jesroads2", "textures/entity/car/car_basic.png")
	};
	
	private static final ModelBase[] models = {
			new ModelCarBasic()
	};
	
	protected RenderCar(RenderManager renderManager) {
		super(renderManager);
		
		this.shadowSize = 1.5F;
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityCar entity) {
		return textures[entity.getDataManager().get(EntityCar.TYPE)];
	}
	
	private ModelBase getEntityModel(EntityCar entity){
		return models[entity.getDataManager().get(EntityCar.TYPE)];
	}
	
	@Override
	public void doRender(EntityCar entity, double x, double y, double z, float entityYaw, float partialTicks){
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y + 1.5F, z);
		GlStateManager.rotate(180.F, 0.F, 0.F, 1.F);
		GlStateManager.scale(0.65F, 0.65F, 0.65F);
		GlStateManager.rotate(entityYaw, 0.F, 1.F, 0.F);
		
		this.bindTexture(getEntityTexture(entity));
		this.getEntityModel(entity).render(entity, partialTicks, 0.F, -0.1F, entityYaw, 0.F, 0.1F);
		
		GlStateManager.popMatrix();
	}

}
