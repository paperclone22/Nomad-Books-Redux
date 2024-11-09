package net.zestyblaze.nomadbooks;

import joptsimple.internal.Classes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetNbtLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.zestyblaze.nomadbooks.block.MembraneBlock;
import net.zestyblaze.nomadbooks.block.NomadMushroomBlock;
import net.zestyblaze.nomadbooks.block.NomadMushroomStemBlock;
import net.zestyblaze.nomadbooks.item.ModItems;
import net.zestyblaze.nomadbooks.item.NomadBookItem;
import net.zestyblaze.nomadbooks.recipe.DyeNomadBookRecipe;
import net.zestyblaze.nomadbooks.recipe.NetherNomadBookCraftRecipe;
import net.zestyblaze.nomadbooks.recipe.NomadBookCraftRecipe;
import net.zestyblaze.nomadbooks.recipe.NomadBookDismantleRecipe;
import net.zestyblaze.nomadbooks.recipe.NomadBookHeightUpgradeRecipe;
import net.zestyblaze.nomadbooks.recipe.NomadBookInkRecipe;
import net.zestyblaze.nomadbooks.recipe.NomadBookUpgradeRecipe;
import net.zestyblaze.nomadbooks.util.Constants;
import net.zestyblaze.nomadbooks.util.NomadBooksYACLConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

import static net.zestyblaze.nomadbooks.util.Constants.MINECRAFT;

@SuppressWarnings("deprecation")
public class NomadBooks implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger(Constants.MODID);

	// Loot Table Names
	private static final Identifier DUNGEON_CHEST_LOOT_TABLE_ID = new Identifier(MINECRAFT, "chests/simple_dungeon");
	private static final Identifier MINESHAFT_CHEST_LOOT_TABLE_ID = new Identifier(MINECRAFT, "chests/abandoned_mineshaft");
	private static final Identifier TEMPLE_CHEST_LOOT_TABLE_ID = new Identifier(MINECRAFT, "chests/jungle_temple");
	private static final Identifier TREASURE_CHEST_LOOT_TABLE_ID = new Identifier(MINECRAFT, "chests/buried_treasure");
	private static final Identifier OUTPOST_CHEST_LOOT_TABLE_ID = new Identifier(MINECRAFT, "chests/pillager_outpost");
	private static final Identifier STRONGHOLD_LIBRARY_CHEST_LOOT_TABLE_ID = new Identifier(MINECRAFT, "chests/stronghold_library");
	private static final Identifier CARTOGRAPHER_CHEST_LOOT_TABLE_ID = new Identifier(MINECRAFT, "chests/village/village_cartographer");
	private static final Identifier BONUS_CHEST_LOOT_TABLE_ID = new Identifier(MINECRAFT, "chests/spawn_bonus_chest");


	// Register Mod Blocks
	public static final Block MEMBRANE = ModItems.registerBlock( "membrane", new MembraneBlock(FabricBlockSettings.copyOf(Blocks.GLASS).strength(0.6f, 0f).nonOpaque().sounds(BlockSoundGroup.HONEY).luminance(6).noCollision().solid()));
	public static final Block NOMAD_MUSHROOM_BLOCK = ModItems.registerBlock( "nomad_mushroom_block", new NomadMushroomBlock(FabricBlockSettings.copyOf(Blocks.OAK_LOG).mapColor(MapColor.PURPLE).strength(0.6F, 0).sounds(BlockSoundGroup.WOOD).notSolid()));
	public static final Block NOMAD_MUSHROOM_STEM = ModItems.registerBlock( "nomad_mushroom_stem", new NomadMushroomStemBlock(FabricBlockSettings.copyOf(Blocks.OAK_LOG).mapColor(MapColor.TERRACOTTA_WHITE).strength(0.6F, 0).sounds(BlockSoundGroup.WOOD).notSolid()));

	// Register Mod Recipes

