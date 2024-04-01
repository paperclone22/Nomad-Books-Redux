package net.zestyblaze.nomadbooks.recipe;

import com.google.common.collect.Lists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
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

public class NomadBookHeightUpgradeRecipe extends ShapelessRecipe {
    public NomadBookHeightUpgradeRecipe(ResourceLocation id, CraftingBookCategory category) {
        // I decided to switch to single page. Multiple pages still works, but it shows just 1 in the Recipe book (BUT IT SHOWS AT ALL)
        super(id, "", category, new ItemStack(ModItems.NOMAD_BOOK), NonNullList.of(Ingredient.EMPTY, Ingredient.of(ModItems.NOMAD_BOOK.getDefaultInstance(), ModItems.NETHER_NOMAD_BOOK.getDefaultInstance()), Ingredient.of(ModItems.GRASS_PAGE)));
    }

    //TODO simplify matches and assemble
    @Override
    public boolean matches(CraftingContainer container, Level level) {
        ItemStack bookStack = ItemStack.EMPTY;
        List<ItemStack> pageStacks = Lists.newArrayList();

        for (int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() instanceof NomadBookItem) {
                    if (!bookStack.isEmpty()) {
                        return false;
                    }
                    bookStack = stack;
                } else if (!stack.getItem().equals(ModItems.GRASS_PAGE)) {
                    return false;
                } else {
                    pageStacks.add(stack);
                }
            }
        }
        return !bookStack.isEmpty() && !pageStacks.isEmpty() &&
            bookStack.getOrCreateTag().getFloat(Constants.DEPLOYED) == 0.0f; // Not deployed
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack bookStack = ItemStack.EMPTY;
        List<Item> pageItems = Lists.newArrayList();

        for (int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                Item item = stack.getItem();
                if (item instanceof NomadBookItem) {
                    if (!bookStack.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                    bookStack = stack.copy();
                } else if (!item.equals(ModItems.GRASS_PAGE)) {
                    return ItemStack.EMPTY;
                } else {
                    pageItems.add(item);
                }
            }
        }
        if (!bookStack.isEmpty() && !pageItems.isEmpty() &&
            bookStack.getOrCreateTag().getFloat(Constants.DEPLOYED) == 0.0f) { // Not deployed
            int height = bookStack.getOrCreateTagElement(Constants.MODID).getInt(Constants.HEIGHT);
            bookStack.getOrCreateTagElement(Constants.MODID).putInt(Constants.HEIGHT, height + pageItems.size());
            return bookStack;
        } else {
            return ItemStack.EMPTY;
        }
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
