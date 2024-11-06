package net.zestyblaze.nomadbooks.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.zestyblaze.nomadbooks.NomadBooks;
import net.zestyblaze.nomadbooks.client.particle.CampfireLimitParticle;
import net.zestyblaze.nomadbooks.item.ModItems;
import net.zestyblaze.nomadbooks.item.NomadBookItem;
import net.zestyblaze.nomadbooks.util.Constants;


@SuppressWarnings("deprecation")
@Environment(EnvType.CLIENT)
public class NomadBooksClient implements ClientModInitializer {
    public static final DefaultParticleType CAMP_LIMIT = Registry.register(Registries.PARTICLE_TYPE, "nomadbooks:camp_limit", FabricParticleTypes.simple(true));

    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(NomadBooks.MEMBRANE, RenderLayer.getTranslucent());

        FabricModelPredicateProviderRegistry.register(new Identifier(Constants.DEPLOYED), ((itemStack, clientLevel, livingEntity, i) -> itemStack.getOrCreateNbt().getFloat(Constants.DEPLOYED))); // Set deployed

        ParticleFactoryRegistry.getInstance().register(CAMP_LIMIT, CampfireLimitParticle.DefaultFactory::new);

        // Render the Color for Nomad Book items
        ColorProviderRegistry.ITEM.register(
            (stack, layer) -> layer != 0 ? -1 : ((NomadBookItem) stack.getItem()).getColor(stack),
            ModItems.NOMAD_BOOK, ModItems.NETHER_NOMAD_BOOK, ModItems.CREATIVE_NOMAD_BOOK);
    }
}

