# CarAutoStarter

Progetto base Android Studio in Kotlin per una **app Android Auto ufficiale** costruita con la **Android for Cars App Library**.

## Ispirazione: CarTube

**CarTube** è un esempio noto di app Android Auto non ufficiale che porta YouTube nell'interfaccia di Android Auto, permettendo la navigazione e la riproduzione di video direttamente dall'head unit dell'auto.

Questo template è stato aggiornato per usare la categoria **VIDEO** come punto di partenza, che è quella più adatta per app di streaming video come CarTube.  
Con questa base puoi costruire un client video per Android Auto che:
- elenca playlist o canali preferiti
- mostra i dettagli di un video prima della riproduzione
- integra `MediaTemplate` o `NavigationTemplate` per controlli multimediali a bordo

> **Nota**: le app video non ufficiali per Android Auto (come CarTube) richiedono l'attivazione della modalità sviluppatore su Android Auto per funzionare. Le app pubblicate su Google Play devono rispettare le linee guida della categoria scelta e superare la review di Play.

## Cosa include
- `CarAppService`
- `Session`
- `Screen` iniziale con `ListTemplate`
- schermata dettaglio con `MessageTemplate`
- `MainActivity` companion sul telefono
- manifest già configurato per Android Auto
- categoria attuale: **VIDEO**

## Nota importante
Questo progetto è un **template dimostrativo** ispirato ad app come CarTube. Prima della pubblicazione devi:
1. scegliere una categoria car supportata coerente con la tua app (es. `VIDEO`, `POI`, `NAVIGATION`)
2. sostituire dati demo e branding
3. testare con Android Auto / DHU
4. completare i requisiti qualità Play per le app in auto

## Come aprirlo
1. Apri Android Studio
2. `Open` → seleziona questa cartella
3. Lascia che Android Studio sincronizzi Gradle
4. Se Android Studio ti propone di aggiornare AGP/Kotlin, puoi accettare
5. Esegui su telefono o usa i tool di test Android Auto

## Test Android Auto
- attiva Developer Mode in Android Auto
- collega il telefono
- verifica che l'app compaia nel launcher di Android Auto
- in alternativa usa il Desktop Head Unit / emulatori

## File principali
- `app/src/main/java/com/example/carautostarter/MyCarAppService.kt`
- `app/src/main/java/com/example/carautostarter/MyCarSession.kt`
- `app/src/main/java/com/example/carautostarter/HomeScreen.kt`
- `app/src/main/java/com/example/carautostarter/VideoDetailScreen.kt`

## Personalizzazioni rapide
- nome app: `res/values/strings.xml`
- colore tema: `res/values/themes.xml`
- categoria Android Auto: `AndroidManifest.xml` (attualmente `VIDEO`)
- video demo nella lista: `HomeScreen.kt`
