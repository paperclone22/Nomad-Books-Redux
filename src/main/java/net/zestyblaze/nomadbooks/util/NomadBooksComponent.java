package net.zestyblaze.nomadbooks.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.dynamic.Codecs;

import java.util.List;

public record NomadBooksComponent(boolean isDeployed, boolean doDisplayBoundaries, int height, int width, String structure, List<String> upgrades) {
	public static final Codec<NomadBooksComponent> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
				Codec.BOOL.fieldOf("is_deployed").forGetter(NomadBooksComponent::isDeployed),
				Codec.BOOL.fieldOf("do_display_boundaries").forGetter(NomadBooksComponent::doDisplayBoundaries),
				Codecs.POSITIVE_INT.fieldOf("height").forGetter(NomadBooksComponent::height),
				Codecs.POSITIVE_INT.fieldOf("width").forGetter(NomadBooksComponent::width),
				Codec.STRING .fieldOf("structure").forGetter(NomadBooksComponent::structure),
				Codec.STRING.listOf().fieldOf("upgrades").forGetter(NomadBooksComponent::upgrades)
		)
				.apply(instance, NomadBooksComponent::new)
	);

	public static final PacketCodec<ByteBuf, NomadBooksComponent> PACKET_CODEC = PacketCodec.tuple( // NOTE: tuple() only allows up to 6 child codecs. I separated this into multiple components
			PacketCodecs.BOOL, NomadBooksComponent::isDeployed,
			PacketCodecs.BOOL, NomadBooksComponent::doDisplayBoundaries,
			PacketCodecs.VAR_INT, NomadBooksComponent::height,
			PacketCodecs.VAR_INT, NomadBooksComponent::width,
			PacketCodecs.STRING, NomadBooksComponent::structure,
			PacketCodecs.STRING.collect(PacketCodecs.toList()), NomadBooksComponent::upgrades,
			NomadBooksComponent::new
	);
}
