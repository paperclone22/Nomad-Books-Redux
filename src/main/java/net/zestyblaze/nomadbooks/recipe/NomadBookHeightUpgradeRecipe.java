package net.zestyblaze.nomadbooks.recipe;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;
import net.zestyblaze.nomadbooks.NomadBooks;
import net.zestyblaze.nomadbooks.item.ModItems;
import net.zestyblaze.nomadbooks.item.NomadBookItem;
import net.zestyblaze.nomadbooks.util.NomadBooksComponent;

import java.util.List;
import java.util.Objects;

import static net.zestyblaze.nomadbooks.util.Helper.findItem;
import static net.zestyblaze.nomadbooks.util.Helper.hasNoExtraItems;

// This one will never be vanilla style.
// Even the most basic form in a smithing table would need to read height for a height += 1 operation.
// I would need some magic mixins to pull this off as vanilla. More magic than even the Itinerant Ink hack
public class NomadBookHeightUpgradeRecipe extends SpecialCraftingRecipe {
	public NomadBookHeightUpgradeRecipe(CraftingRecipeCategory category) {
		super(category);
	}

	@Override
	public boolean matches(RecipeInputInventory container, World level) {
		ItemStack book = findItem(container, stack -> stack.getItem() instanceof NomadBookItem);
		List<ItemStack> pageStacks = container.getHeldStacks().stream()
				.filter(stack -> stack.getItem().equals(ModItems.GRASS_PAGE)).toList();

		return book != null && !pageStacks.isEmpty() && hasNoExtraItems(container, pageStacks.size()) &&
				!Objects.requireNonNull(book.get(NomadBooks.NOMAD_BOOK_DATA)).isDeployed(); // Not deployed
	}

	@Override
	public ItemStack craft(RecipeInputInventory container, RegistryWrapper.WrapperLookup registryAccess) {
		ItemStack book = findItem(container, stack -> stack.getItem() instanceof NomadBookItem);
		List<Item> pageStacks = container.getHeldStacks().stream()
				.map(ItemStack::getItem)
				.filter(item -> item.equals(ModItems.GRASS_PAGE)).toList();

		if (book != null && !pageStacks.isEmpty() && hasNoExtraItems(container, pageStacks.size()) &&
				!Objects.requireNonNull(book.get(NomadBooks.NOMAD_BOOK_DATA)).isDeployed()) { // Not deployed
			ItemStack ret = book.copy();
			NomadBooksComponent tags = ret.get(NomadBooks.NOMAD_BOOK_DATA);
			assert tags != null;
			ret.set(NomadBooks.NOMAD_BOOK_DATA, new NomadBooksComponent(false, false, tags.height() + pageStacks.size(), tags.width(), tags.structure(), tags.upgrades()));
			return ret;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public boolean fits(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return NomadBooks.UPGRADE_HEIGHT_NOMAD_BOOK;
	}
}
