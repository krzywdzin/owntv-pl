# Activation backend

Minimalny backend zgodny z kontraktem aplikacji.

## Uruchomienie lokalne

\`\`\`bash
cd backend
cp activations.example.json activations.json
python3 activate.py
\`\`\`

Serwer startuje domyślnie na porcie \`8787\`. Przykładowy kod ma 8 znaków: \`DEMO2026\`.

Przykładowe konto wskazuje na publiczny Xtream mock używany wyłącznie do developmentu:
\`test_user / test_pass\`.

## Konfiguracja aplikacji

Emulator Androida może trafić do hosta przez \`10.0.2.2\`. Fizyczny Android TV musi dostać
adres komputera/serwera widoczny z jego sieci:

\`\`\`bash
./gradlew :app:assembleStandardDebug -Pproduct.activationBaseUrl=http://192.168.1.20:8787
\`\`\`

W release ustaw \`ACTIVATION_BASE_URL\` albo \`-Pproduct.activationBaseUrl=...\`.

## Kontrakt

\`GET /activate/{KOD}\` z nagłówkiem \`X-Device-ID\`.

- \`200\` — poprawny kod i dane Xtream,
- \`404\` — nieprawidłowy kod,
- \`409\` — kod jest już przypisany do innego urządzenia,
- \`410\` — kod wygasł,
- \`429\` — limit prób.

Pierwsze poprawne użycie kodu zapisuje powiązanie z urządzeniem w \`bindings.json\`. Ten sam kod
i ten sam \`X-Device-ID\` można odpytać ponownie po odnowieniu abonamentu — backend czyta aktualną
datę \`expires\` przy każdym żądaniu.

Pliki \`activations.json\` i \`bindings.json\` zawierają dane operacyjne i nie mogą trafiać do
publicznego repozytorium.
