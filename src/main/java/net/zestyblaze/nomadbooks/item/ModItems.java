package net.zestyblaze.nomadbooks.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.zestyblaze.nomadbooks.NomadBooks;
import net.zestyblaze.nomadbooks.util.Constants;

/**
 * Register Mod Items and Creative Tabs
 */
public class ModItems {

	private ModItems() {}

	// Items self Registry (These static final fields don't need to be called in onInitialize)
	public static final Item GRASS_PAGE = registerItem( "grass_page", new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
	public static final Item NOMAD_BOOK = registerItem( "nomad_book", new NomadBookItem(new FabricItemSettings().maxCount(1).rarity(Rarity.RARE)));
	public static final Item NETHER_NOMAD_BOOK = registerItem( "nether_nomad_book", new NetherNomadBookItem(new FabricItemSettings().maxCount(1).rarity(Rarity.RARE).fireproof()));
	public static final Item AQUATIC_MEMBRANE_PAGE = registerItem( "aquatic_membrane_page", new BookUpgradeItem(new FabricItemSettings().maxCount(1).rarity(Rarity.UNCOMMON), Constants.AQUATIC_MEMBRANE));
	public static final Item MYCELIUM_PAGE = registerItem( "mycelium_page", new BookUpgradeItem(new FabricItemSettings().maxCount(1).rarity(Rarity.UNCOMMON), Constants.FUNGI_SUPPORT));
	public static final Item SPACIAL_DISPLACER_PAGE = registerItem( "spacial_displacer_page", new BookUpgradeItem(new FabricItemSettings().maxCount(1).rarity(Rarity.UNCOMMON), Constants.SPACIAL_DISPLACER));
	public static final Item CREATIVE_NOMAD_BOOK = registerItem( "creative_nomad_book", new NetherNomadBookItem(new FabricItemSettings().maxCount(1).rarity(Rarity.RARE).fireproof()) {
		@Override
		public ItemStack getDefaultInstance() {
			super.getDefaultInstance();
			ItemStack itemStack = new ItemStack(this);
			CompoundTag tags = itemStack.getOrCreateTagElement(Constants.MODID);
			tags.putInt(Constants.HEIGHT, 15);
			tags.putInt(Constants.WIDTH, 15);
			tags.putString(Constants.STRUCTURE, DEFAULT_STRUCTURE_PATH);
			// upgrades
			ListTag upgradeList = new ListTag();
			upgradeList.add(StringTag.valueOf(Constants.AQUATIC_MEMBRANE));
			upgradeList.add(StringTag.valueOf(Constants.FUNGI_SUPPORT));
			upgradeList.add(StringTag.valueOf(Constants.SPACIAL_DISPLACER));
			tags.put(Constants.UPGRADES, upgradeList);
			return itemStack;
		}
	});

	// Mod Block Items
	public static Block registerBlock(String name, Block block) {
		registerBlockItem(name, block);
		return Blocks.register(new ResourceLocation(Constants.MODID, name).toString(), block);
	}

	private static void registerBlockItem(String name, Block block) {
		Items.registerItem( new ResourceLocation(Constants.MODID, name), new BlockItem(block, new FabricItemSettings()));
	}

	// Creative Tab
	public static	final CreativeModeTab NOMAD_BOOKS_TAB_BUILD = FabricItemGroup.builder() // yeah I know, this should have its own class to be consistent
			.icon(() -> new ItemStack(ModItems.NOMAD_BOOK))
			.title(Component.translatable("item.nomadbooks.nomad_book"))
			.displayItems((context, entries) -> {
				entries.accept(ModItems.GRASS_PAGE);
				entries.accept(ModItems.NOMAD_BOOK.getDefaultInstance());
				entries.accept(ModItems.NETHER_NOMAD_BOOK.getDefaultInstance());
				entries.accept(ModItems.AQUATIC_MEMBRANE_PAGE);
				entries.accept(ModItems.MYCELIUM_PAGE);
				entries.accept(ModItems.SPACIAL_DISPLACER_PAGE);
				entries.accept(ModItems.CREATIVE_NOMAD_BOOK.getDefaultInstance());
				//  See: https://youtu.be/5VEh1dDngd8?si=5B9WQH_VFxQtILjq
				entries.accept(NomadBooks.MEMBRANE);
				entries.accept(NomadBooks.NOMAD_MUSHROOM_BLOCK);
				entries.accept(NomadBooks.NOMAD_MUSHROOM_STEM);
			})
			.build();

	/**
	 * A Helper method to shorten the calls for Registering Items
	 * @param name java.lang.String
	 * @param item net.minecraft.world.item.Item
	 * @return net.minecraft.world.item.Item
	 */
	public static Item registerItem(String name, Item item) {
		return Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(Constants.MODID, name), item);
	}

	/**
	 * Register The Creative Tabs
	 * Remember to call this in onInitialize()
	 */
	public static void registerCreativeTabs() {
		NomadBooks.LOGGER.info(Constants.MODID + ": Registering Items");

		// Creative Tab Registry
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Constants.MODID, NOMAD_BOOKS_TAB_BUILD);
	}
}
