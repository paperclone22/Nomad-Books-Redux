package net.zestyblaze.nomadbooks.util;

import com.google.common.collect.Lists;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

import static net.zestyblaze.nomadbooks.util.Constants.MINECRAFT_YACL;

public class NomadBooksYACLConfig { // NOSONAR

	public static final ConfigClassHandler<NomadBooksYACLConfig> CONFIG = ConfigClassHandler.createBuilder(NomadBooksYACLConfig.class)
			.serializer(config -> GsonConfigSerializerBuilder.create(config)
					.setPath(FabricLoader.getInstance().getConfigDir().resolve("nomad_books.json5"))
					.setJson5(true)
					.build())
			.build();

	@SuppressWarnings("final")
	@SerialEntry(comment = "Number of blocks fungi checks above clicked position on deployment")
	public static int checksAboveOnDeploy = 2; // NOSONAR
	@SuppressWarnings("final")
	@SerialEntry(comment = "List of additional blocks that will be treated as air")
	public static List<String> airReplaceable = Lists.newArrayList("minecraft:allium", "minecraft:azure_bluet", "minecraft:blue_orchid", "minecraft:cornflower", "minecraft:dandelion", "minecraft:lilac", "minecraft:lily_of_the_valley", "minecraft:orange_tulip", "minecraft:oxeye_daisy", "minecraft:peony", "minecraft:pink_tulip", "minecraft:poppy", "minecraft:red_tulip", "minecraft:rose_bush", "minecraft:sunflower", "minecraft:torchflower", "minecraft:white_tulip", "minecraft:wither_rose"); // NOSONAR
	@SuppressWarnings("final")
	@SerialEntry(comment = "List of additional blocks that will be prevent displacement. Also contains the camp blocks blacklist")
	public static List<String> notSpacialDisplaceable = Lists.newArrayList("minecraft:nether_portal", "minecraft:obsidian"); // NOSONAR // I added nether_portal & obsidian here because its an easy and common player built structure that could be used as a protection block to prevent displacement. they would still be available to be created inside a camp. // NOTE: notSpacialDisplaceable includes campBlocksBlacklist. eg. anything that can't be included in a structure, shouldn't be treated as terrain and shouldn't be moved
	@SuppressWarnings("final")
	@SerialEntry(comment = "Blacklist of blocks that won't be saved into a camp/structure")
	public  static List<String> campBlocksBlacklist = Lists.newArrayList("minecraft:bedrock", "minecraft:end_gateway", "minecraft:end_portal_frame", "minecraft:barrier", "minecraft:command_block", "minecraft:chain_command_block", "minecraft:repeating_command_block", "minecraft:jigsaw", "minecraft:light", "minecraft:structure_block"); // NOSONAR

	/** @noinspection AccessStaticViaInstance*/
	public static Screen createScreen(Screen parent) {
		return YetAnotherConfigLib.create(CONFIG, ((defaults, config, builder) -> builder
				.title(Text.translatable("nomadbooks.yacl.title"))
				.category(ConfigCategory.createBuilder()
						.name(Text.translatable("nomadbooks.yacl.title"))
						.option(Option.<Integer>createBuilder()
								.name(Text.translatable("nomadbooks.yacl.checks_above_on_deploy"))
								.description(OptionDescription.of(Text.translatable("nomadbooks.yacl.checks_above_on_deploy.desc")))
								.binding(defaults.checksAboveOnDeploy, () -> config.checksAboveOnDeploy, newVal -> config.checksAboveOnDeploy = newVal) // NOSONAR
								.controller(option -> IntegerSliderControllerBuilder.create(option)
										.range(0, 5)
										.step(1))
								.build())
						.option(ListOption.<String>createBuilder()
								.name(Text.translatable("nomadbooks.yacl.air_replaceable"))
								.description(OptionDescription.of(Text.translatable("nomadbooks.yacl.air_replaceable.desc")))
								.binding(defaults.airReplaceable, () -> config.airReplaceable, newVal -> config.airReplaceable = newVal) // NOSONAR
								.initial(MINECRAFT_YACL)
								.insertEntriesAtEnd(true)
								.controller(StringControllerBuilder::create)
								.build())
						.option(ListOption.<String>createBuilder()
								.name(Text.translatable("nomadbooks.yacl.not_spacial_displaceable"))
								.description(OptionDescription.of(Text.translatable("nomadbooks.yacl.not_spacial_displaceable.desc")))
								.binding(defaults.notSpacialDisplaceable, () -> config.notSpacialDisplaceable, newVal -> config.notSpacialDisplaceable = newVal) // NOSONAR
								.initial(MINECRAFT_YACL)
								.insertEntriesAtEnd(true)
								.controller(StringControllerBuilder::create)
								.build())
						.option(ListOption.<String>createBuilder()
								.name(Text.translatable("nomadbooks.yacl.camp_blocks_blacklist"))
								.description(OptionDescription.of(Text.translatable("nomadbooks.yacl.camp_blocks_blacklist.desc")))
								.binding(defaults.campBlocksBlacklist, () -> config.campBlocksBlacklist, newVal -> config.campBlocksBlacklist = newVal) // NOSONAR
								.initial(MINECRAFT_YACL)
								.insertEntriesAtEnd(true)
								.controller(StringControllerBuilder::create)
								.build())
						.build()
				))).generateScreen(parent);
	}

}
