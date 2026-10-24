package com.example.visualaid.client.hud;

import com.example.visualaid.client.AttackerTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3x2fStack;

/**
 * Disegna una freccia sul bordo dello schermo che punta verso l'attaccante,
 * in stile "compass indicator": ruota intorno al centro dello schermo in
 * base all'angolo fra la direzione in cui guardi e la direzione
 * dell'attaccante.
 */
public final class AttackerIndicatorHud {
	private AttackerIndicatorHud() {
	}

	// Raggio (in pixel) a cui disegnare la freccia rispetto al centro schermo.
	private static final int RING_RADIUS = 90;
	private static final int ARROW_SIZE = 14;
	private static final int COLOR = 0xFFFF3B30; // rosso

	public static void render(DrawContext context) {
		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayerEntity player = client.player;
		if (player == null) {
			return;
		}

		Vec3d direction = AttackerTracker.getDirectionOrNull();
		if (direction == null) {
			return;
		}

		// Angolo della direzione verso l'attaccante sul piano orizzontale (gradi, sistema Minecraft).
		double targetYaw = Math.toDegrees(MathHelper.atan2(direction.x, direction.z)) + 180.0;

		// Angolo verso cui sta guardando il giocatore.
		double playerYaw = player.getYaw();

		// Differenza normalizzata fra -180 e 180: quanto devi ruotare rispetto allo sguardo.
		double relativeAngle = MathHelper.wrapDegrees(targetYaw - playerYaw);

		int screenWidth = context.getScaledWindowWidth();
		int screenHeight = context.getScaledWindowHeight();
		int centerX = screenWidth / 2;
		int centerY = screenHeight / 2;

		double radians = Math.toRadians(relativeAngle);
		// 0 gradi = davanti (freccia in alto), ruota in senso orario.
		int arrowX = centerX + (int) (Math.sin(radians) * RING_RADIUS);
		int arrowY = centerY - (int) (Math.cos(radians) * RING_RADIUS);

		drawArrow(context, arrowX, arrowY, relativeAngle);
	}

	private static void drawArrow(DrawContext context, int x, int y, double angleDegrees) {
		// Dalla 1.21.2 circa, il rendering 2D della GUI usa uno stack di
		// matrici 2D (JOML Matrix3x2fStack) invece del vecchio MatrixStack 3D.
		Matrix3x2fStack matrices = context.getMatrices();
		matrices.pushMatrix();
		matrices.translate(x, y);
		matrices.rotate((float) Math.toRadians(angleDegrees));

		int half = ARROW_SIZE / 2;

		// Triangolo semplice disegnato con rettangoli sottili (evitiamo dipendenze da texture custom).
		// Punta in alto, base in basso: ricorda una freccia/triangolo rivolto verso il bersaglio.
		for (int row = 0; row < ARROW_SIZE; row++) {
			int width = (int) ((row / (float) ARROW_SIZE) * ARROW_SIZE);
			int rowY = -half + row;
			context.fill(-width / 2, rowY, width / 2 + 1, rowY + 1, COLOR);
		}

		matrices.popMatrix();
	}
}
