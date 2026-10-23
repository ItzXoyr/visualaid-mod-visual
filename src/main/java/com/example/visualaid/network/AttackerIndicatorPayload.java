package com.example.visualaid.network;

import com.example.visualaid.VisualAidMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Pacchetto inviato dal server al client: "l'entita' con questo id ti ha
 * appena colpito". Il client user questo per mostrare la freccia direzionale.
 */
public record AttackerIndicatorPayload(int attackerEntityId) implements CustomPayload {
	public static final CustomPayload.Id<AttackerIndicatorPayload> ID =
			new CustomPayload.Id<>(Identifier.of(VisualAidMod.MOD_ID, "attacker_indicator"));

	public static final PacketCodec<RegistryByteBuf, AttackerIndicatorPayload> CODEC =
			PacketCodec.tuple(
					PacketCodecs.VAR_INT, AttackerIndicatorPayload::attackerEntityId,
					AttackerIndicatorPayload::new
			);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
