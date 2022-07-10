package mod.jesroads2.entity.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelCarBasic extends ModelBase{
    private final ModelRenderer bottom;
    private final ModelRenderer wheelLB;
    private final ModelRenderer wheelLF;
    private final ModelRenderer wheelRB;
    private final ModelRenderer wheelRF;
    private final ModelRenderer trunk;
    private final ModelRenderer hood;
    private final ModelRenderer pillarLB;
    private final ModelRenderer pillarLF;
    private final ModelRenderer pillarRB;
    private final ModelRenderer pillarRF;
    private final ModelRenderer roof;
  
  public ModelCarBasic(){
	  textureWidth = 256;
	  textureHeight = 64;
    
      bottom = new ModelRenderer(this, 0, 0).addBox(0F, 0F, 0F, 32, 2, 40).setTextureSize(256, 64);
      bottom.setRotationPoint(-16F, 14F, -20F);
      bottom.mirror = true;
      setRotation(bottom, 0F, 0F, 0F);
      
      wheelLB = new ModelRenderer(this, 0, 0).addBox(0F, 0F, 0F, 3, 8, 8).setTextureSize(256, 64);
      wheelLB.setRotationPoint(12F, 16F, 9F);
      wheelLB.mirror = true;
      setRotation(wheelLB, 0F, 0F, 0F);
      
      wheelLF = new ModelRenderer(this, 0, 0).addBox(0F, 0F, 0F, 3, 8, 8).setTextureSize(256, 64);
      wheelLF.setRotationPoint(12F, 16F, -17F);
      wheelLF.mirror = true;
      setRotation(wheelLF, 0F, 0F, 0F);
      
      wheelRB = new ModelRenderer(this, 0, 0).addBox(0F, 0F, 0F, 3, 8, 8).setTextureSize(256, 64);
      wheelRB.setRotationPoint(-15F, 16F, 9F);
      wheelRB.mirror = true;
      setRotation(wheelRB, 0F, 0F, 0F);
      
      wheelRF = new ModelRenderer(this, 0, 0).addBox(0F, 0F, 0F, 3, 8, 8).setTextureSize(256, 64);
      wheelRF.setRotationPoint(-15F, 16F, -17F);
      wheelRF.mirror = true;
      setRotation(wheelRF, 0F, 0F, 0F);
      
      trunk = new ModelRenderer(this, 0, 0).addBox(0F, 0F, 0F, 32, 8, 12).setTextureSize(256, 64);
      trunk.setRotationPoint(-16F, 6F, 7F);
      trunk.mirror = true;
      setRotation(trunk, 0F, 0F, 0F);
      
      hood = new ModelRenderer(this, 0, 0).addBox(0F, 0F, 0F, 32, 8, 12).setTextureSize(256, 64);
      hood.setRotationPoint(-16F, 6F, -19F);
      hood.mirror = true;
      setRotation(hood, 0F, 0F, 0F);
      
      pillarLB = new ModelRenderer(this, 0, 0).addBox(0F, 0F, 0F, 3, 12, 3).setTextureSize(256, 64);
      pillarLB.setRotationPoint(-16F, -6F, -10F);
      pillarLB.mirror = true;
      setRotation(pillarLB, 0F, 0F, 0F);
      
      pillarLF = new ModelRenderer(this, 0, 0).addBox(0F, 0F, 0F, 3, 12, 3).setTextureSize(256, 64);
      pillarLF.setRotationPoint(-16F, -6F, 7F);
      pillarLF.mirror = true;
      setRotation(pillarLF, 0F, 0F, 0F);
      
      pillarRB = new ModelRenderer(this, 0, 0).addBox(0F, 0F, 0F, 3, 12, 3).setTextureSize(256, 64);
      pillarRB.setRotationPoint(13F, -6F, -10F);
      pillarRB.mirror = true;
      setRotation(pillarRB, 0F, 0F, 0F);
      
      pillarRF = new ModelRenderer(this, 0, 0).addBox(0F, 0F, 0F, 3, 12, 3).setTextureSize(256, 64);
      pillarRF.setRotationPoint(13F, -6F, 7F);
      pillarRF.mirror = true;
      setRotation(pillarRF, 0F, 0F, 0F);
      
      roof = new ModelRenderer(this, 0, 0).addBox(0F, 0F, 0F, 32, 2, 20).setTextureSize(256, 64);
      roof.setRotationPoint(-16F, -8F, -10F);
      roof.mirror = true;
      setRotation(roof, 0F, 0F, 0F);
  }
  
  public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
	  super.render(entity, f, f1, f2, f3, f4, f5);
	  setRotationAngles(f, f1, f2, f3, f4, f5, entity);
	  bottom.render(f5);
	  wheelLB.render(f5);
	  wheelLF.render(f5);
	  wheelRB.render(f5);
	  wheelRF.render(f5);
	  trunk.render(f5);
	  hood.render(f5);
	  pillarLB.render(f5);
	  pillarLF.render(f5);
	  pillarRB.render(f5);
	  pillarRF.render(f5);
	  roof.render(f5);
  }
  
  private void setRotation(ModelRenderer model, float x, float y, float z){
	  model.rotateAngleX = x;
	  model.rotateAngleY = y;
	  model.rotateAngleZ = z;
  }
  
  public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity){
	  super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
  }

}
