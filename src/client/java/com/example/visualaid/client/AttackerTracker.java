package com.example.visualaid.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

/**
 * Tiene traccia, lato client, dell'ultima entita' che ci ha colpiti.
 * Ad ogni tick client controlliamo se e' ancora:
 *  - viva e caricata nel mondo
 *  - entro il raggio massimo
 *  - visibile (nessun blocco nel mezzo)
 * Se una di queste condizioni fallisce, l'indicatore sparisce subito,
 * esattamente come richiesto ("se si nasconde dietro un muro la freccia
 * se ne va").
 */
public final class AttackerTracker {
	private AttackerTracker() {
	}

	private static Integer trackedEntityId = null;
	private static long lastHitTimeMs = 0L;

	/** Ultima direzione valida calcolata (per il rendering), null se non c'e' nulla da mostrare. */
	private static Vec3d lastKnownDirection = null;

	public static void onAttackerPacket(int attackerEntityId) {
		trackedEntityId = attackerEntityId;
		lastHitTimeMs = System.currentTimeMillis();
	}

	/** Da chiamare una volta per tick client (vedi VisualAidModClient). */
	public static void tick(MinecraftClient client) {
		if (trackedEntityId == null) {
			lastKnownDirection = null;
			return;
		}

		if (client.world == null || client.player == null) {
			clear();
			return;
		}

		// Timeout di sicurezza: se non arrivano nuovi colpi da un bel po', puliamo.
		if (System.currentTimeMillis() - lastHitTimeMs > VisualAidClientConfig.attackerIndicatorTimeoutMs) {
			clear();
			return;
		}

		Entity attacker = client.world.getEntityById(trackedEntityId);
		if (attacker == null || attacker.isRemoved() || !attacker.isAlive()) {
			clear();
			return;
		}

		ClientPlayerEntity player = client.player;

		double distanceSq = player.squaredDistanceTo(attacker);
		double maxRange = VisualAidClientConfig.maxAttackerTrackRange;
		if (distanceSq > maxRange * maxRange) {
			clear();
			return;
		}

		if (!hasLineOfSight(player, attacker)) {
			clear();
			return;
		}

		// Tutto ok: aggiorna la direzione da mostrare in HUD.
		Vec3d from = player.getEyePos();
		Vec3d to = attacker.getBoundingBox().getCenter();
		lastKnownDirection = to.subtract(from);
	}

	private static boolean hasLineOfSight(ClientPlayerEntity player, Entity attacker) {
		Vec3d from = player.getEyePos();
		Vec3d to = attacker.getBoundingBox().getCenter();

		RaycastContext context = new RaycastContext(
				from,
				to,
				RaycastContext.ShapeType.COLLIDER,
				RaycastContext.FluidHandling.NONE,
				player
		);

		// getWorld() e' stato rinominato getEntityWorld() nelle mappature recenti.
		HitResult result = player.getEntityWorld().raycast(context);
		return result.getType() == HitResult.Type.MISS;
	}

	public static void clear() {
		trackedEntityId = null;
		lastKnownDirection = null;
	}

	/** @return la direzione (world-space, non normalizzata) verso l'attaccante, o null se non c'e' nulla da mostrare. */
	public static Vec3d getDirectionOrNull() {
		return lastKnownDirection;
	}
}
