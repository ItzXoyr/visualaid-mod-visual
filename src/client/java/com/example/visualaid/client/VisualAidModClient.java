package com.example.visualaid.client;

import com.example.visualaid.client.hud.AttackerIndicatorHud;
import com.example.visualaid.network.AttackerIndicatorPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class VisualAidModClient implements ClientModInitializer {

	// Tasto di default: G. L'utente puo' comunque rimapparlo da
	// Opzioni > Controlli > Tasti personalizzati > Visual Aid.
	private static final KeyBinding TOGGLE_GLOW_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.visualaid.toggle_glow",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_G,
			"category.visualaid"
	));

	@Override
	public void onInitializeClient() {
		// Riceve dal server l'id dell'entita' che ci ha colpiti.
		ClientPlayNetworking.registerGlobalReceiver(AttackerIndicatorPayload.ID, (payload, context) ->
				context.client().execute(() -> AttackerTracker.onAttackerPacket(payload.attackerEntityId()))
		);

		// Ogni tick: gestisce il tasto toggle e aggiorna il tracker dell'attaccante.
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (TOGGLE_GLOW_KEY.wasPressed()) {
				VisualAidClientConfig.selfGlowEnabled = !VisualAidClientConfig.selfGlowEnabled;
				sendFeedback(client, VisualAidClientConfig.selfGlowEnabled);
			}

			AttackerTracker.tick(client);
		});

		// Pulisce lo stato quando si esce dal mondo/server, cosi' non resta
		// un indicatore "fantasma" alla sessione successiva.
		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			if (client.world == null) {
				AttackerTracker.clear();
			}
		});

		// NB: dalla 1.20.6 in poi il secondo parametro di questo callback e' un
		// RenderTickCounter (non piu' un float). Se la tua versione di Fabric
		// API usa ancora `float tickDelta`, cambia semplicemente il tipo qui.
		HudRenderCallback.EVENT.register((drawContext, tickCounter) -> AttackerIndicatorHud.render(drawContext));
	}

	private static void sendFeedback(MinecraftClient client, boolean enabled) {
		if (client.player == null) {
			return;
		}
		String key = enabled ? "message.visualaid.glow_on" : "message.visualaid.glow_off";
		client.player.sendMessage(net.minecraft.text.Text.translatable(key), true);
	}
}
