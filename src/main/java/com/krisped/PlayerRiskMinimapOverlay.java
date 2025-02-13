package com.krisped;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Varbits;
import net.runelite.api.WorldType;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;
import javax.inject.Inject;

public class PlayerRiskMinimapOverlay extends Overlay {

    private final Client client;
    private final ItemManager itemManager;
    private final PlayerRiskConfig config;
    private final CombatManager combatManager;
    private boolean enabled = true;

    @Inject
    public PlayerRiskMinimapOverlay(Client client, ItemManager itemManager, PlayerRiskConfig config, CombatManager combatManager) {
        this.client = client;
        this.itemManager = itemManager;
        this.config = config;
        this.combatManager = combatManager;
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        // Bruker DYNAMIC ettersom det ikke finnes en spesifikk MINIMAP-posisjon
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.HIGH);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!enabled || client.getLocalPlayer() == null) {
            return null;
        }
        if (config.disableHighlightInCombat() && combatManager.isInCombat()) {
            return null;
        }

        Set<Point> usedMinimapLocations = new HashSet<>();
        for (Player player : client.getPlayers()) {
            if (player == null || player.getName() == null) {
                continue;
            }
            // PvP-filtrering
            PlayerRiskConfig.PvPMode pvpMode = config.pvpMode();
            if (pvpMode != PlayerRiskConfig.PvPMode.OFF) {
                boolean inPvPWorld = client.getWorldType().contains(WorldType.PVP);
                boolean inWilderness = client.getVar(Varbits.IN_WILDERNESS) > 0;
                if (!inPvPWorld && !inWilderness)
                    continue;
                if (pvpMode == PlayerRiskConfig.PvPMode.ATTACKABLE) {
                    Player local = client.getLocalPlayer();
                    if (local != null) {
                        int localCombat = local.getCombatLevel();
                        int targetCombat = player.getCombatLevel();
                        int allowedDiff = 15;
                        if (inWilderness)
                            allowedDiff += client.getVar(Varbits.IN_WILDERNESS);
                        if (Math.abs(localCombat - targetCombat) > allowedDiff)
                            continue;
                    }
                }
            }

            // Bruker RiskCalculator for å beregne risiko og bestemme kategori
            int totalRisk = RiskCalculator.calculateRisk(player, itemManager);
            PlayerRiskOverlay.RiskCategory category = RiskCalculator.getRiskCategory(totalRisk, config);
            if (category == PlayerRiskOverlay.RiskCategory.NONE || !isCategoryEnabled(category))
                continue;
            Color riskColor = getRiskColor(totalRisk);
            if (riskColor == null)
                continue;

            Point minimapLoc = player.getMinimapLocation();
            if (minimapLoc == null || usedMinimapLocations.contains(minimapLoc))
                continue;
            usedMinimapLocations.add(minimapLoc);

            switch (config.minimapDisplayMode()) {
                case DOT:
                    int size = 4;
                    graphics.setColor(riskColor);
                    graphics.fillOval(minimapLoc.getX() - size / 2, minimapLoc.getY() - size / 2, size, size);
                    break;
                case RISK:
                    String riskText = formatRiskValue(totalRisk);
                    OverlayUtil.renderTextLocation(graphics, minimapLoc, riskText, riskColor);
                    break;
                default:
                    break;
            }
        }
        return null;
    }

    private boolean isCategoryEnabled(PlayerRiskOverlay.RiskCategory category) {
        switch (category) {
            case LOW:
                return config.enableLowRisk();
            case MEDIUM:
                return config.enableMediumRisk();
            case HIGH:
                return config.enableHighRisk();
            case INSANE:
                return config.enableInsaneRisk();
            default:
                return false;
        }
    }

    private Color getRiskColor(int riskValue) {
        if (riskValue > config.insaneRiskGP())
            return config.insaneRiskColor();
        else if (riskValue > config.highRiskGP())
            return config.highRiskColor();
        else if (riskValue > config.mediumRiskGP())
            return config.mediumRiskColor();
        else if (riskValue > config.lowRiskGP())
            return config.lowRiskColor();
        return null;
    }

    private String formatRiskValue(int value) {
        if (value >= 1_000_000)
            return String.format("%.1fM", value / 1_000_000.0);
        else if (value >= 1_000)
            return String.format("%dK", value / 1_000);
        else
            return String.valueOf(value);
    }
}
