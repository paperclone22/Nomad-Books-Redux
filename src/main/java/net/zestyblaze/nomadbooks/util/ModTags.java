package net.zestyblaze.nomadbooks.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags {

	private ModTags() {}
	public static class Blocks {

		private Blocks() {}
		public static final TagKey<Block> IS_AIR_REPLACEABLE = createTag("replaceable_to_air");
		public static final TagKey<Block> IS_WATER_REPLACEABLE = createTag("replaceable_to_water");
		public static final TagKey<Block> IS_NOT_DISPLACABLE = createTag("terrain_not_displaceable");

		private static TagKey<Block> createTag(String name) {
			return TagKey.create(BuiltInRegistries.BLOCK.key(), new ResourceLocation(Constants.MODID, name));
		}
	}

	public static class Items {

		private Items() {}

		private static TagKey<Item> createTag(String name) {
			return TagKey.create(BuiltInRegistries.ITEM.key(), new ResourceLocation(Constants.MODID, name));
		}
	}

}