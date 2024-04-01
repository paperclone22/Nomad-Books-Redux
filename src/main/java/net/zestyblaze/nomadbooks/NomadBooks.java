package net.zestyblaze.nomadbooks;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.zestyblaze.nomadbooks.block.MembraneBlock;
import net.zestyblaze.nomadbooks.block.NomadMushroomBlock;
import net.zestyblaze.nomadbooks.block.NomadMushroomStemBlock;
import net.zestyblaze.nomadbooks.item.ModItems;
import net.zestyblaze.nomadbooks.item.NomadBookItem;
import net.zestyblaze.nomadbooks.recipe.NetherNomadBookCraftRecipe;
import net.zestyblaze.nomadbooks.recipe.NomadBookCraftRecipe;
import net.zestyblaze.nomadbooks.recipe.NomadBookDismantleRecipe;
import net.zestyblaze.nomadbooks.recipe.NomadBookHeightUpgradeRecipe;
import net.zestyblaze.nomadbooks.recipe.NomadBookInkRecipe;
import net.zestyblaze.nomadbooks.recipe.NomadBookUpgradeRecipe;
import net.zestyblaze.nomadbooks.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static net.zestyblaze.nomadbooks.util.Constants.MINECRAFT;

@SuppressWarnings("deprecation")
public class NomadBooks implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger(Constants.MODID);

	// Loot Table Names
	private static final ResourceLocation DUNGEON_CHEST_LOOT_TABLE_ID = new ResourceLocation(MINECRAFT, "chests/simple_dungeon");
	private static final ResourceLocation MINESHAFT_CHEST_LOOT_TABLE_ID = new ResourceLocation(MINECRAFT, "chests/abandoned_mineshaft");
	private static final ResourceLocation TEMPLE_CHEST_LOOT_TABLE_ID = new ResourceLocation(MINECRAFT, "chests/jungle_temple");
	private static final ResourceLocation TREASURE_CHEST_LOOT_TABLE_ID = new ResourceLocation(MINECRAFT, "chests/buried_treasure");
	private static final ResourceLocation OUTPOST_CHEST_LOOT_TABLE_ID = new ResourceLocation(MINECRAFT, "chests/pillager_outpost");
	private static final ResourceLocation STRONGHOLD_LIBRARY_CHEST_LOOT_TABLE_ID = new ResourceLocation(MINECRAFT, "chests/stronghold_library");
	private static final ResourceLocation CARTOGRAPHER_CHEST_LOOT_TABLE_ID = new ResourceLocation(MINECRAFT, "chests/village/village_cartographer");
	private static final ResourceLocation BONUS_CHEST_LOOT_TABLE_ID = new ResourceLocation(MINECRAFT, "chests/spawn_bonus_chest");


	// Register Mod Blocks
	public static final Block MEMBRANE = ModItems.registerBlock( "membrane", new MembraneBlock(FabricBlockSettings.copyOf(Blocks.GLASS).strength(0.6f, 0f).nonOpaque().sounds(SoundType.HONEY_BLOCK).luminance(6).noCollision().solid()));
	public static final Block NOMAD_MUSHROOM_BLOCK = ModItems.registerBlock( "nomad_mushroom_block", new NomadMushroomBlock(FabricBlockSettings.copyOf(Blocks.OAK_LOG).mapColor(MapColor.COLOR_PURPLE).strength(0.6F, 0).sounds(SoundType.WOOD).notSolid()));
	public static final Block NOMAD_MUSHROOM_STEM = ModItems.registerBlock( "nomad_mushroom_stem", new NomadMushroomStemBlock(FabricBlockSettings.copyOf(Blocks.OAK_LOG).mapColor(MapColor.TERRACOTTA_WHITE).strength(0.6F, 0).sounds(SoundType.WOOD).notSolid()));

	// Register Mod Recipes
	public static final RecipeSerializer<NomadBookCraftRecipe> CRAFT_NOMAD_BOOK = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Constants.MODID + ":crafting_special_nomadbookcraft", new SimpleCraftingRecipeSerializer<>(NomadBookCraftRecipe::new));
	public static final RecipeSerializer<NomadBookHeightUpgradeRecipe> UPGRADE_HEIGHT_NOMAD_BOOK = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Constants.MODID + ":crafting_special_nomadbookupgradeheight", new SimpleCraftingRecipeSerializer<>(NomadBookHeightUpgradeRecipe::new));
	public static final RecipeSerializer<NomadBookDismantleRecipe> DISMANTLE_NOMAD_BOOK = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Constants.MODID + ":crafting_special_nomadbookdismantle", new SimpleCraftingRecipeSerializer<>(NomadBookDismantleRecipe::new)); // Special
	public static final RecipeSerializer<NomadBookUpgradeRecipe> UPGRADE_NOMAD_BOOK = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Constants.MODID + ":crafting_special_nomadbookupgrade", new SimpleCraftingRecipeSerializer<>(NomadBookUpgradeRecipe::new));
	public static final RecipeSerializer<NomadBookInkRecipe> INK_NOMAD_BOOK = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Constants.MODID + ":crafting_special_nomadbookink", new SimpleCraftingRecipeSerializer<>(NomadBookInkRecipe::new));
	public static final RecipeSerializer<NetherNomadBookCraftRecipe> CRAFT_NETHER_NOMAD_BOOK = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Constants.MODID + ":crafting_special_nethernomadbookcraft", new SimpleCraftingRecipeSerializer<>(NetherNomadBookCraftRecipe::new));

	@Override
	public void onInitialize() {
		LOGGER.info(Constants.MODID + " Loaded");

		// Register Mod Items and Creative Tab
		ModItems.registerCreativeTabs();

		// Add Loot Tables
		UniformGenerator lootTableRange = UniformGenerator.between(0, 1);
		UniformGenerator strongholdRange = UniformGenerator.between(0, 3);

		// Map the pool names with our loot additions
		Map<ResourceLocation, LootPool.Builder> lootTableConfigurations = Map.of(
				CARTOGRAPHER_CHEST_LOOT_TABLE_ID, LootPool.lootPool().setRolls(lootTableRange).add(LootItem.lootTableItem(ModItems.GRASS_PAGE)),
				DUNGEON_CHEST_LOOT_TABLE_ID, LootPool.lootPool().setRolls(lootTableRange).add(LootItem.lootTableItem(ModItems.GRASS_PAGE)),
				MINESHAFT_CHEST_LOOT_TABLE_ID, LootPool.lootPool().setRolls(lootTableRange).add(LootItem.lootTableItem(ModItems.GRASS_PAGE)),
				OUTPOST_CHEST_LOOT_TABLE_ID, LootPool.lootPool().setRolls(lootTableRange).add(LootItem.lootTableItem(ModItems.GRASS_PAGE)),
				STRONGHOLD_LIBRARY_CHEST_LOOT_TABLE_ID, LootPool.lootPool().setRolls(strongholdRange).add(LootItem.lootTableItem(ModItems.GRASS_PAGE)),
				TEMPLE_CHEST_LOOT_TABLE_ID, LootPool.lootPool().setRolls(lootTableRange).add(LootItem.lootTableItem(ModItems.GRASS_PAGE)),
				TREASURE_CHEST_LOOT_TABLE_ID, LootPool.lootPool().setRolls(lootTableRange).add(LootItem.lootTableItem(ModItems.GRASS_PAGE)),
				BONUS_CHEST_LOOT_TABLE_ID, LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(LootItem.lootTableItem(ModItems.NOMAD_BOOK))
						.apply(SetNbtFunction.setTag(Util.make(new CompoundTag(), compoundTag -> compoundTag.put(Constants.MODID, Util.make(new CompoundTag(), child -> {
							child.putInt(Constants.HEIGHT, 1);
							child.putInt(Constants.WIDTH, 3);
							child.putString(Constants.STRUCTURE, NomadBookItem.DEFAULT_STRUCTURE_PATH);
						})))).build())
		);

		// Using our map, edit the loot pools
		lootTableConfigurations.forEach((id, poolBuilder) -> LootTableEvents.MODIFY.register((resourceManager, lootManager, tableId, supplier, setter) -> {
			if (id.equals(tableId)) {
				supplier.withPool(poolBuilder);
			}
		}));

	}

}
