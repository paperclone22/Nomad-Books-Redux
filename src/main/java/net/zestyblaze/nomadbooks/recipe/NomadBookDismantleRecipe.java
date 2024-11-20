package net.zestyblaze.nomadbooks.recipe;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;
import net.zestyblaze.nomadbooks.NomadBooks;
import net.zestyblaze.nomadbooks.item.ModItems;
import net.zestyblaze.nomadbooks.item.NomadBookItem;
import net.zestyblaze.nomadbooks.util.Constants;
import net.zestyblaze.nomadbooks.util.NomadBooksComponent;

import java.util.List;
import java.util.Objects;

import static net.zestyblaze.nomadbooks.util.Helper.findItem;
import static net.zestyblaze.nomadbooks.util.Helper.hasNoExtraItems;
import static net.zestyblaze.nomadbooks.util.NomadBooksYACLConfig.defaultStandardBookHeight;
import static net.zestyblaze.nomadbooks.util.NomadBooksYACLConfig.defaultStandardBookWidth;

// The main reason this recipe is special is to prevent players from dismantling an un-deployed camp
// If deployed books had their own item_id, then I could safely remove this recipe
public class NomadBookDismantleRecipe extends SpecialCraftingRecipe {

	public NomadBookDismantleRecipe(CraftingRecipeCategory category) {
		super(category);
	}

	@Override
	public boolean matches(RecipeInputInventory container, World level) {
		ItemStack book = findItem(container, item -> item.getItem() instanceof NomadBookItem);
		return book != null && hasNoExtraItems(container, 0) && Objects.requireNonNull(book.get(NomadBooks.NOMAD_BOOK_DATA)).isDeployed();  // is deployed
	}

	@Override
	public ItemStack craft(RecipeInputInventory container, RegistryWrapper.WrapperLookup registryAccess) {
		ItemStack book = findItem(container, item -> item.getItem() instanceof NomadBookItem);

		if (book != null) {
			int amount = calculateAmount(book);
			return new ItemStack(ModItems.GRASS_PAGE, amount); // 🟩
		} else {
			return ItemStack.EMPTY;
		}
	}

	private int calculateAmount(ItemStack book) {
		NomadBooksComponent tags = book.get(NomadBooks.NOMAD_BOOK_DATA);
		if(tags == null) {
			tags = new NomadBooksComponent(false, false, defaultStandardBookHeight, defaultStandardBookWidth, NomadBookItem.DEFAULT_STRUCTURE_PATH, List.of());
		}
		int upgradesCount = tags.upgrades().size();
		if(upgradesCount > 0 && tags.upgrades().contains(Constants.AQUATIC_MEMBRANE)) {
			upgradesCount -= 1;
		}
		return tags.height() - defaultStandardBookHeight + 3 + upgradesCount; // Grass pages are now returned 1 to 1
	}

	@Override
	public boolean fits(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return NomadBooks.DISMANTLE_NOMAD_BOOK;
	}
}
