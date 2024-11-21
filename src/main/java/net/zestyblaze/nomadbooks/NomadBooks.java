package net.zestyblaze.nomadbooks;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetComponentsLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.zestyblaze.nomadbooks.block.MembraneBlock;
import net.zestyblaze.nomadbooks.block.NomadMushroomBlock;
import net.zestyblaze.nomadbooks.block.NomadMushroomStemBlock;
import net.zestyblaze.nomadbooks.item.ModItems;
import net.zestyblaze.nomadbooks.item.NomadBookItem;
import net.zestyblaze.nomadbooks.recipe.NomadBookDismantleRecipe;
import net.zestyblaze.nomadbooks.recipe.NomadBookHeightUpgradeRecipe;
import net.zestyblaze.nomadbooks.recipe.NomadBookUpgradeRecipe;
import net.zestyblaze.nomadbooks.util.Constants;
import net.zestyblaze.nomadbooks.util.NomadBooksComponent;
import net.zestyblaze.nomadbooks.util.NomadBooksYACLConfig;
import net.zestyblaze.nomadbooks.util.NomadInkComponent;
import net.zestyblaze.nomadbooks.util.PlayerFirstJoinCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static net.zestyblaze.nomadbooks.util.Constants.MINECRAFT;
import static net.zestyblaze.nomadbooks.util.NomadBooksYACLConfig.defaultStandardBookHeight;
import static net.zestyblaze.nomadbooks.util.NomadBooksYACLConfig.defaultStandardBookWidth;
import static net.zestyblaze.nomadbooks.util.NomadBooksYACLConfig.doStartWithBook;

