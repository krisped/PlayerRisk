package com.krisped;

import net.runelite.api.Player;
import net.runelite.client.game.ItemManager;
import net.runelite.api.kit.KitType;

public class RiskCalculator {

    /**
     * Beregner total risiko for en spiller ved å summere prisen på hvert utstyr.
     * Bruker long for å unngå overflow ved høye summer.
     */
    public static long calculateRisk(Player player, ItemManager itemManager) {
        if (player.getPlayerComposition() == null) {
            return 0;
        }
        long total = 0;
        for (KitType kit : KitType.values()) {
            int itemId = player.getPlayerComposition().getEquipmentId(kit);
            if (itemId > 0) {
                total += itemManager.getItemPrice(itemId);
            }
        }
        return total;
    }

    /**
     * Returnerer risikokategori basert på total risikoverdi og terskler definert i config.
     */
    public static PlayerRiskOverlay.RiskCategory getRiskCategory(long riskValue, PlayerRiskConfig config) {
        int lowThreshold = Math.max(config.lowRiskGP(), 1000);
        if (riskValue > config.insaneRiskGP()) {
            return PlayerRiskOverlay.RiskCategory.INSANE;
        } else if (riskValue > config.highRiskGP()) {
            return PlayerRiskOverlay.RiskCategory.HIGH;
        } else if (riskValue > config.mediumRiskGP()) {
            return PlayerRiskOverlay.RiskCategory.MEDIUM;
        } else if (riskValue > lowThreshold) {
            return PlayerRiskOverlay.RiskCategory.LOW;
        }
        return PlayerRiskOverlay.RiskCategory.NONE;
    }
}
