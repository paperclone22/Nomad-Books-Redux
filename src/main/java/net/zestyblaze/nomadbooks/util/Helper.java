package net.zestyblaze.nomadbooks.util;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.Box;

import java.util.function.Predicate;

public class Helper {

	private Helper(){}

	public static final String DISPLAY = "display";
	public static final String COLOR = "color";

	/**
	 * Return an ItemStack based on a Predicate test condition.
	 * Useful for searching inventories and constructing special recipes.
	 */
	public static ItemStack findItem(RecipeInputInventory container, Predicate<ItemStack> predicate) {
		return container.getInputStacks().stream()
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
	public static boolean hasNoExtraItems(RecipeInputInventory container, int myDesiredItemsAmount) {
        int emptySlots = container.getInputStacks().stream()
            .filter(ItemStack::isEmpty)
            .toList().size();
        int allSlotsMinusFilledSlots = container.size() - 1 - myDesiredItemsAmount;
        return emptySlots == allSlotsMinusFilledSlots;
	}

}
