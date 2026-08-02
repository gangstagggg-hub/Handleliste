# Handleliste

En enkel handleliste-app som lagrer alt lokalt i telefonen din (ingen server, ingen konto).

## Slik legger du den ut på GitHub Pages

1. Opprett et nytt repository på GitHub, f.eks. `handleliste`.
2. Last opp alle filene i denne mappen til repoet:
   - `index.html`
   - `manifest.json`
   - `sw.js`
   - `icons/icon-192.png`
   - `icons/icon-512.png`
3. Gå til **Settings → Pages** i repoet.
4. Under "Build and deployment", velg **Deploy from a branch**, branch `main`, mappe `/ (root)`. Lagre.
5. Etter ca ett minutt får du en lenke som:
   `https://dittbrukernavn.github.io/handleliste/`

## Slik legger du den til som ikon på telefonen

**iPhone (Safari):**
1. Åpne lenken over i Safari.
2. Trykk på del-ikonet (firkant med pil opp).
3. Velg "Legg til på Hjem-skjerm".

**Android (Chrome):**
1. Åpne lenken over i Chrome.
2. Trykk på de tre prikkene øverst til høyre.
3. Velg "Legg til på startskjermen" / "Installer app".

## Om lagring

Alt du legger inn (varer, kjøpsstatistikk og faste varer) lagres direkte i telefonens nettleser
(`localStorage`) og forsvinner ikke når du lukker appen. Det lagres kun lokalt på din enhet —
ingenting sendes til en server.

Merk: Hvis du sletter nettleserdata / cache for siden, eller bruker "privat nettlesing", vil
listen bli tom. Vanlig bruk (åpne, lukke, starte telefonen på nytt) påvirker ikke lagringen.
