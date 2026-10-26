package com.example.visualaid.client.mixin;

import com.example.visualaid.client.VisualAidClientConfig;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Quando la luminosita' e' attiva, disegna un vero contorno "Glowing" (lo
 * stesso effetto usato per i mob con l'effetto di stato Glowing) attorno al
 * braccio/oggetto in prima persona, avvolgendo il VertexConsumerProvider
 * usato per quel render in un OutlineVertexConsumerProvider dedicato e
 * forzandone il disegno a fine metodo.
 */
@Mixin(HeldItemRenderer.class)
public abstract class FirstPersonArmOutlineMixin {

	@Unique
	private OutlineVertexConsumerProvider visualaid$outlineProvider;

	@ModifyVariable(method = "renderFirstPersonItem", at = @At("HEAD"), argsOnly = true)
	private VertexConsumerProvider visualaid$wrapOutline(VertexConsumerProvider original) {
		if (!VisualAidClientConfig.selfGlowEnabled) {
			this.visualaid$outlineProvider = null;
			return original;
		}

		OutlineVertexConsumerProvider outline = new OutlineVertexConsumerProvider();
		// ARGB compattato in un solo int: giallo pieno opaco.
		outline.setColor(0xFFFFDD33);
		this.visualaid$outlineProvider = outline;
		return outline;
	}

	@Inject(method = "renderFirstPersonItem", at = @At("TAIL"))
	private void visualaid$flushOutline(CallbackInfo ci) {
		if (this.visualaid$outlineProvider != null) {
			this.visualaid$outlineProvider.draw();
			this.visualaid$outlineProvider = null;
		}
	}
}
