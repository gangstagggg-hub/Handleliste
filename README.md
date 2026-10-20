# Handleliste – Android-app (WebView)

Handlelisten pakket som en vanlig Android-app. Siden, skriftene og Firebase-biblioteket ligger **inne i appen**, så den åpner uten nett. Alt du legger inn lagres lokalt på telefonen.

## Få APK-en (ingen installasjon på PC nødvendig)

1. Opprett et **nytt** repo på GitHub, f.eks. `handleliste-app` (må være separat fra repoet som har nettsiden din).
2. Last opp **hele innholdet** i denne mappen (dra og slipp fra PC, inkludert mappen `.github`). Hvis `.github` ikke blir med: *Add file → Create new file*, skriv `.github/workflows/build-apk.yml` som filnavn og lim inn innholdet fra filen med samme navn.
3. Gå til fanen **Actions**. Jobben «Bygg AAB og APK» starter selv (tar 3–5 minutter). Hvis ikke: velg den og trykk *Run workflow*.
4. Når den er grønn: gå til **Releases** (høyre side på repo-forsiden) → **Handleliste.aab** er filen til Google Play, **Handleliste.apk** kan installeres direkte på telefonen.
5. Åpne filen. Android ber om lov til å installere fra nettleseren/Filer første gang – godta.

Hver gang du endrer noe i repoet bygges en ny APK, og den installeres rett over den gamle uten å miste data (samme signeringsnøkkel).

## Viktig å vite

- **Data fra nettversjonen følger ikke med.** Appen har sin egen lagring, atskilt fra nettsiden/PWA-en. Listene du har i dag må legges inn på nytt (eller flyttes via deling, når Firebase er satt opp).
- **Avinstallerer du appen, slettes dataene.** Android kan gjenopprette dem fra Google-sikkerhetskopi hvis den er på.
- **Delelenker** peker til `https://gangstagggg-hub.github.io/Handleliste/`. Har nettsiden en annen adresse, endre `WEB_BASE` i `app/src/main/assets/index.html` og `android:host`/`pathPrefix` i `AndroidManifest.xml`.
- **Deling mellom telefoner** krever at Firebase-verdiene er limt inn i `index.html` (se `firebaseConfig`), som i nettversjonen.
- `app/handleliste.jks` er en enkel nøkkel kun for sideloading. Google Play-versjonen signeres med opplastingsnøkkelen fra GitHub Secrets (under).

## Google Play (AAB)

1. I repoet: *Settings → Secrets and variables → Actions → New repository secret*. Lag fire secrets med verdiene fra `GITHUB-SECRETS.txt` i mappen `play-nokkel`: `UPLOAD_KEYSTORE_BASE64`, `UPLOAD_STORE_PASSWORD`, `UPLOAD_KEY_ALIAS`, `UPLOAD_KEY_PASSWORD`.
2. Kjør *Actions → Run workflow*. Last ned **Handleliste.aab** fra Releases.
3. Play Console → *Opprett app* → velg testspor (f.eks. lukket test) → *Opprett ny utgivelse* → last opp `.aab`. Si ja til **Play App Signing**.
4. Fyll ut: personvernerklæring (URL), Data safety, innholdsvurdering, målgruppe, butikkoppføring (ikon 512×512 = `play-ikon-512.png`, funksjonsgrafikk 1024×500, minst 2 skjermbilder).
5. Personlige utviklerkontoer opprettet etter 13. nov. 2023 må ha en lukket test med minst 12 testere i 14 dager før produksjon.

Appen er satt til målversjon Android 16 (API 36), kant-til-kant og «predictive back», slik Play krever. Pakkenavnet `no.handleliste.app` kan ikke endres etter publisering. Sideload-APK-en og Play-versjonen har ulik signatur: avinstaller den gamle før du installerer fra Play.

## Oppdatere appen

Bytt ut `app/src/main/assets/index.html` (resten av filene i `assets` følger med) og last opp. Ny APK bygges automatisk.

## Åpne i Android Studio (valgfritt)

*File → Open* på denne mappen. Studio lager selv Gradle-wrapper og henter alt som trengs.

## Hva er i prosjektet

| Del | Innhold |
|---|---|
| `app/src/main/assets/` | `index.html` (appen), `fonts/` (Fraunces og Inter), `vendor/` (Firebase) |
| `MainActivity.java` | WebView, tilbake-knapp, stemme, deling, hold skjermen våken, åpne delelenker |
| `res/` | Adaptivt ikon (+ ensfarget til Android 13+), farger og lyst/mørkt tema |
