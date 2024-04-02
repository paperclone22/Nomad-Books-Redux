package net.zestyblaze.nomadbooks.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.zestyblaze.nomadbooks.util.ModTags;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagProvider.BlockTagProvider {
	public ModBlockTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	protected void addTags(HolderLookup.Provider arg) {
		// TODO consider adding flowers to the replace list
		// TODO consider a config list that can add to this tag. cloth-config ? forge-config-api ?
		getOrCreateTagBuilder(ModTags.Blocks.IS_AIR_REPLACEABLE)
				.add(Blocks.AIR, Blocks.CAVE_AIR, Blocks.VOID_AIR, Blocks.CRIMSON_ROOTS, Blocks.DEAD_BUSH, Blocks.FERN, Blocks.FIRE, Blocks.GLOW_LICHEN, Blocks.GRASS, Blocks.HANGING_ROOTS, Blocks.LARGE_FERN, Blocks.LAVA, Blocks.LIGHT, Blocks.NETHER_SPROUTS, Blocks.SNOW, Blocks.SOUL_FIRE, Blocks.STRUCTURE_VOID, Blocks.TALL_GRASS, Blocks.VINE, Blocks.WARPED_ROOTS)
				.forceAddTag(BlockTags.REPLACEABLE)
		;
		getOrCreateTagBuilder(ModTags.Blocks.IS_WATER_REPLACEABLE)
				.add(Blocks.WATER, Blocks.BUBBLE_COLUMN, Blocks.SEAGRASS, Blocks.TALL_SEAGRASS, Blocks.LAVA)
		;
	}
}
