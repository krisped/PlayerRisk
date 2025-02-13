package com.krisped;

import net.runelite.api.Client;
import net.runelite.api.Player;
import javax.inject.Inject;

public class CombatManager {
    private final Client client;
    private final long combatTimeoutMillis;
    private long lastCombatTime = 0;

    @Inject
    public CombatManager(Client client, PlayerRiskConfig config) {
        this.client = client;
        // Hent timeout fra config (i sekunder) og konverter til millisekunder
        this.combatTimeoutMillis = config.combatTimeout() * 1000L;
    }

    public boolean isInCombat() {
        Player local = client.getLocalPlayer();
        if (local == null) {
            return false;
        }
        // Spilleren er i kamp dersom den har en aktiv interaksjon
        if (local.getInteracting() != null) {
            lastCombatTime = System.currentTimeMillis();
            return true;
        }
        // Sjekk om tiden siden sist kamp er innenfor timeout
        long timeSinceCombat = System.currentTimeMillis() - lastCombatTime;
        return timeSinceCombat < combatTimeoutMillis;
    }
}
