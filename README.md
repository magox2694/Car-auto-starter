# CarBeats

CarBeats e un progetto Android Studio in Kotlin per una app Android Auto ufficiale,
costruita con Android for Cars App Library e orientata alla fruizione audio in auto.

## Cosa include

- CarAppService
- Session
- schermata iniziale con ListTemplate
- schermata dettaglio con MessageTemplate
- MainActivity companion sul telefono con ricerca demo e controlli playlist (play, prev, next, pausa, riprendi, stop)
- PlaybackService con Media3 ExoPlayer + MediaSessionService
- ricerca rapida e ricerca libera lato Android Auto con risultati demo e play da lista
- manifest configurato per categoria MEDIA Android Auto

## Obiettivo

CarBeats e una base per un'app media audio-first:

- catalogo contenuti
- playlist
- riproduzione in background
- UI sicura e coerente con l'uso in auto

## Stato attuale

Il progetto include una base funzionante di playback audio in background.
La companion app avvia un sample audio remoto, consente ricerca demo locale e controlla il player via servizio media.
La ricerca usa un repository a provider multipli con cache in memoria, validazione URL, client HTTP dedicato e diagnostica provider esplicita.

Prima della pubblicazione devi:

1. sostituire dati demo e branding finale
2. integrare catalogo reale (playlist, ricerca, cronologia)
3. testare su Android Auto e DHU
4. completare i requisiti qualita Play per app in auto

## Come aprirlo

1. Apri Android Studio
2. Seleziona Open e scegli questa cartella
3. Lascia completare la sincronizzazione Gradle
4. Avvia su telefono e testa la parte car in Android Auto

## Test Android Auto

1. Attiva Developer Mode in Android Auto
2. Collega il telefono (USB o wireless, se supportato)
3. Verifica che CarBeats compaia nel launcher Android Auto
4. In alternativa usa Desktop Head Unit (DHU)

## Test senza auto

### Test rapido su telefono

1. Avvia l'app sul telefono
2. Cerca un brano demo (es: Night o Acoustic) e premi Cerca e riproduci
3. Scegli un numero risultato e premi Riproduci risultato selezionato
4. Oppure premi Riproduci playlist demo
5. Usa Brano precedente e Brano successivo per testare la coda
6. Verifica riproduzione in background uscendo dall'app
7. Controlla notifica media e lockscreen
8. Verifica i pulsanti Pausa, Riprendi e Stop

### Test con DHU su Windows

1. Sul telefono apri Android Auto e attiva Developer Mode
2. In Android Auto Developer Settings attiva Start head unit server
3. Collega il telefono via USB
4. Avvia DHU dal percorso SDK: extras/google/auto/desktop-head-unit
5. Verifica che CarBeats compaia e navighi correttamente

Nota: telefono + DHU coprono quasi tutto il ciclo sviluppo. Il test in auto reale resta utile per validare head unit e firmware specifici.

## File principali

- app/src/main/java/com/example/carbeats/MyCarAppService.kt
- app/src/main/java/com/example/carbeats/MyCarSession.kt
- app/src/main/java/com/example/carbeats/HomeScreen.kt
- app/src/main/java/com/example/carbeats/PlaceDetailScreen.kt
- app/src/main/java/com/example/carbeats/CarSearchScreen.kt
- app/src/main/java/com/example/carbeats/CarSearchResultsScreen.kt
- app/src/main/java/com/example/carbeats/MainActivity.kt
- app/src/main/java/com/example/carbeats/PlaybackService.kt
- app/src/main/java/com/example/carbeats/AudioCatalog.kt
- app/src/main/java/com/example/carbeats/search/SearchRepository.kt
- app/src/main/java/com/example/carbeats/search/DemoTrackSearchProvider.kt
- app/src/main/java/com/example/carbeats/search/YouTubeMetadataProvider.kt

## Nota YouTube

Ricerca metadata YouTube e possibile con YouTube Data API v3 (API key, quote e rispetto dei Terms).
Streaming audio o estrazione da YouTube per riproduzione diretta richiede diritti/licenze e conformita ai termini della piattaforma.
Per un'app pubblicabile e stabile, usa catalogo e stream di cui possiedi i diritti o provider ufficialmente licenziati.

Aggiornamento corrente:
1. provider YouTube metadata attivo con endpoint ufficiale v3
2. risultati YouTube sono metadata-only sia su telefono sia su Android Auto
3. playback diretto disponibile solo per sorgenti stream validate http/https
4. la ricerca distingue tra nessun risultato, provider non raggiungibile e provider non configurato

### Configurazione chiave API YouTube

1. genera una API key per YouTube Data API v3 su Google Cloud
2. aggiungi in gradle.properties (locale):

YOUTUBE_API_KEY=la_tua_chiave

3. sincronizza Gradle e riavvia l'app

## Personalizzazioni rapide

- nome app: app/src/main/res/values/strings.xml
- tema: app/src/main/res/values/themes.xml
- categoria Android Auto: app/src/main/AndroidManifest.xml
- contenuti demo lista: app/src/main/java/com/example/carbeats/HomeScreen.kt

