package net.zestyblaze.nomadbooks.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.zestyblaze.nomadbooks.NomadBooks;
import net.zestyblaze.nomadbooks.client.particle.CampfireLimitParticle;
import net.zestyblaze.nomadbooks.util.Constants;


@SuppressWarnings("deprecation")
@Environment(EnvType.CLIENT)
public class NomadBooksClient implements ClientModInitializer {
    public static final SimpleParticleType CAMP_LIMIT = Registry.register(BuiltInRegistries.PARTICLE_TYPE, "nomadbooks:camp_limit", FabricParticleTypes.simple(true));

    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(NomadBooks.MEMBRANE, RenderType.translucent());

        FabricModelPredicateProviderRegistry.register(new ResourceLocation(Constants.DEPLOYED), ((itemStack, clientLevel, livingEntity, i) -> itemStack.getOrCreateTag().getFloat(Constants.DEPLOYED))); // Set deployed

        ParticleFactoryRegistry.getInstance().register(CAMP_LIMIT, CampfireLimitParticle.DefaultFactory::new);
    }
}