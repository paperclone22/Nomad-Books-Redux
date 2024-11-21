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
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.zestyblaze.nomadbooks.NomadBooks;
import net.zestyblaze.nomadbooks.client.particle.CampfireLimitParticle;
import net.zestyblaze.nomadbooks.item.ModItems;
import net.zestyblaze.nomadbooks.util.Constants;

import java.util.Optional;

import static net.minecraft.component.type.DyedColorComponent.getColor;


@SuppressWarnings("deprecation")
@Environment(EnvType.CLIENT)
public class NomadBooksClient implements ClientModInitializer {
    public static final SimpleParticleType CAMP_LIMIT = Registry.register(Registries.PARTICLE_TYPE, "nomadbooks:camp_limit", FabricParticleTypes.simple(true));

    public static final int DEFAULT_COLOR = -6265536;

    @Override
    public void onInitializeClient() {
        // allow Membrane to render transparent
        BlockRenderLayerMap.INSTANCE.putBlock(NomadBooks.MEMBRANE, RenderLayer.getTranslucent());

        // determines weather books should display the retrieved or deployed model
        FabricModelPredicateProviderRegistry.register(Identifier.of(Constants.DEPLOYED), ((itemStack, clientLevel, livingEntity, i) -> {
            float v;
            if (Optional.ofNullable(itemStack.get(NomadBooks.NOMAD_BOOK_DATA)).isPresent()) {
                v = Optional.ofNullable(itemStack.get(NomadBooks.NOMAD_BOOK_DATA)).get().isDeployed() ? 1.0f : 0.0f;
            } else {
                v = 0.0f;
            }
            return v;
        }
        )); // get deployed

        // Register particle
        ParticleFactoryRegistry.getInstance().register(CAMP_LIMIT, CampfireLimitParticle.DefaultFactory::new);

        // Render the Color for Nomad Book items
        ColorProviderRegistry.ITEM.register(
            (stack, layer) -> layer > 0 ? -1 : getColor(stack, DEFAULT_COLOR),
            ModItems.NOMAD_BOOK, ModItems.NETHER_NOMAD_BOOK, ModItems.CREATIVE_NOMAD_BOOK);
    }
}

