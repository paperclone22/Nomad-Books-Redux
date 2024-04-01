package net.zestyblaze.nomadbooks;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.zestyblaze.nomadbooks.datagen.ModBlockTagProvider;
import net.zestyblaze.nomadbooks.datagen.ModModelProvider;
import net.zestyblaze.nomadbooks.datagen.ModRecipeProvider;

public class NomadBooksDataGenerator implements DataGeneratorEntrypoint {

	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		pack.addProvider(ModBlockTagProvider::new);
		pack.addProvider(ModModelProvider::new);
		pack.addProvider(ModRecipeProvider::new);
	}
}
