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

    // Variabel for å styre om overlayen skal rendere
    private boolean enabled = true;

    @Inject
    public PlayerRiskOverlay(Client client, ItemManager itemManager, ModelOutlineRenderer modelOutlineRenderer, PlayerRiskConfig config) {
        this.client = client;
        this.itemManager = itemManager;
        this.modelOutlineRenderer = modelOutlineRenderer;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.HIGH);
    }

    // Metode for å skru rendering av og på
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!enabled) {
            return null; // Stopp rendering hvis deaktivert
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

            int totalRisk = calculatePlayerRisk(player);
            RiskCategory category = getRiskCategory(totalRisk);
            if (category == RiskCategory.NONE)
                continue;
            if (!isCategoryEnabled(category))
                continue;
            Color riskColor = getRiskColor(totalRisk);
            if (riskColor == null)
                continue;

            // Tegn outline
            if (config.enableOutline()) {
                modelOutlineRenderer.drawOutline(player, config.outlineThickness(), riskColor, 0);
            }

            // Tegn tile-overlay (hvis aktiv)
            if (config.enableTile()) {
                Polygon tilePoly = player.getCanvasTilePoly();
                if (tilePoly != null) {
                    graphics.setColor(riskColor);
                    graphics.drawPolygon(tilePoly);
                }
            }

            // Tegn hull-overlay (hvis aktiv)
            if (config.enableHull()) {
                Shape hullShape = player.getConvexHull();
                if (hullShape != null) {
                    graphics.setColor(riskColor);
                    graphics.draw(hullShape);
                }
            }

            // Tegn risikotekst med posisjonering basert på modellens polygon
            if (config.textPosition() != PlayerRiskConfig.TextPosition.DISABLED) {
                drawRiskText(graphics, player, totalRisk, riskColor);
            }
        }
        return null;
    }

    private int calculatePlayerRisk(Player player) {
        PlayerComposition comp = player.getPlayerComposition();
        if (comp == null)
            return 0;
        int total = 0;
        for (KitType kit : KitType.values()) {
            int itemId = comp.getEquipmentId(kit);
            if (itemId > 0)
                total += itemManager.getItemPrice(itemId);
        }
        return total;
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
            // Forsøk å hente minY og maxY direkte hvis convexHull er et Polygon
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
            // Bruk hullens bounding box for horisontal posisjonering
            Rectangle hullBounds = convexHull.getBounds();
            centerX = hullBounds.x + hullBounds.width / 2;
            int centerY = (headY + feetY) / 2;

            // Posisjoner basert på valgt modus
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
            // Fall tilbake: bruk canvas-tekstposisjonering
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

    // RiskCategory er package-private slik at minimap-overlayet også kan bruke den
    enum RiskCategory {
        NONE,
        LOW,
        MEDIUM,
        HIGH,
        INSANE
    }

    RiskCategory getRiskCategory(int riskValue) {
        if (riskValue > config.insaneRiskGP())
            return RiskCategory.INSANE;
        else if (riskValue > config.highRiskGP())
            return RiskCategory.HIGH;
        else if (riskValue > config.mediumRiskGP())
            return RiskCategory.MEDIUM;
        else if (riskValue > config.lowRiskGP())
            return RiskCategory.LOW;
        return RiskCategory.NONE;
    }

    boolean isCategoryEnabled(RiskCategory category) {
        if (!config.enableLowRisk() && !config.enableMediumRisk() && !config.enableHighRisk() && !config.enableInsaneRisk()) {
            return false; // Deaktiver alt hvis ingen risiko er slått på
        }
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
