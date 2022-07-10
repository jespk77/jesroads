package mod.jesroads2.proxy;

import mod.jesroads2.client.renderer.*;
import mod.jesroads2.entity.EntityCar;
import mod.jesroads2.entity.model.RenderCarFactory;
import mod.jesroads2.tileentity.*;
import mod.jesroads2.tileentity.dynamicsigns.TileEntityDynamicSignController;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy implements ICommonProxy {
    @Override
    public void preInitProxies() {
        RenderingRegistry.registerEntityRenderingHandler(EntityCar.class, new RenderCarFactory());
    }

    @Override
    public void initProxies() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRoadSlope.class, new RendererBlockRoadSlope());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRoadSign.class, new RendererBlockSign());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGateBarrier.class, new RendererBlockBarrierGate());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMemory.class, new RendererBlockMemoryBeam());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDynamicSignController.class, new RendererBlockBound<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityIntersectionController.class, new RendererBlockBound<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGateController.class, new RendererBlockBound<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRoadDetector.class, new RendererBlockBound<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFloodlightController.class, new RendererBlockBound<>());
    }
}