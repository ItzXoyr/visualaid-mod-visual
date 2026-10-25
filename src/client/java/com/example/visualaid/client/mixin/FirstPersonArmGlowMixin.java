package com.example.visualaid.client.mixin;

import com.example.visualaid.client.VisualAidClientConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Quando la luminosita' e' attiva, tinge di colore il braccio/oggetto
 * disegnato in prima persona (l'unica parte del personaggio che Minecraft
 * disegna normalmente quando non sei in terza persona).
 */
@Mixin(GameRenderer.class)
public abstract class FirstPersonArmGlowMixin {

	private static final float GLOW_R = 1.0F;
	private static final float GLOW_G = 0.85F;
	private static final float GLOW_B = 0.2F;

	@Inject(method = "renderHand", at = @At("HEAD"))
	private void visualaid$tintArmStart(CallbackInfo ci) {
		if (VisualAidClientConfig.selfGlowEnabled) {
			RenderSystem.setShaderColor(GLOW_R, GLOW_G, GLOW_B, 1.0F);
		}
	}

	@Inject(method = "renderHand", at = @At("TAIL"))
	private void visualaid$tintArmEnd(CallbackInfo ci) {
		if (VisualAidClientConfig.selfGlowEnabled) {
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		}
	}
}
