package net.zestyblaze.nomadbooks.util;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.Box;

import java.util.function.Predicate;

public class Helper {

	private Helper(){}

	/**
	 * Return an ItemStack based on a Predicate test condition.
	 * Useful for searching inventories and constructing special recipes.
	 */
	public static ItemStack findItem(CraftingRecipeInput container, Predicate<ItemStack> predicate) {
		return container.getStacks().stream()
				.filter(predicate)
				.findFirst()
				.orElse(null); // do not use ItemStack.Empty here. it will mess up many checks
	}

	public static BlockBox convertAABBtoBoundingBox(Box entityBB) {
		return new BlockBox((int) Math.floor(entityBB.minX), (int) Math.floor(entityBB.minY), (int) Math.floor(entityBB.minZ),
				(int) Math.ceil(entityBB.maxX), (int) Math.ceil(entityBB.maxY), (int) Math.ceil(entityBB.maxZ));
	}

	/**
	 * Return true if all extra crafting slots are empty
	 */
	public static boolean hasNoExtraItems(CraftingRecipeInput container, int myDesiredItemsAmount) {
		int emptySlots = container.getStacks().stream()
				.filter(ItemStack::isEmpty)
				.toList().size();
		int allSlotsMinusFilledSlots = container.getSize() - 1 - myDesiredItemsAmount;
		return emptySlots == allSlotsMinusFilledSlots;
	}

	/**
	 * Generic helper method to provide an alternative if null pointer is encountered from the first argument
	 */
	public static <T> T getOrElse(T first, T second) {
		return (first != null) ? first : second;
	}

//	Compat stuff
	public static boolean isModLoaded(String id) {
		return FabricLoader.getInstance().isModLoaded(id);
	}

//	YACL
	public static boolean isYACLLoaded() {
		return isModLoaded("yet_another_config_lib_v3");
	}

}
