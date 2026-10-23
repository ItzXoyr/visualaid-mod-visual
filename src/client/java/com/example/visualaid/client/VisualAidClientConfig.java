package com.example.visualaid.client;

/**
 * Stato/config client-side molto semplice, tenuta in memoria (si resetta
 * ad ogni riavvio del gioco). Se vuoi che venga salvata su file, si puo'
 * estendere facilmente con una libreria di config o leggendo/scrivendo un
 * json in .minecraft/config/visualaid.json.
 */
public final class VisualAidClientConfig {
	private VisualAidClientConfig() {
	}

	/** Se true, il proprio personaggio viene renderizzato con il contorno luminoso. */
	public static boolean selfGlowEnabled = false;

	/** Distanza massima (in blocchi) oltre la quale la freccia dell'attaccante sparisce. */
	public static double maxAttackerTrackRange = 48.0;

	/**
	 * Se non arriva nessun nuovo colpo da questo attaccante entro questo
	 * tempo (ms), la freccia sparisce comunque, anche se l'attaccante e'
	 * ancora visibile e in range (rete di sicurezza contro indicatori
	 * "appiccicati" all'infinito).
	 */
	public static long attackerIndicatorTimeoutMs = 10_000L;
}
