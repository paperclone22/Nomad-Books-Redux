package net.zestyblaze.nomadbooks.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.model.ModelTemplates;
import net.zestyblaze.nomadbooks.NomadBooks;
import net.zestyblaze.nomadbooks.item.ModItems;


public class ModModelProvider extends FabricModelProvider {
	public ModModelProvider(FabricDataOutput output) {
		super(output);
	}

	@Override
	public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
		blockStateModelGenerator.createTrivialCube(NomadBooks.MEMBRANE);
	}

	@Override
	public void generateItemModels(ItemModelGenerators itemModelGenerator) {
		itemModelGenerator.generateFlatItem(ModItems.GRASS_PAGE, ModelTemplates.FLAT_ITEM);
		itemModelGenerator.generateFlatItem(ModItems.AQUATIC_MEMBRANE_PAGE, ModelTemplates.FLAT_ITEM);
		itemModelGenerator.generateFlatItem(ModItems.MYCELIUM_PAGE, ModelTemplates.FLAT_ITEM);
		itemModelGenerator.generateFlatItem(ModItems.SPACIAL_DISPLACER_PAGE, ModelTemplates.FLAT_ITEM);
	}

}
