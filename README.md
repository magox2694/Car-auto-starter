# CarTube

Progetto base Android Studio in Kotlin per una **app Android Auto ufficiale** costruita con la **Android for Cars App Library**, personalizzata in stile **CarTube**.

## Cosa include
- `CarAppService`
- `Session`
- `Screen` iniziale con `ListTemplate`
- schermata dettaglio con `PaneTemplate`
- `MainActivity` companion sul telefono
- manifest già configurato per Android Auto
- categoria attuale: **POI** (Point of Interest)

## Nota importante
Questo progetto è un **template dimostrativo**. Prima della pubblicazione devi:
1. scegliere una categoria car supportata coerente con la tua app
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
- `app/src/main/java/com/example/carautostarter/PlaceDetailScreen.kt`

## Personalizzazioni rapide
- nome app: `res/values/strings.xml`
- colore tema: `res/values/themes.xml`
- categoria Android Auto: `AndroidManifest.xml`
- righe demo della lista: `HomeScreen.kt`

## Stato attuale dell'app
Al momento l'app è un **template funzionante** per Android Auto:
- avvio servizio car (`CarAppService`) e sessione (`Session`) configurati
- schermata home con lista contenuti demo
- schermata dettaglio con messaggio informativo
- companion app base su telefono

In pratica: base tecnica pronta, ma il prodotto non è ancora completo per la pubblicazione.

## Roadmap MVP (prossimi passi)
1. definire categoria finale Android Auto coerente col caso d'uso
2. sostituire contenuti demo con dati reali (locale o backend)
3. completare UX e flussi consentiti in auto/parcheggio
4. testare su Android Auto reale e/o Desktop Head Unit (DHU)
5. hardening pre-release (host validation, quality checklist Play)

## YouTube su Android Auto: stato reale
Se l'obiettivo è "player video YouTube sul display dell'auto", è importante chiarire che **Android Auto non supporta la riproduzione video di YouTube su schermo auto durante la guida**.

Questo template è stato quindi impostato come **companion sicuro**:
- UI in auto: solo messaggi informativi conformi alle regole di sicurezza
- riproduzione video: demandata al telefono
- azioni sensibili: disponibili solo da fermo (parked-only)
