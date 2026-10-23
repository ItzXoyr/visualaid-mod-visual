package com.example.visualaid;

import com.example.visualaid.network.AttackerIndicatorPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point comune (gira sia su client che su server, incluso il server
 * integrato del singleplayer). Qui registriamo solo il necessario per capire
 * CHI ha colpito il giocatore e per avvisare il client tramite un pacchetto.
 *
 * Perche' serve lato server? Perche' il client da solo non puo' sapere con
 * certezza quale entita' lo ha colpito (specialmente a distanza, con frecce
 * o incantesimi): questa informazione la conosce con sicurezza solo il
 * server, quindi va installata la mod anche lato server (o si gioca in
 * singleplayer/LAN, dove il server integrato la usa comunque).
 */
public class VisualAidMod implements ModInitializer {
	public static final String MOD_ID = "visualaid";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// Registra il tipo di pacchetto custom usato per avvisare il client
		// di chi lo ha colpito.
		PayloadTypeRegistry.playS2C().register(
				AttackerIndicatorPayload.ID,
				AttackerIndicatorPayload.CODEC
		);

		// Ogni volta che una entita' subisce danno sul server, se e' un
		// giocatore e il danno ha un "attacker" riconoscibile, lo comunichiamo
		// al client di quel giocatore.
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
			if (damageTaken <= 0.0F) {
				return;
			}
			if (!(entity instanceof ServerPlayerEntity player)) {
				return;
			}
			if (source.getAttacker() == null) {
				// Danno ambientale (caduta, fuoco, fame...): nessuna freccia da mostrare.
				return;
			}

			ServerPlayNetworking.send(player, new AttackerIndicatorPayload(source.getAttacker().getId()));
		});

		LOGGER.info("[VisualAid] Mod inizializzata.");
	}
}
