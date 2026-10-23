package com.example.visualaid.client.mixin;

import com.example.visualaid.client.VisualAidClientConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Forza isGlowing() a restituire true per il PROPRIO giocatore quando il
 * toggle e' attivo. E' puramente cosmetico e locale: modifica solo come il
 * tuo client disegna te stesso, non cambia nulla lato server ne' per gli
 * altri giocatori (ognuno vede questa mod solo sul proprio client).
 */
@Mixin(Entity.class)
public abstract class EntityGlowMixin {

	@Inject(method = "isGlowing", at = @At("RETURN"), cancellable = true)
	private void visualaid$forceSelfGlow(CallbackInfoReturnable<Boolean> cir) {
		if (!VisualAidClientConfig.selfGlowEnabled) {
			return;
		}

		Entity self = (Entity) (Object) this;
		MinecraftClient client = MinecraftClient.getInstance();

		if (client.player != null && self == client.player) {
			cir.setReturnValue(true);
		}
	}
}
