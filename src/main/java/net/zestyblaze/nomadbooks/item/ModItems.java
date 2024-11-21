package net.zestyblaze.nomadbooks.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.zestyblaze.nomadbooks.NomadBooks;
import net.zestyblaze.nomadbooks.util.Constants;
import net.zestyblaze.nomadbooks.util.NomadBooksComponent;
import net.zestyblaze.nomadbooks.util.NomadInkComponent;

import java.util.Arrays;
import java.util.List;

import static net.zestyblaze.nomadbooks.util.NomadBooksYACLConfig.defaultStandardBookHeight;
import static net.zestyblaze.nomadbooks.util.NomadBooksYACLConfig.defaultStandardBookWidth;

/**
 * Register Mod Items and Creative Tabs
 */
public class ModItems {

	private ModItems() {
	}

	// Items self Registry (These static final fields don't need to be called in onInitialize)
	public static final Item GRASS_PAGE = registerItem("grass_page", new Item(new Item.Settings().rarity(Rarity.UNCOMMON)));
	public static final Item NOMAD_BOOK = registerItem("nomad_book", new NomadBookItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE)
			.component(NomadBooks.NOMAD_BOOK_DATA, new NomadBooksComponent(false, false, defaultStandardBookHeight, defaultStandardBookWidth, NomadBookItem.DEFAULT_STRUCTURE_PATH, List.of()))
			.component(NomadBooks.NOMAD_INK_DATA, new NomadInkComponent(false, 0, 0, List.of()))
	));
	public static final Item NETHER_NOMAD_BOOK = registerItem("nether_nomad_book", new NomadBookItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE).fireproof()
			.component(NomadBooks.NOMAD_BOOK_DATA, new NomadBooksComponent(false, false, 3, 7, NomadBookItem.NETHER_DEFAULT_STRUCTURE_PATH, List.of()))
			.component(NomadBooks.NOMAD_INK_DATA, new NomadInkComponent(false, 0, 0, List.of()))
	));
	public static final Item AQUATIC_MEMBRANE_PAGE = registerItem("aquatic_membrane_page", new BookUpgradeItem(new Item.Settings().rarity(Rarity.UNCOMMON), Constants.AQUATIC_MEMBRANE));
	public static final Item MYCELIUM_PAGE = registerItem("mycelium_page", new BookUpgradeItem(new Item.Settings().rarity(Rarity.UNCOMMON), Constants.FUNGI_SUPPORT));
	public static final Item SPACIAL_DISPLACER_PAGE = registerItem("spacial_displacer_page", new BookUpgradeItem(new Item.Settings().rarity(Rarity.UNCOMMON), Constants.SPACIAL_DISPLACER));
	public static final Item CREATIVE_NOMAD_BOOK = registerItem("creative_nomad_book", new NomadBookItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE).fireproof()
			.component(NomadBooks.NOMAD_BOOK_DATA, new NomadBooksComponent(false, false, 15, 15, NomadBookItem.CREATIVE_DEFAULT_STRUCTURE_PATH, Arrays.asList(Constants.AQUATIC_MEMBRANE, Constants.FUNGI_SUPPORT, Constants.SPACIAL_DISPLACER)))
			.component(NomadBooks.NOMAD_INK_DATA, new NomadInkComponent(false, 0, 0, List.of()))
	));

	// Mod Block Items
	public static Block registerBlock(String name, Block block) {
		registerBlockItem(name, block);
		return Blocks.register(Identifier.of(Constants.MODID, name).toString(), block);
	}

	private static void registerBlockItem(String name, Block block) {
		Items.register(Identifier.of(Constants.MODID, name), new BlockItem(block, new Item.Settings()));
	}

	// Creative Tab
	public static final ItemGroup NOMAD_BOOKS_TAB_BUILD = FabricItemGroup.builder() // yeah I know, this should have its own class to be consistent
			.icon(() -> new ItemStack(ModItems.NOMAD_BOOK))
			.displayName(Text.translatable("item.nomadbooks.nomad_book"))
			.entries((context, entries) -> {
				entries.add(ModItems.GRASS_PAGE);
				entries.add(ModItems.NOMAD_BOOK.getDefaultStack());
				entries.add(ModItems.NETHER_NOMAD_BOOK.getDefaultStack());
				entries.add(ModItems.AQUATIC_MEMBRANE_PAGE);
				entries.add(ModItems.MYCELIUM_PAGE);
				entries.add(ModItems.SPACIAL_DISPLACER_PAGE);
				entries.add(ModItems.CREATIVE_NOMAD_BOOK.getDefaultStack());
				//  See: https://youtu.be/5VEh1dDngd8?si=5B9WQH_VFxQtILjq
				entries.add(NomadBooks.MEMBRANE);
				entries.add(NomadBooks.NOMAD_MUSHROOM_BLOCK);
				entries.add(NomadBooks.NOMAD_MUSHROOM_STEM);
			})
			.build();

	/**
	 * A Helper method to shorten the calls for Registering Items
	 *
	 * @param name java.lang.String
	 * @param item net.minecraft.world.item.Item
	 * @return net.minecraft.world.item.Item
	 */
	public static Item registerItem(String name, Item item) {
		return Registry.register(Registries.ITEM, Identifier.of(Constants.MODID, name), item);
	}

	/**
	 * Register The Creative Tabs
	 * Remember to call this in onInitialize()
	 */
	public static void registerCreativeTabs() {
		NomadBooks.LOGGER.info(Constants.MODID + ": Registering Items");

		// Creative Tab Registry
		Registry.register(Registries.ITEM_GROUP, Constants.MODID, NOMAD_BOOKS_TAB_BUILD);
	}
}
