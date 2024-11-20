package net.zestyblaze.nomadbooks.recipe;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;
import net.zestyblaze.nomadbooks.NomadBooks;
import net.zestyblaze.nomadbooks.item.BookUpgradeItem;
import net.zestyblaze.nomadbooks.item.NomadBookItem;
import net.zestyblaze.nomadbooks.util.NomadBooksComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.zestyblaze.nomadbooks.util.Helper.findItem;
import static net.zestyblaze.nomadbooks.util.Helper.hasNoExtraItems;

// could be vanilla style only if each upgrade was its own component. Not the worst idea. But maybe worse than jei/rei/emi support
public class NomadBookUpgradeRecipe extends SpecialCraftingRecipe {
	public NomadBookUpgradeRecipe(CraftingRecipeCategory category) {
		super(category);
	}

	@Override
	public boolean matches(RecipeInputInventory container, World level) {
		ItemStack book = findItem(container, item -> item.getItem() instanceof NomadBookItem);
		ItemStack upgrade = findItem(container, item -> item.getItem() instanceof BookUpgradeItem);

		return book != null && upgrade != null && hasNoExtraItems(container, 1);
	}

	@Override
	public ItemStack craft(RecipeInputInventory container, RegistryWrapper.WrapperLookup registryAccess) {
		ItemStack book = findItem(container, item -> item.getItem() instanceof NomadBookItem);
		ItemStack upgrade = findItem(container, item -> item.getItem() instanceof BookUpgradeItem);

		if (book != null && upgrade != null && hasNoExtraItems(container, 1)) {
			ItemStack result = book.copy();
			Optional<NomadBooksComponent> tags = Optional.ofNullable(result.get(NomadBooks.NOMAD_BOOK_DATA));
			if(tags.isEmpty()) {
				return ItemStack.EMPTY;
			}
			List<String> upgradeList = new ArrayList<>(tags.get().upgrades());

			if (!upgradeList.contains(getUpgrade(upgrade))) {
				upgradeList.add(getUpgrade(upgrade));
				result.set(NomadBooks.NOMAD_BOOK_DATA, new NomadBooksComponent(tags.get().isDeployed(), tags.get().doDisplayBoundaries(), tags.get().height(), tags.get().width(), tags.get().structure(), upgradeList));
			}

			return result;
		}

		return ItemStack.EMPTY;
	}

	private String getUpgrade(ItemStack itemStack) {
		return ((BookUpgradeItem) itemStack.getItem()).getUpgrade();
	}

	@Override
	public boolean fits(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return NomadBooks.UPGRADE_NOMAD_BOOK;
	}
}