@SuppressWarnings("deprecation")
public class NomadBooks implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger(Constants.MODID);

	// Loot Table Names
	private static final Identifier DUNGEON_CHEST_LOOT_TABLE_ID = Identifier.of(MINECRAFT, "chests/simple_dungeon");
	private static final Identifier MINESHAFT_CHEST_LOOT_TABLE_ID = Identifier.of(MINECRAFT, "chests/abandoned_mineshaft");
	private static final Identifier TEMPLE_CHEST_LOOT_TABLE_ID = Identifier.of(MINECRAFT, "chests/jungle_temple");
	private static final Identifier TREASURE_CHEST_LOOT_TABLE_ID = Identifier.of(MINECRAFT, "chests/buried_treasure");
	private static final Identifier OUTPOST_CHEST_LOOT_TABLE_ID = Identifier.of(MINECRAFT, "chests/pillager_outpost");
	private static final Identifier STRONGHOLD_LIBRARY_CHEST_LOOT_TABLE_ID = Identifier.of(MINECRAFT, "chests/stronghold_library");
	private static final Identifier CARTOGRAPHER_CHEST_LOOT_TABLE_ID = Identifier.of(MINECRAFT, "chests/village/village_cartographer");
	private static final Identifier BONUS_CHEST_LOOT_TABLE_ID = Identifier.of(MINECRAFT, "chests/spawn_bonus_chest");


	// Register Mod Blocks
	public static final Block MEMBRANE = ModItems.registerBlock( "membrane", new MembraneBlock(FabricBlockSettings.copyOf(Blocks.GLASS).strength(0.6f, 0f).nonOpaque().sounds(BlockSoundGroup.HONEY).luminance(6).noCollision().solid()));
	public static final Block NOMAD_MUSHROOM_BLOCK = ModItems.registerBlock( "nomad_mushroom_block", new NomadMushroomBlock(FabricBlockSettings.copyOf(Blocks.OAK_LOG).mapColor(MapColor.PURPLE).strength(0.6F, 0).sounds(BlockSoundGroup.WOOD).notSolid()));
	public static final Block NOMAD_MUSHROOM_STEM = ModItems.registerBlock( "nomad_mushroom_stem", new NomadMushroomStemBlock(FabricBlockSettings.copyOf(Blocks.OAK_LOG).mapColor(MapColor.TERRACOTTA_WHITE).strength(0.6F, 0).sounds(BlockSoundGroup.WOOD).notSolid()));

	// Register Mod Recipes
	public static final RecipeSerializer<NomadBookHeightUpgradeRecipe> UPGRADE_HEIGHT_NOMAD_BOOK = RecipeSerializer.register(Identifier.of(Constants.MODID, "crafting_special_nomadbookupgradeheight").toString(), new SpecialRecipeSerializer<>(NomadBookHeightUpgradeRecipe::new));
	public static final RecipeSerializer<NomadBookDismantleRecipe> DISMANTLE_NOMAD_BOOK = RecipeSerializer.register(Identifier.of(Constants.MODID, "crafting_special_nomadbookdismantle").toString(), new SpecialRecipeSerializer<>(NomadBookDismantleRecipe::new)); // Special
	public static final RecipeSerializer<NomadBookUpgradeRecipe> UPGRADE_NOMAD_BOOK = RecipeSerializer.register(Identifier.of(Constants.MODID, "crafting_special_nomadbookupgrade").toString(), new SpecialRecipeSerializer<>(NomadBookUpgradeRecipe::new));

	// Register Components
		private static <T> ComponentType<T> register(Identifier id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
			return Registry.register(Registries.DATA_COMPONENT_TYPE, id, builderOperator.apply(ComponentType.builder()).build());
		}

	public static final ComponentType<NomadBooksComponent> NOMAD_BOOK_DATA = register(
			Identifier.of(Constants.MODID, "nomad_book_data"), builder -> builder.codec(NomadBooksComponent.CODEC).packetCodec(NomadBooksComponent.PACKET_CODEC).cache()
	);
	public static final ComponentType<NomadInkComponent> NOMAD_INK_DATA = register(
			Identifier.of(Constants.MODID, "nomad_ink_data"), builder -> builder.codec(NomadInkComponent.CODEC).packetCodec(NomadInkComponent.PACKET_CODEC).cache()
	);

	@Override
	public void onInitialize() {
		LOGGER.info(Constants.MODID + " Loaded");

		// Register Mod Items and Creative Tab
		ModItems.registerCreativeTabs();

		// Init Config\
		try {
			NomadBooksYACLConfig.CONFIG.load();
		} catch (Exception ex) {
			LOGGER.error("Error loading Nomad Books config, restoring default", ex);
		}
		NomadBooksYACLConfig.CONFIG.save();

		// Add Loot Tables
		UniformLootNumberProvider lootTableRange = UniformLootNumberProvider.create(0.4f, 1);
		UniformLootNumberProvider strongholdRange = UniformLootNumberProvider.create(1, 5);

		// Map the pool names with our loot additions
		Map<Identifier, LootPool.Builder> lootTableConfigurations = Map.of(
				CARTOGRAPHER_CHEST_LOOT_TABLE_ID, LootPool.builder().rolls(lootTableRange).with(ItemEntry.builder(ModItems.GRASS_PAGE)),
				DUNGEON_CHEST_LOOT_TABLE_ID, LootPool.builder().rolls(lootTableRange).with(ItemEntry.builder(ModItems.GRASS_PAGE)),
				MINESHAFT_CHEST_LOOT_TABLE_ID, LootPool.builder().rolls(lootTableRange).with(ItemEntry.builder(ModItems.GRASS_PAGE)),
				OUTPOST_CHEST_LOOT_TABLE_ID, LootPool.builder().rolls(lootTableRange).with(ItemEntry.builder(ModItems.GRASS_PAGE)),
				STRONGHOLD_LIBRARY_CHEST_LOOT_TABLE_ID, LootPool.builder().rolls(strongholdRange).with(ItemEntry.builder(ModItems.GRASS_PAGE)),
				TEMPLE_CHEST_LOOT_TABLE_ID, LootPool.builder().rolls(lootTableRange).with(ItemEntry.builder(ModItems.GRASS_PAGE)),
				TREASURE_CHEST_LOOT_TABLE_ID, LootPool.builder().rolls(lootTableRange).with(ItemEntry.builder(ModItems.GRASS_PAGE)),
				BONUS_CHEST_LOOT_TABLE_ID, LootPool.builder().rolls(ConstantLootNumberProvider.create(1)).with(ItemEntry.builder(ModItems.NOMAD_BOOK)
						.apply(SetComponentsLootFunction.builder(NOMAD_BOOK_DATA, new NomadBooksComponent(false, false, defaultStandardBookHeight, defaultStandardBookWidth, NomadBookItem.DEFAULT_STRUCTURE_PATH, List.of()))))
						.apply(SetComponentsLootFunction.builder(NOMAD_INK_DATA, new NomadInkComponent(false, 0, 0, List.of())))
		);

		// Using our map, edit the loot pools
		LootTableEvents.MODIFY.register((key, tableBuilder, source) -> {
					String stringId = key.getValue().toString();
					if (!stringId.startsWith("minecraft:chests")) {
						return;
					}
					if (lootTableConfigurations.containsKey(key.getValue())) {
						tableBuilder.pool(lootTableConfigurations.get(key.getValue()));
					}
				}
		);

		// Add a book to every player's inventory on first join
		if (doStartWithBook) {
			PlayerFirstJoinCallback.EVENT.register(this::playerJoined);
		}
	}

	private void playerJoined(ServerPlayerEntity player, MinecraftServer minecraftServer) {
		ItemStack book = new ItemStack(ModItems.NOMAD_BOOK);
		book.set(NOMAD_BOOK_DATA, new NomadBooksComponent(false, false, defaultStandardBookHeight, defaultStandardBookWidth, NomadBookItem.DEFAULT_STRUCTURE_PATH, List.of()));
		book.set(NOMAD_INK_DATA, new NomadInkComponent(false, 0, 0, List.of()));
		player.getInventory().insertStack(book);
	}

}
