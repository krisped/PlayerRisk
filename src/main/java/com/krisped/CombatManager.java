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
        // Timeout fra config (sekunder) konverteres til millisekunder
        this.combatTimeoutMillis = config.combatTimeout() * 1000L;
    }

    public boolean isInCombat() {
        Player local = client.getLocalPlayer();
        if (local == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (local.getInteracting() != null) {
            lastCombatTime = now;
            return true;
        }
        return (now - lastCombatTime) < combatTimeoutMillis;
    }
}
