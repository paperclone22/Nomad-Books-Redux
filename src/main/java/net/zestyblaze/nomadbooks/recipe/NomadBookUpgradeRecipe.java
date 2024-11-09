package net.zestyblaze.nomadbooks.recipe;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.zestyblaze.nomadbooks.NomadBooks;
import net.zestyblaze.nomadbooks.item.BookUpgradeItem;
import net.zestyblaze.nomadbooks.item.ModItems;
import net.zestyblaze.nomadbooks.item.NomadBookItem;
import net.zestyblaze.nomadbooks.util.Constants;

import static net.zestyblaze.nomadbooks.util.Helper.findItem;
import static net.zestyblaze.nomadbooks.util.Helper.hasNoExtraItems;

public class NomadBookUpgradeRecipe extends SpecialCraftingRecipe {
    public NomadBookUpgradeRecipe(/*Identifier resourceLocation, */CraftingRecipeCategory category) {
        // TODO make a Tag for the upgrade pages
        super(/*resourceLocation.toString(), */category/*, new ItemStack(ModItems.NOMAD_BOOK),*/
            /*DefaultedList.copyOf(Ingredient.EMPTY, Ingredient.ofStacks(ModItems.NOMAD_BOOK.getDefaultStack(), ModItems.NETHER_NOMAD_BOOK.getDefaultStack()), Ingredient.ofItems(ModItems.AQUATIC_MEMBRANE_PAGE, ModItems.MYCELIUM_PAGE, ModItems.SPACIAL_DISPLACER_PAGE))*/);
    }

    @Override
    public boolean matches(RecipeInputInventory container, World level) {
        ItemStack book = findItem(container, item -> item.getItem() instanceof NomadBookItem);
        ItemStack upgrade = findItem(container, item -> item.getItem() instanceof BookUpgradeItem);

        return book != null && upgrade != null && hasNoExtraItems(container, 1);
    }

    @Override
    public ItemStack craft(RecipeInputInventory container, DynamicRegistryManager registryAccess) {
        ItemStack book = findItem(container, item -> item.getItem() instanceof NomadBookItem);
        ItemStack upgrade = findItem(container, item -> item.getItem() instanceof BookUpgradeItem);

        if (book != null && upgrade != null && hasNoExtraItems(container, 1)) {
            ItemStack result = book.copy();
            NbtCompound tag = result.getOrCreateSubNbt(Constants.MODID);
            NbtList upgradeList = tag.getList(Constants.UPGRADES, NbtElement.STRING_TYPE);

            if (!upgradeList.contains(NbtString.of(getUpgrade(upgrade)))) {
                upgradeList.add(NbtString.of(getUpgrade(upgrade)));
                tag.put(Constants.UPGRADES, upgradeList);
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
        return width >= 3 && height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return NomadBooks.UPGRADE_NOMAD_BOOK;
    }
}
