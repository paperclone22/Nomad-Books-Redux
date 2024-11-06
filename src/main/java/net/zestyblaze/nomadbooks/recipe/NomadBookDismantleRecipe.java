package net.zestyblaze.nomadbooks.recipe;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.zestyblaze.nomadbooks.NomadBooks;
import net.zestyblaze.nomadbooks.item.ModItems;
import net.zestyblaze.nomadbooks.item.NomadBookItem;
import net.zestyblaze.nomadbooks.util.Constants;

import static net.zestyblaze.nomadbooks.util.Helper.findItem;
import static net.zestyblaze.nomadbooks.util.Helper.hasNoExtraItems;

public class NomadBookDismantleRecipe extends ShapelessRecipe {

    public NomadBookDismantleRecipe(Identifier id, CraftingRecipeCategory category) {
        // just don't specify the output count and it still works as normal while showing in the recipe book
        // WONT FIX: if I want to show that the recipe requires the camp to be deployed, I need the following NBT on the input itemstack: .getOrCreateTagElement(Constants.MODID).putFloat(Constants.DEPLOYED, 1F)
        super(id, "", category, new ItemStack(ModItems.GRASS_PAGE),
            DefaultedList.copyOf(Ingredient.EMPTY, Ingredient.ofStacks(ModItems.NOMAD_BOOK.getDefaultStack(), ModItems.NETHER_NOMAD_BOOK.getDefaultStack())));
    }
    @Override
    public boolean matches(RecipeInputInventory container, World level) {
        ItemStack book = findItem(container, item -> item.getItem() instanceof NomadBookItem);
        return book != null && hasNoExtraItems(container, 0) && book.getOrCreateNbt().getFloat(Constants.DEPLOYED) == 1.0f;  // is deployed
    }

    @Override
    public ItemStack craft(RecipeInputInventory container, DynamicRegistryManager registryAccess) {
        ItemStack book = findItem(container, item -> item.getItem() instanceof NomadBookItem);

        if (book != null) {
            int amount = calculateAmount(book);
            return new ItemStack(ModItems.GRASS_PAGE, amount); // 🟩
        } else {
            return ItemStack.EMPTY;
        }
    }

    private int calculateAmount(ItemStack book) {
        int height = book.getOrCreateSubNbt(Constants.MODID).getInt(Constants.HEIGHT);
        int width = book.getOrCreateSubNbt(Constants.MODID).getInt(Constants.WIDTH);
        int upgrades = book.getOrCreateSubNbt(Constants.MODID).getList(Constants.UPGRADES, NbtElement.STRING_TYPE).size();

        return 3 + height - 1 + (width - 3) / 2 + upgrades;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public boolean fits(int width, int height) {
        return width + height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return NomadBooks.DISMANTLE_NOMAD_BOOK;
    }
}
