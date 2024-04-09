package net.zestyblaze.nomadbooks.util;

import com.google.common.collect.Lists;
import eu.midnightdust.lib.config.MidnightConfig;

import java.util.List;

@SuppressWarnings("final")
public class NomadBooksConfig extends MidnightConfig {
	@Entry(min = 0, max = 5, isSlider = true) public static int checksAboveOnDeploy = 2;
	@Entry(name = "Additional Air Replaceable Blocks") public static List<String> airReplaceable = Lists.newArrayList("minecraft:poppy", "minecraft:dandelion");
	@Entry(name = "Blocks that should not me displaced") public static List<String> notSpacialDisplaceable = Lists.newArrayList();
}
