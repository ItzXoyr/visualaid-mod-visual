# Visual Aid — mod Fabric per Minecraft 1.21.1

Mod che aggiunge due funzionalità:

1. **Luminosità personaggio** — premi **G** per attivare/disattivare un
   contorno luminoso (come l'effetto "Glowing") sul tuo personaggio.
   È puramente visivo e lato client: lo vedi solo tu, non cambia nulla per
   gli altri giocatori né per il server.
2. **Indicatore direzionale dell'attaccante** — quando subisci danno da
   un'entità (mob o giocatore), compare una freccia rossa sullo schermo che
   indica la direzione da cui arriva l'attacco. La freccia **sparisce
   automaticamente** se:
   - l'attaccante si nasconde dietro un muro/blocco (niente più "linea di
     vista" diretta),
   - l'attaccante esce dal raggio massimo (di default 48 blocchi,
     modificabile in `VisualAidClientConfig`),
   - non arriva nessun nuovo colpo da 10 secondi (rete di sicurezza).

## Importante: serve anche lato server

A differenza di una mod puramente estetica, per sapere con certezza **chi**
ti ha colpito serve un'informazione che solo il server conosce (specialmente
per danni a distanza). Per questo:

- **Singleplayer / mondo LAN**: funziona subito, perché il gioco usa un
  server integrato che include automaticamente la mod.
- **Server dedicato**: la mod va installata anche sul server (oltre che sul
  client), non solo lato client.

La parte "luminosità personaggio" invece è puramente client-side e non
richiede nulla lato server.

## Build automatica (consigliato)

Il repository include una GitHub Action (`.github/workflows/build.yml`) che
compila la mod automaticamente ad ogni push e carica il `.jar` come
artifact scaricabile dalla scheda **Actions** del repository. Se crei un
tag `v*` (es. `v1.0.0`), l'action pubblica anche una Release con il jar
allegato.

Questo progetto **non include i file del Gradle wrapper**
(`gradlew`, `gradlew.bat`, `gradle-wrapper.jar`): la CI usa
`gradle/actions/setup-gradle`, che installa Gradle da sé, quindi non ne ha
bisogno. Se vuoi compilare in locale e preferisci non avere Gradle
installato tu, genera il wrapper una volta con `gradle wrapper` (richiede
Gradle installato) e da lì in poi userai `./gradlew`.

## Come compilare (in locale)

Questo progetto segue la struttura standard del **Fabric Example Mod**.
Per compilarlo ti serve:

- **JDK 21**
- Connessione internet (Gradle scarica Minecraft, le mappature Yarn e
  Fabric API la prima volta)

Passaggi:

```bash
cd visualaid-mod
# genera il wrapper se non presente (richiede gradle installato una volta)
gradle wrapper
./gradlew build
```

In alternativa, apri semplicemente la cartella in **IntelliJ IDEA** con il
plugin Fabric/Gradle: IntelliJ genererà il wrapper e scaricherà le
dipendenze automaticamente.

Il jar compilato lo trovi in `build/libs/visualaid-1.0.0.jar`: copialo nella
cartella `mods` della tua installazione Fabric (client e/o server, come
installato sopra) insieme a [Fabric API](https://modrinth.com/mod/fabric-api).

### Nota sulle versioni esatte

Le versioni di Yarn mappings / Fabric Loader / Fabric API cambiano spesso.
Ho messo nel `gradle.properties` dei numeri plausibili per la 1.21.1, ma se
la build fallisce per un problema di dipendenze mancanti, vai su
https://fabricmc.net/develop/, seleziona Minecraft 1.21.1 e copia i numeri
di versione aggiornati in `gradle.properties`. Il codice stesso non cambia.

## Struttura del progetto

```
src/main/java      -> codice comune (gira su client E server)
src/main/resources -> fabric.mod.json, lingue
src/client/java     -> codice solo-client (mixin, HUD, keybind, tracking)
src/client/resources -> config mixin del client
```

File principali:

- `VisualAidMod.java` — lato server/comune: ascolta i danni subiti dai
  giocatori e manda un pacchetto al client con l'id dell'attaccante.
- `network/AttackerIndicatorPayload.java` — il pacchetto custom usato per
  comunicare l'id dell'attaccante dal server al client.
- `client/AttackerTracker.java` — logica di tracciamento: raycast per la
  linea di vista, controllo distanza, timeout.
- `client/hud/AttackerIndicatorHud.java` — disegna la freccia sullo schermo.
- `client/mixin/EntityGlowMixin.java` — forza `isGlowing()` a true per il
  giocatore locale quando il toggle è attivo.
- `client/VisualAidModClient.java` — registra il tasto G, il tick client e
  il render HUD.

## Personalizzazioni facili

In `client/VisualAidClientConfig.java`:

- `maxAttackerTrackRange` — distanza massima (blocchi) per mostrare la freccia.
- `attackerIndicatorTimeoutMs` — dopo quanto tempo senza nuovi colpi la
  freccia sparisce comunque.

In `client/hud/AttackerIndicatorHud.java`:

- `RING_RADIUS` — quanto lontano dal centro schermo viene disegnata la freccia.
- `COLOR` — colore della freccia (ARGB).

Il tasto di default è **G**, ma è rimappabile da
`Opzioni > Controlli > Tasti personalizzati > Visual Aid`.
