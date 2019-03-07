package de.maxhenkel.corpse.entities;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderFactoryCorpse implements IRenderFactory {
    @Override
    public Render createRenderFor(RenderManager manager) {
        return new RenderCorpse(manager);
    }
}