// TODO does this work?
//	public <T extends Recipe<?>> RecipeType<T> recipeTypeHelper(String name, Class<T> ob) { // NOSONAR
//		return Registry.register(Registries.RECIPE_SERIALIZER,
//		new Identifier(Constants.MODID, name).toString(),
//		new SpecialRecipeSerializer<>(T::new)); // NOSONAR
//	} // NOSONAR
//	RecipeSerializer<NomadBookCraftRecipe> ass = recipeTypeHelper("ass", NomadBookCraftRecipe.class); // NOSONAR

	public static final RecipeSerializer<NomadBookCraftRecipe> CRAFT_NOMAD_BOOK = Registry.register(Registries.RECIPE_SERIALIZER, new Identifier(Constants.MODID, "crafting_special_nomadbookcraft").toString(), new SpecialRecipeSerializer<>(NomadBookCraftRecipe::new));
	public static final RecipeSerializer<NomadBookHeightUpgradeRecipe> UPGRADE_HEIGHT_NOMAD_BOOK = RecipeSerializer.register(new Identifier(Constants.MODID, "crafting_special_nomadbookupgradeheight").toString(), new SpecialRecipeSerializer<>(NomadBookHeightUpgradeRecipe::new));
	public static final RecipeSerializer<NomadBookDismantleRecipe> DISMANTLE_NOMAD_BOOK = RecipeSerializer.register(new Identifier(Constants.MODID, "crafting_special_nomadbookdismantle").toString(), new SpecialRecipeSerializer<>(NomadBookDismantleRecipe::new)); // Special
	public static final RecipeSerializer<NomadBookUpgradeRecipe> UPGRADE_NOMAD_BOOK = RecipeSerializer.register(new Identifier(Constants.MODID, "crafting_special_nomadbookupgrade").toString(), new SpecialRecipeSerializer<>(NomadBookUpgradeRecipe::new));
	public static final RecipeSerializer<NomadBookInkRecipe> INK_NOMAD_BOOK = RecipeSerializer.register(new Identifier(Constants.MODID, "crafting_special_nomadbookink").toString(), new SpecialRecipeSerializer<>(NomadBookInkRecipe::new));
	public static final RecipeSerializer<NetherNomadBookCraftRecipe> CRAFT_NETHER_NOMAD_BOOK = RecipeSerializer.register(new Identifier(Constants.MODID, "crafting_special_nethernomadbookcraft").toString(), new SpecialRecipeSerializer<>(NetherNomadBookCraftRecipe::new));
	public static final RecipeSerializer<DyeNomadBookRecipe> DYE_NOMAD_BOOK = RecipeSerializer.register(new Identifier(Constants.MODID, "crafting_special_dyenomadbook").toString(), new SpecialRecipeSerializer<>(DyeNomadBookRecipe::new));
	@Override
	public void onInitialize() {
		LOGGER.info(Constants.MODID + " Loaded");

		// Register Mod Items and Creative Tab
		ModItems.registerCreativeTabs();

		// Init Config\
		// TODO re-add this
//		try {
//			NomadBooksYACLConfig.CONFIG.load();
//		} catch (Exception ex) {
//			LOGGER.error("Error loading Nomad Books config, restoring default", ex);
//		}
//		NomadBooksYACLConfig.CONFIG.save();

		// Add Loot Tables
		UniformLootNumberProvider lootTableRange = UniformLootNumberProvider.create(0, 1);
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
				BONUS_CHEST_LOOT_TABLE_ID, LootPool.builder().rolls(ConstantLootNumberProvider.create(1)).with(ItemEntry.builder(ModItems.NOMAD_BOOK))
						.apply(SetNbtLootFunction.builder(Util.make(new NbtCompound(), compoundTag -> compoundTag.put(Constants.MODID, Util.make(new NbtCompound(), child -> {
							child.putInt(Constants.HEIGHT, 1);
							child.putInt(Constants.WIDTH, 3);
							child.putString(Constants.STRUCTURE, NomadBookItem.DEFAULT_STRUCTURE_PATH);
						})))).build())
		);

		// Using our map, edit the loot pools
		lootTableConfigurations.forEach((id, poolBuilder) -> LootTableEvents.MODIFY.register((resourceManager, lootManager, tableId, supplier, setter) -> {
			if (id.equals(tableId)) {
				supplier.pool(poolBuilder);
			}
		}));

	}

}
