package net.zestyblaze.nomadbooks.util;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;

import java.util.function.Predicate;

public class Helper {

	private Helper(){}

	public static final String DISPLAY = "display";
	public static final String COLOR = "color";

	/**
	 * Return an ItemStack based on a Predicate test condition.
	 * Useful for searching inventories and constructing special recipes.
	 */
	public static ItemStack findItem(CraftingContainer container, Predicate<ItemStack> predicate) {
		return container.getItems().stream()
				.filter(predicate)
				.findFirst()
				.orElse(null); // do not use ItemStack.Empty here. it will mess up many checks
	}

	public static BoundingBox convertAABBtoBoundingBox(AABB entityBB) {
		return new BoundingBox((int) Math.floor(entityBB.minX), (int) Math.floor(entityBB.minY), (int) Math.floor(entityBB.minZ),
				(int) Math.ceil(entityBB.maxX), (int) Math.ceil(entityBB.maxY), (int) Math.ceil(entityBB.maxZ));
	}

}
