package net.zestyblaze.nomadbooks.recipe;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

public class NetherNomadBookCraftRecipe extends ShapelessRecipe {
    public NetherNomadBookCraftRecipe(ResourceLocation resourceLocation, CraftingBookCategory category) {
        super(resourceLocation, "", category, getCraftResult(),
            NonNullList.of(Ingredient.EMPTY, Ingredient.of(ModItems.NOMAD_BOOK.getDefaultInstance()), Ingredient.of(Items.NETHERITE_INGOT)));
    }

    public static ItemStack getCraftResult() {
        ItemStack result = new ItemStack(ModItems.NETHER_NOMAD_BOOK);
        result.getOrCreateTagElement(Constants.MODID).putInt(Constants.HEIGHT, 3);
        result.getOrCreateTagElement(Constants.MODID).putInt(Constants.WIDTH, 7);

        return result;
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        ItemStack book = findItem(container, item -> item.getItem() instanceof NomadBookItem);
        ItemStack ingot = findItem(container, item -> item.is(Items.NETHERITE_INGOT));
        return book != null && ingot != null && book.getOrCreateTag().getFloat(Constants.DEPLOYED) == 0.0f; // Not deployed
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack book = findItem(container, item -> item.getItem() instanceof NomadBookItem);
        ItemStack ingot = findItem(container, item -> item.is(Items.NETHERITE_INGOT));

        if (book != null && ingot != null && book.getOrCreateTag().getFloat(Constants.DEPLOYED) == 0.0f) { // Not deployed
            ItemStack result = getCraftResult(); // TODO perhaps on craft, increase the height and width of the input book ? Might want to wait until after adding "ender page" upgrade
            applyBookDataToResult(book, result);
            return result;
        }

        return ItemStack.EMPTY;
    }

    private void applyBookDataToResult(ItemStack book, ItemStack result) {
        CompoundTag bookTag = book.getOrCreateTagElement(Constants.MODID);
        CompoundTag resultTag = result.getOrCreateTagElement(Constants.MODID);

        resultTag.putInt(Constants.HEIGHT, bookTag.getInt(Constants.HEIGHT));
        resultTag.putInt(Constants.WIDTH, bookTag.getInt(Constants.WIDTH));
        resultTag.put(Constants.UPGRADES, bookTag.getList(Constants.UPGRADES, Tag.TAG_STRING));
        resultTag.putString("Structure", bookTag.getString(Constants.STRUCTURE).equals(NomadBookItem.DEFAULT_STRUCTURE_PATH) ?
            NomadBookItem.NETHER_DEFAULT_STRUCTURE_PATH : bookTag.getString(Constants.STRUCTURE));
    }

    @Environment(EnvType.CLIENT)
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return NomadBooks.CRAFT_NETHER_NOMAD_BOOK;
    }
}
