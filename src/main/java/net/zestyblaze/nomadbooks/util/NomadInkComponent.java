package net.zestyblaze.nomadbooks.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.List;

public record NomadInkComponent(boolean isInked, int inkProgress, int inkGoal, List<String> visitedBiomes) {

	public static final Codec<NomadInkComponent> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
				Codec.BOOL.fieldOf("is_inked").forGetter(NomadInkComponent::isInked),
				Codec.INT.fieldOf("ink_progress").forGetter(NomadInkComponent::inkProgress),
				Codec.INT.fieldOf("ink_goal").forGetter(NomadInkComponent::inkGoal),
				Codec.STRING.listOf().fieldOf("visited_biomes").forGetter(NomadInkComponent::visitedBiomes)
			)
					.apply(instance, NomadInkComponent::new)
	);

	public static final PacketCodec<ByteBuf, NomadInkComponent> PACKET_CODEC = PacketCodec.tuple(
			PacketCodecs.BOOL, NomadInkComponent::isInked,
			PacketCodecs.INTEGER, NomadInkComponent::inkProgress,
			PacketCodecs.INTEGER, NomadInkComponent::inkGoal,
			PacketCodecs.STRING.collect(PacketCodecs.toList()), NomadInkComponent::visitedBiomes,
			NomadInkComponent::new
	);
}
