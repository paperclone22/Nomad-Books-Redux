package net.zestyblaze.nomadbooks.util;

public class NomadBooksYACLConfig {

//	private NomadBooksYACLConfig() {}

	// TODO re-add this
//	public static final ConfigClassHandler<NomadBooksYACLConfig> CONFIG = ConfigClassHandler.createBuilder(NomadBooksYACLConfig.class)
//			.serializer(config -> GsonConfigSerializerBuilder.create(config)
//					.setPath(FabricLoader.getInstance().getConfigDir().resolve("nomad_books.json5"))
//					.setJson5(true)
//					.build())
//			.build();
//
//	@SuppressWarnings("final")
//	@SerialEntry(comment = "Number of blocks fungi checks above clicked position on deployment")
//	public static int checksAboveOnDeploy = 2; // NOSONAR
//	@SuppressWarnings("final")
//	@SerialEntry(comment = "List of additional blocks that will be treated as air")
//	public static List<String> airReplaceable = Lists.newArrayList("minecraft:poppy", "minecraft:dandelion"); // NOSONAR
//	@SuppressWarnings("final")
//	@SerialEntry(comment = "List of additional blocks that will be prevent displacement")
//	public static List<String> notSpacialDisplaceable = Lists.newArrayList(); // NOSONAR
//
//	/** @noinspection AccessStaticViaInstance*/
//	public static Screen createScreen(Screen parent) {
//		return YetAnotherConfigLib.create(CONFIG, ((defaults, config, builder) -> builder
//				.title(Text.translatable("nomadbooks.yacl.title"))
//				.category(ConfigCategory.createBuilder()
//						.name(Text.translatable("nomadbooks.yacl.title"))
//						.option(Option.<Integer>createBuilder()
//								.name(Text.translatable("nomadbooks.yacl.checks_above_on_deploy"))
//								.description(OptionDescription.of(Text.translatable("nomadbooks.yacl.checks_above_on_deploy.desc")))
//								.binding(defaults.checksAboveOnDeploy, () -> config.checksAboveOnDeploy, newVal -> config.checksAboveOnDeploy = newVal) // NOSONAR
//								.controller(option -> IntegerSliderControllerBuilder.create(option)
//										.range(0, 5)
//										.step(1))
//								.build())
//						.option(ListOption.<String>createBuilder()
//								.name(Text.translatable("nomadbooks.yacl.air_replaceable"))
//								.description(OptionDescription.of(Text.translatable("nomadbooks.yacl.air_replaceable.desc")))
//								.binding(defaults.airReplaceable, () -> config.airReplaceable, newVal -> config.airReplaceable = newVal) // NOSONAR
//								.initial("minecraft:")
//								.insertEntriesAtEnd(true)
//								.controller(StringControllerBuilder::create)
//								.build())
//						.option(ListOption.<String>createBuilder()
//								.name(Text.translatable("nomadbooks.yacl.not_spacial_displaceable"))
//								.description(OptionDescription.of(Text.translatable("nomadbooks.yacl.not_spacial_displaceable.desc")))
//								.binding(defaults.notSpacialDisplaceable, () -> config.notSpacialDisplaceable, newVal -> config.notSpacialDisplaceable = newVal) // NOSONAR
//								.initial("minecraft:")
//								.insertEntriesAtEnd(true)
//								.controller(StringControllerBuilder::create)
//								.build())
//						.build()
//				))).generateScreen(parent);
//	}

}
