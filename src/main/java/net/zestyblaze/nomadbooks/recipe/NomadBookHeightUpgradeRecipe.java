package net.zestyblaze.nomadbooks.recipe;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
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

import java.util.List;

import static net.zestyblaze.nomadbooks.util.Helper.findItem;

public class NomadBookHeightUpgradeRecipe extends ShapelessRecipe {
    public NomadBookHeightUpgradeRecipe(ResourceLocation id, CraftingBookCategory category) {
        // I decided to switch to single page. Multiple pages still works, but it shows just 1 in the Recipe book (BUT IT SHOWS AT ALL)
        super(id, "", category, new ItemStack(ModItems.NOMAD_BOOK), NonNullList.of(Ingredient.EMPTY,
            Ingredient.of(ModItems.NOMAD_BOOK.getDefaultInstance(), ModItems.NETHER_NOMAD_BOOK.getDefaultInstance()), Ingredient.of(ModItems.GRASS_PAGE)));
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        ItemStack book = findItem(container, stack -> stack.getItem() instanceof NomadBookItem);
        List<ItemStack> pageStacks = container.getItems().stream()
            .filter(stack -> stack.getItem().equals(ModItems.GRASS_PAGE)).toList();

        return book != null && !pageStacks.isEmpty() &&
            book.getOrCreateTag().getFloat(Constants.DEPLOYED) == 0.0f; // Not deployed
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack book = findItem(container, stack -> stack.getItem() instanceof NomadBookItem);
        List<Item> pageStacks = container.getItems().stream()
            .map(ItemStack::getItem)
            .filter(item -> item.equals(ModItems.GRASS_PAGE)).toList();

        if (book != null && !pageStacks.isEmpty() &&
            book.getOrCreateTag().getFloat(Constants.DEPLOYED) == 0.0f) { // Not deployed
            ItemStack ret = book.copy();
            int height = ret.getOrCreateTagElement(Constants.MODID).getInt(Constants.HEIGHT);
            CompoundTag tags = ret.getOrCreateTagElement(Constants.MODID);
            tags.putInt(Constants.HEIGHT, height + pageStacks.size());
            return ret;
        }
        return ItemStack.EMPTY;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return NomadBooks.UPGRADE_HEIGHT_NOMAD_BOOK;
    }
}
