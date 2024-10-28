package net.zestyblaze.nomadbooks.recipe;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
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
import net.zestyblaze.nomadbooks.item.BookUpgradeItem;
import net.zestyblaze.nomadbooks.item.ModItems;
import net.zestyblaze.nomadbooks.item.NomadBookItem;
import net.zestyblaze.nomadbooks.util.Constants;

import static net.zestyblaze.nomadbooks.util.Helper.findItem;
import static net.zestyblaze.nomadbooks.util.Helper.hasNoExtraItems;

public class NomadBookUpgradeRecipe extends ShapelessRecipe {
    public NomadBookUpgradeRecipe(ResourceLocation resourceLocation, CraftingBookCategory category) {
        // TODO make a Tag for the upgrade pages
        super(resourceLocation, "", category, new ItemStack(ModItems.NOMAD_BOOK),
            NonNullList.of(Ingredient.EMPTY, Ingredient.of(ModItems.NOMAD_BOOK.getDefaultInstance(), ModItems.NETHER_NOMAD_BOOK.getDefaultInstance()), Ingredient.of(ModItems.AQUATIC_MEMBRANE_PAGE, ModItems.MYCELIUM_PAGE, ModItems.SPACIAL_DISPLACER_PAGE)));
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        ItemStack book = findItem(container, item -> item.getItem() instanceof NomadBookItem);
        ItemStack upgrade = findItem(container, item -> item.getItem() instanceof BookUpgradeItem);

        return book != null && upgrade != null && hasNoExtraItems(container, 1);
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack book = findItem(container, item -> item.getItem() instanceof NomadBookItem);
        ItemStack upgrade = findItem(container, item -> item.getItem() instanceof BookUpgradeItem);

        if (book != null && upgrade != null && hasNoExtraItems(container, 1)) {
            ItemStack result = book.copy();
            CompoundTag tag = result.getOrCreateTagElement(Constants.MODID);
            ListTag upgradeList = tag.getList(Constants.UPGRADES, Tag.TAG_STRING);

            if (!upgradeList.contains(StringTag.valueOf(getUpgrade(upgrade)))) {
                upgradeList.add(StringTag.valueOf(getUpgrade(upgrade)));
                tag.put(Constants.UPGRADES, upgradeList);
            }

            return result;
        }

        return ItemStack.EMPTY;
    }

    private String getUpgrade(ItemStack itemStack) {
        return ((BookUpgradeItem) itemStack.getItem()).getUpgrade();
    }

    @Environment(EnvType.CLIENT)
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return NomadBooks.UPGRADE_NOMAD_BOOK;
    }
}
