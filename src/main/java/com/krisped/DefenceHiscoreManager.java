package com.krisped;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DefenceHiscoreManager {
    private static final Map<String, Integer> defenceCache = new ConcurrentHashMap<>();
    private static final Map<String, Long> failureCache = new ConcurrentHashMap<>();
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    // Vent 60 sekunder før en ny henting ved feil
    private static final long RETRY_DELAY_MS = 60_000;

    public static Integer getDefenceLevel(String playerName) {
        return defenceCache.get(playerName);
    }

    public static void fetchDefenceLevel(String playerName) {
        if (playerName == null || playerName.isEmpty())
            return;
        // Hvis vi allerede har en verdi, gjør ingenting
        if (defenceCache.containsKey(playerName))
            return;
        // Hvis vi nylig har hatt en feil, og ikke har ventet lenge nok, gjør heller ikke et nytt kall
        Long lastFailure = failureCache.get(playerName);
        if (lastFailure != null && (System.currentTimeMillis() - lastFailure < RETRY_DELAY_MS)) {
            return;
        }

        executor.submit(() -> {
            try {
                String encodedName = URLEncoder.encode(playerName, StandardCharsets.UTF_8.toString());
                URL url = new URL("https://services.runescape.com/m=hiscore_oldschool/index_lite.ws?player=" + encodedName);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                        String line;
                        int lineCount = 0;
                        Integer defenceLevel = null;
                        // Tredje linje tilsvarer defence (etter Overall og Attack)
                        while ((line = reader.readLine()) != null) {
                            lineCount++;
                            if (lineCount == 3) {
                                String[] parts = line.split(",");
                                if (parts.length >= 2) {
                                    defenceLevel = Integer.parseInt(parts[1]);
                                    // Dersom verdien er 100 eller høyere, sett maks til 99
                                    if (defenceLevel >= 100) {
                                        defenceLevel = 99;
                                    }
                                }
                                break;
                            }
                        }
                        if (defenceLevel != null) {
                            defenceCache.put(playerName, defenceLevel);
                            failureCache.remove(playerName);
                        }
                    }
                } else {
                    // Ikke 200-respons – registrer feil
                    failureCache.put(playerName, System.currentTimeMillis());
                }
            } catch (SocketTimeoutException ste) {
                // Ved timeout, registrer tidspunktet for feilen slik at vi prøver igjen etter en stund
                failureCache.put(playerName, System.currentTimeMillis());
            } catch (Exception ex) {
                failureCache.put(playerName, System.currentTimeMillis());
            }
        });
    }
}
