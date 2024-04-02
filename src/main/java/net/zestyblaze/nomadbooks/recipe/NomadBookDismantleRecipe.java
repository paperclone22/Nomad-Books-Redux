package net.zestyblaze.nomadbooks.recipe;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import net.zestyblaze.nomadbooks.NomadBooks;
import net.zestyblaze.nomadbooks.item.ModItems;
import net.zestyblaze.nomadbooks.item.NomadBookItem;
import net.zestyblaze.nomadbooks.util.Constants;

import static net.zestyblaze.nomadbooks.util.Helper.findItem;

public class NomadBookDismantleRecipe extends ShapelessRecipe {

    public NomadBookDismantleRecipe(ResourceLocation id, CraftingBookCategory category) {
        // just don't specify the output count and it still works as normal while showing in the recipe book
        // WONT FIX: if I want to show that the recipe requires the camp to be deployed, I need the following NBT on the input itemstack: .getOrCreateTagElement(Constants.MODID).putFloat(Constants.DEPLOYED, 1F)
        super(id, "", category, new ItemStack(ModItems.GRASS_PAGE),
            NonNullList.of(Ingredient.EMPTY, Ingredient.of(ModItems.NOMAD_BOOK.getDefaultInstance(), ModItems.NETHER_NOMAD_BOOK.getDefaultInstance())));
    }
    @Override
    public boolean matches(CraftingContainer container, Level level) {
        ItemStack book = findItem(container, item -> item.getItem() instanceof NomadBookItem);
        return book != null && book.getOrCreateTag().getFloat(Constants.DEPLOYED) == 1.0f;  // is deployed
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack book = findItem(container, item -> item.getItem() instanceof NomadBookItem);

        if (book != null) {
            int amount = calculateAmount(book);
            return new ItemStack(ModItems.GRASS_PAGE, amount); // 🟩
        } else {
            return ItemStack.EMPTY;
        }
    }

    private int calculateAmount(ItemStack book) {
        int height = book.getOrCreateTagElement(Constants.MODID).getInt(Constants.HEIGHT);
        int width = book.getOrCreateTagElement(Constants.MODID).getInt(Constants.WIDTH);
        int upgrades = book.getOrCreateTagElement(Constants.MODID).getList(Constants.UPGRADES, Tag.TAG_STRING).size();

        return 3 + height - 1 + (width - 3) / 2 + upgrades;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width + height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return NomadBooks.DISMANTLE_NOMAD_BOOK;
    }
}
