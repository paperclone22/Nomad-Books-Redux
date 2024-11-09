package net.zestyblaze.nomadbooks.recipe;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.world.World;
import net.zestyblaze.nomadbooks.NomadBooks;
import net.zestyblaze.nomadbooks.item.NomadBookItem;

import java.util.List;

import static net.zestyblaze.nomadbooks.util.Helper.findItem;
import static net.zestyblaze.nomadbooks.util.Helper.hasNoExtraItems;

public class DyeNomadBookRecipe extends SpecialCraftingRecipe {
	// see https://github.com/Lythom/capsule/blob/a1881ed43d9445ed48c31caa7710e2979359ed19/src/main/java/capsule/recipes/DyeCapsuleRecipe.java#L98
	public DyeNomadBookRecipe(CraftingRecipeCategory category) {
		super(category);
	}

	@Override
	public boolean matches(RecipeInputInventory container, World level) {
		ItemStack book = findItem(container, stack -> stack.getItem() instanceof NomadBookItem);
		List<DyeItem> dyes = container.getHeldStacks().stream()
				.map(ItemStack::getItem)
				.filter(DyeItem.class::isInstance).map(DyeItem.class::cast).toList();

		return book != null && !dyes.isEmpty() && hasNoExtraItems(container, dyes.size());
	}

	@Override
	public ItemStack craft(RecipeInputInventory container, DynamicRegistryManager registryAccess) {
		ItemStack book = findItem(container, stack -> stack.getItem() instanceof NomadBookItem);
		List<DyeItem> dyes = container.getHeldStacks().stream()
				.map(ItemStack::getItem)
				.filter(DyeItem.class::isInstance).map(DyeItem.class::cast).toList();
		ItemStack ret = book != null ? book.copy() : ItemStack.EMPTY;

		return !ret.isEmpty() && !dyes.isEmpty() && hasNoExtraItems(container, dyes.size()) ? DyeableItem.blendAndSetColor(ret, dyes) : ItemStack.EMPTY;
	}

	@Override
	public boolean fits(int width, int height) {
		return width >= 3 && height >= 3;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return NomadBooks.DYE_NOMAD_BOOK;
	}
}
