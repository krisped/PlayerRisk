package com.krisped;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Varbits;
import net.runelite.api.WorldType;
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

public class PlayerRiskOverlay extends Overlay
{
    private final Client client;
    private final ItemManager itemManager;
    private final ModelOutlineRenderer modelOutlineRenderer;
    private final PlayerRiskConfig config;
    private final CombatManager combatManager;
    private boolean enabled = true;

    @Inject
    public PlayerRiskOverlay(Client client, ItemManager itemManager, ModelOutlineRenderer modelOutlineRenderer,
                             PlayerRiskConfig config, CombatManager combatManager)
    {
        this.client = client;
        this.itemManager = itemManager;
        this.modelOutlineRenderer = modelOutlineRenderer;
        this.config = config;
        this.combatManager = combatManager;
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.HIGH);
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        // Hvis plugin er "av" pga hotkey, eller overlay globalt avslått
        if (!enabled || !PlayerRiskPlugin.isHotkeyActive(client, config))
        {
            return null;
        }

        if (config.disableHighlightInCombat() && combatManager.isInCombat())
        {
            return null;
        }

        for (Player player : client.getPlayers())
        {
            if (player == null || player.getName() == null)
                continue;

            if (player.equals(client.getLocalPlayer()) && !config.highlightLocalPlayer())
                continue;

            // PvP-filtrering
            PlayerRiskConfig.PvPMode pvpMode = config.pvpMode();
            if (pvpMode != PlayerRiskConfig.PvPMode.DISABLED)
            {
                boolean inPvPWorld = client.getWorldType().contains(WorldType.PVP);
                boolean inWilderness = client.getVar(Varbits.IN_WILDERNESS) > 0;
                if (!inPvPWorld && !inWilderness)
                    continue;
                if (pvpMode == PlayerRiskConfig.PvPMode.ATTACKABLE)
                {
                    Player local = client.getLocalPlayer();
                    if (local != null)
                    {
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

            // Skull Mode-filtrering: Bruk kun dersom PvP Mode != DISABLED
            if (config.pvpMode() != PlayerRiskConfig.PvPMode.DISABLED)
            {
                PlayerRiskConfig.SkullMode skullMode = config.skullMode();
                boolean isSkulled = player.getSkullIcon() != -1;
                if (skullMode == PlayerRiskConfig.SkullMode.UNSKULLED && isSkulled)
                    continue;
                else if (skullMode == PlayerRiskConfig.SkullMode.SKULLED && !isSkulled)
                    continue;
            }

            long totalRisk = RiskCalculator.calculateRisk(player, itemManager);
            RiskCategory category = RiskCalculator.getRiskCategory(totalRisk, config);
            if (category == RiskCategory.NONE || !isCategoryEnabled(category))
                continue;

            Color riskColor = getRiskColor(totalRisk);
            if (riskColor == null)
                continue;

            // Tegn outline hvis aktivert
            if (config.enableOutline())
            {
                modelOutlineRenderer.drawOutline(player, config.outlineThickness(), riskColor, 0);
            }

            // Tegn tile
            if (config.enableTile())
            {
                Polygon tilePoly = player.getCanvasTilePoly();
                if (tilePoly != null)
                {
                    graphics.setColor(riskColor);
                    graphics.drawPolygon(tilePoly);
                }
            }

            // Tegn hull
            if (config.enableHull())
            {
                Shape hullShape = player.getConvexHull();
                if (hullShape != null)
                {
                    graphics.setColor(riskColor);
                    graphics.draw(hullShape);
                }
            }

            // Tegn risikotekst over/under/center
            if (config.riskText() != PlayerRiskConfig.TextPosition.DISABLED)
            {
                drawRiskText(graphics, player, totalRisk, riskColor);
            }

            // Tegn defence-nivå-tekst dersom aktivert
            if (config.defenceText() != PlayerRiskConfig.DefenceTextPosition.DISABLED)
            {
                drawDefenceText(graphics, player);
            }
        }
        return null;
    }

    private Color getRiskColor(long riskValue)
    {
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

    private void drawRiskText(Graphics2D graphics, Player player, long totalRisk, Color riskColor)
    {
        String riskText = formatRiskValue(totalRisk);
        if (config.showPlayernames())
        {
            riskText = player.getName() + ": " + riskText;
        }
        FontMetrics metrics = graphics.getFontMetrics();

        Shape convexHull = player.getConvexHull();
        int baseline;
        int centerX;

        if (convexHull != null)
        {
            Rectangle bounds = convexHull.getBounds();
            int headY = bounds.y;
            int feetY = bounds.y + bounds.height;
            centerX = bounds.x + bounds.width / 2;
            int centerY = (headY + feetY) / 2;

            switch (config.riskText())
            {
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
        }
        else
        {
            Point canvasText = player.getCanvasTextLocation(graphics, riskText, player.getLogicalHeight() / 2);
            if (canvasText == null) return;
            centerX = canvasText.getX();
            baseline = canvasText.getY();
        }

        int textWidth = metrics.stringWidth(riskText);
        int drawX = centerX - textWidth / 2;

        graphics.setColor(riskColor);
        graphics.drawString(riskText, drawX, baseline);
    }

    private void drawDefenceText(Graphics2D graphics, Player player)
    {
        // Hent defence-nivå fra DefenceHiscoreManager (kun last én gang per spiller)
        Integer defenceLevel = DefenceHiscoreManager.getDefenceLevel(player.getName());
        if (defenceLevel == null)
        {
            // Start henting asynkront og vis ingenting denne runden
            DefenceHiscoreManager.fetchDefenceLevel(player.getName());
            return;
        }
        String defText = defenceLevel + " Def.";
        FontMetrics metrics = graphics.getFontMetrics();
        int baseline;
        int centerX;
        Shape convexHull = player.getConvexHull();
        if (convexHull != null)
        {
            Rectangle bounds = convexHull.getBounds();
            int headY = bounds.y;
            int feetY = bounds.y + bounds.height;
            centerX = bounds.x + bounds.width / 2;
            int centerY = (headY + feetY) / 2;
            switch (config.defenceText())
            {
                case OVER:
                    // Juster med en offset basert på tekstens høyde slik at den ikke overlapper playername og risk
                    baseline = headY - 2 + metrics.getAscent() + metrics.getHeight() + 2;
                    break;
                case UNDER:
                    baseline = feetY + 2 + metrics.getAscent();
                    break;
                case CENTRE:
                default:
                    baseline = centerY + (metrics.getAscent() - metrics.getDescent()) / 2;
                    break;
            }
        }
        else
        {
            Point canvasText = player.getCanvasTextLocation(graphics, defText, player.getLogicalHeight() / 2);
            if (canvasText == null)
                return;
            centerX = canvasText.getX();
            baseline = canvasText.getY();
        }
        int textWidth = metrics.stringWidth(defText);
        int drawX = centerX - textWidth / 2;
        graphics.setColor(Color.WHITE);
        graphics.drawString(defText, drawX, baseline);
    }

    private String formatRiskValue(long value)
    {
        if (value >= 1_000_000)
            return String.format("%.1fM", value / 1_000_000.0);
        else if (value >= 1_000)
            return String.format("%dK", value / 1_000);
        else
            return String.valueOf(value);
    }

    // Definerer vårt interne RiskCategory for overlayet
    enum RiskCategory
    {
        NONE,
        LOW,
        MEDIUM,
        HIGH,
        INSANE
    }

    // Bruker vårt interne RiskCategory for å sjekke om kategorien er aktivert
    private boolean isCategoryEnabled(RiskCategory category)
    {
        switch (category)
        {
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
