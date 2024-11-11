package net.zestyblaze.nomadbooks.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.zestyblaze.nomadbooks.util.ModTags;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagProvider.BlockTagProvider {
	public ModBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	protected void configure(RegistryWrapper.WrapperLookup arg) { // NOTE:{This is datagen, the real info is in *.json}. These tags exist because I want it to be difficult for players to remove them. eg. Vanilla functionality works best with them in these lists. For those reading the configs, pretend these tags don't exist
		getOrCreateTagBuilder(ModTags.Blocks.IS_AIR_REPLACEABLE)
				.add(Blocks.AIR, Blocks.CAVE_AIR, Blocks.VOID_AIR, Blocks.CRIMSON_ROOTS, Blocks.DEAD_BUSH, Blocks.FERN, Blocks.FIRE, Blocks.GLOW_LICHEN, Blocks.SHORT_GRASS, Blocks.HANGING_ROOTS, Blocks.LARGE_FERN, Blocks.LAVA, Blocks.LIGHT, Blocks.NETHER_SPROUTS, Blocks.SNOW, Blocks.SOUL_FIRE, Blocks.STRUCTURE_VOID, Blocks.TALL_GRASS, Blocks.VINE, Blocks.WARPED_ROOTS)
				.forceAddTag(BlockTags.REPLACEABLE)
		;

		getOrCreateTagBuilder(ModTags.Blocks.IS_WATER_REPLACEABLE)
				.add(Blocks.WATER, Blocks.BUBBLE_COLUMN, Blocks.SEAGRASS, Blocks.TALL_SEAGRASS, Blocks.LAVA)
		;

		getOrCreateTagBuilder(ModTags.Blocks.IS_NOT_DISPLACEABLE)
				.add(Blocks.BEDROCK, Blocks.END_PORTAL, Blocks.END_PORTAL_FRAME, Blocks.COMMAND_BLOCK, Blocks.BARRIER, Blocks.LIGHT, Blocks.END_GATEWAY, Blocks.REPEATING_COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK, Blocks.JIGSAW)
		;

	}

}
