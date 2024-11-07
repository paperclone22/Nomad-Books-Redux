package net.zestyblaze.nomadbooks.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModTags {

	private ModTags() {}
	public static class Blocks {

		private Blocks() {}
		public static final TagKey<Block> IS_AIR_REPLACEABLE = createTag("replaceable_to_air");
		public static final TagKey<Block> IS_WATER_REPLACEABLE = createTag("replaceable_to_water");
		public static final TagKey<Block> IS_NOT_DISPLACABLE = createTag("terrain_not_displaceable");

		private static TagKey<Block> createTag(String name) {
			return TagKey.of(Registries.BLOCK.getKey(), new Identifier(Constants.MODID, name));
		}
	}

	public static class Items {

		private Items() {}

		@SuppressWarnings("Unused")
		private static TagKey<Item> createTag(String name) {
			return TagKey.of(Registries.ITEM.getKey(), new Identifier(Constants.MODID, name));
		}
	}

}