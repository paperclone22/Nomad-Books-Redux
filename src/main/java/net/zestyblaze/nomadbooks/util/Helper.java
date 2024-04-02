package net.zestyblaze.nomadbooks.util;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public class Helper {

	private Helper(){}

	/**
	 * Return an ItemStack based on a Predicate test condition.
	 * Useful for searching inventories and constructing special recipes.
	 * @param container net.minecraft.world.inventory.CraftingContainer
	 * @param predicate java.util.function.Predicate of net.minecraft.world.item.ItemStack
	 * @return net.minecraft.world.item.ItemStack
	 */
	public static ItemStack findItem(CraftingContainer container, Predicate<ItemStack> predicate) {
		return container.getItems().stream()
				.filter(predicate)
				.findFirst()
				.orElse(null); // do not use ItemStack.Empty here. it will mess up many checks
	}

}
