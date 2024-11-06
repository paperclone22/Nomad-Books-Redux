package net.zestyblaze.nomadbooks.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;
import net.zestyblaze.nomadbooks.NomadBooks;
import net.zestyblaze.nomadbooks.item.ModItems;


public class ModModelProvider extends FabricModelProvider {
	public ModModelProvider(FabricDataOutput output) {
		super(output);
	}

	@Override
	public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
		blockStateModelGenerator.registerSimpleCubeAll(NomadBooks.MEMBRANE);
	}

	@Override
	public void generateItemModels(ItemModelGenerator itemModelGenerator) {
		itemModelGenerator.register(ModItems.GRASS_PAGE, Models.GENERATED);
		itemModelGenerator.register(ModItems.AQUATIC_MEMBRANE_PAGE, Models.GENERATED);
		itemModelGenerator.register(ModItems.MYCELIUM_PAGE, Models.GENERATED);
		itemModelGenerator.register(ModItems.SPACIAL_DISPLACER_PAGE, Models.GENERATED);
	}

}
