package com.krisped;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.PlayerComposition;
import net.runelite.api.Varbits;
import net.runelite.api.WorldType;
import net.runelite.api.kit.KitType;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;
import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;

public class PlayerRiskOverlay extends Overlay {

    private final Client client;
    private final ItemManager itemManager;
    private final ModelOutlineRenderer modelOutlineRenderer;
    private final PlayerRiskConfig config;
    private final CombatManager combatManager;
    private boolean enabled = true;

    @Inject
    public PlayerRiskOverlay(Client client, ItemManager itemManager, ModelOutlineRenderer modelOutlineRenderer, PlayerRiskConfig config, CombatManager combatManager) {
        this.client = client;
        this.itemManager = itemManager;
        this.modelOutlineRenderer = modelOutlineRenderer;
        this.config = config;
        this.combatManager = combatManager;
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.HIGH);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!enabled || config.disableHighlightInCombat() && combatManager.isInCombat()) {
            return null;
        }

        for (Player player : client.getPlayers()) {
            if (player == null || player.getName() == null)
                continue;

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

            // Bruker RiskCalculator for risikoberegning
            int totalRisk = RiskCalculator.calculateRisk(player, itemManager);
            RiskCategory category = RiskCalculator.getRiskCategory(totalRisk, config);
            if (category == RiskCategory.NONE || !isCategoryEnabled(category))
                continue;
            Color riskColor = getRiskColor(totalRisk);
            if (riskColor == null)
                continue;

            // Tegn outline
            if (config.enableOutline()) {
                modelOutlineRenderer.drawOutline(player, config.outlineThickness(), riskColor, 0);
            }

            // Tegn tile-overlay
            if (config.enableTile()) {
                Polygon tilePoly = player.getCanvasTilePoly();
                if (tilePoly != null) {
                    graphics.setColor(riskColor);
                    graphics.drawPolygon(tilePoly);
                }
            }

            // Tegn hull-overlay
            if (config.enableHull()) {
                Shape hullShape = player.getConvexHull();
                if (hullShape != null) {
                    graphics.setColor(riskColor);
                    graphics.draw(hullShape);
                }
            }

            // Tegn risikotekst
            if (config.textPosition() != PlayerRiskConfig.TextPosition.DISABLED) {
                drawRiskText(graphics, player, totalRisk, riskColor);
            }
        }
        return null;
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

    private void drawRiskText(Graphics2D graphics, Player player, int totalRisk, Color riskColor) {
        String riskText = formatRiskValue(totalRisk);
        FontMetrics metrics = graphics.getFontMetrics();
        int textWidth = metrics.stringWidth(riskText);
        int baseline = 0;
        int centerX = 0;
        int headY = 0;
        int feetY = 0;

        Shape convexHull = player.getConvexHull();
        if (convexHull != null) {
            if (convexHull instanceof Polygon) {
                Polygon poly = (Polygon) convexHull;
                int minY = Integer.MAX_VALUE;
                int maxY = Integer.MIN_VALUE;
                for (int i = 0; i < poly.npoints; i++) {
                    minY = Math.min(minY, poly.ypoints[i]);
                    maxY = Math.max(maxY, poly.ypoints[i]);
                }
                headY = minY;
                feetY = maxY;
            } else {
                Rectangle bounds = convexHull.getBounds();
                headY = bounds.y;
                feetY = bounds.y + bounds.height;
            }
            Rectangle hullBounds = convexHull.getBounds();
            centerX = hullBounds.x + hullBounds.width / 2;
            int centerY = (headY + feetY) / 2;

            switch (config.textPosition()) {
                case OVER:
                    baseline = headY - 2 + metrics.getAscent();
                    break;
                case UNDER:
                    baseline = feetY + 2 + metrics.getAscent();
                    break;
                case CENTER:
                default:
                    baseline = centerY + (metrics.getAscent() - metrics.getDescent()) / 2;
                    break;
            }
        } else {
            net.runelite.api.Point canvasText = player.getCanvasTextLocation(graphics, riskText, player.getLogicalHeight() / 2);
            if (canvasText == null)
                return;
            centerX = canvasText.getX();
            baseline = canvasText.getY();
        }
        int drawX = centerX - textWidth / 2;
        graphics.setColor(riskColor);
        graphics.drawString(riskText, drawX, baseline);
    }

    private String formatRiskValue(int value) {
        if (value >= 1_000_000)
            return String.format("%.1fM", value / 1_000_000.0);
        else if (value >= 1_000)
            return String.format("%dK", value / 1_000);
        else
            return String.valueOf(value);
    }

    // Risiko-kategorier slik at de kan brukes i RiskCalculator
    enum RiskCategory {
        NONE,
        LOW,
        MEDIUM,
        HIGH,
        INSANE
    }

    private boolean isCategoryEnabled(RiskCategory category) {
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
}
