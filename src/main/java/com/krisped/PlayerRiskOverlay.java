package com.krisped;

import net.runelite.api.*;
import net.runelite.api.kit.KitType;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

public class PlayerRiskOverlay extends Overlay
{
    private final Client client;
    private final ItemManager itemManager;
    private final ModelOutlineRenderer modelOutlineRenderer;
    private final PlayerRiskConfig config;

    @Inject
    public PlayerRiskOverlay(Client client, ItemManager itemManager, ModelOutlineRenderer modelOutlineRenderer, PlayerRiskConfig config)
    {
        this.client = client;
        this.itemManager = itemManager;
        this.modelOutlineRenderer = modelOutlineRenderer;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.HIGH);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        for (Player player : client.getPlayers())
        {
            if (player == null || player.getName() == null)
            {
                continue;
            }

            int totalRisk = calculatePlayerRisk(player);
            RiskCategory category = getRiskCategory(totalRisk);
            if (category == RiskCategory.NONE)
            {
                continue;
            }
            if (!isCategoryEnabled(category))
            {
                continue;
            }
            Color riskColor = getRiskColor(totalRisk);
            if (riskColor == null)
            {
                continue;
            }
            // Draw both outline and text
            modelOutlineRenderer.drawOutline(player, config.outlineThickness(), riskColor, 0);
            drawRiskText(graphics, player, totalRisk, riskColor);
        }
        return null;
    }

    private int calculatePlayerRisk(Player player)
    {
        PlayerComposition composition = player.getPlayerComposition();
        if (composition == null)
        {
            return 0;
        }
        int totalValue = 0;
        for (KitType kitType : KitType.values())
        {
            int itemId = composition.getEquipmentId(kitType);
            if (itemId > 0)
            {
                totalValue += itemManager.getItemPrice(itemId);
            }
        }
        return totalValue;
    }

    private Color getRiskColor(int riskValue)
    {
        if (riskValue > config.insaneRiskGP()) return config.insaneRiskColor();
        else if (riskValue > config.highRiskGP()) return config.highRiskColor();
        else if (riskValue > config.mediumRiskGP()) return config.mediumRiskColor();
        else if (riskValue > config.lowRiskGP()) return config.lowRiskColor();
        return null;
    }

    private void drawRiskText(Graphics2D graphics, Player player, int totalRisk, Color riskColor)
    {
        String riskText = formatRiskValue(totalRisk);
        java.awt.Point textLocation = getTextLocation(graphics, player, riskText);
        if (textLocation != null)
        {
            graphics.setColor(riskColor);
            graphics.drawString(riskText, textLocation.x, textLocation.y);
        }
    }

    private String formatRiskValue(int value)
    {
        if (value >= 1_000_000) return String.format("%.1fM", value / 1_000_000.0);
        else if (value >= 1_000) return String.format("%dK", value / 1_000);
        else return String.valueOf(value);
    }

    // Bruk player.getCanvasTextLocation i stedet for convex hull
    private java.awt.Point getTextLocation(Graphics2D graphics, Player player, String riskText)
    {
        if (player == null || riskText == null)
        {
            return null;
        }
        if (config.textPosition() == PlayerRiskConfig.TextPosition.NONE)
        {
            return null;
        }
        int yOffset;
        switch (config.textPosition())
        {
            case ABOVE:
                yOffset = -20;  // juster etter behov
                break;
            case MIDDLE:
                yOffset = 0;
                break;
            case BELOW:
                yOffset = 20;   // juster etter behov
                break;
            default:
                yOffset = 0;
                break;
        }
        return player.getCanvasTextLocation(graphics, riskText, yOffset);
    }

    // Internal enum for risk categories.
    private enum RiskCategory
    {
        NONE,
        LOW,
        MEDIUM,
        HIGH,
        INSANE
    }

    private RiskCategory getRiskCategory(int riskValue)
    {
        if (riskValue > config.insaneRiskGP())
        {
            return RiskCategory.INSANE;
        }
        else if (riskValue > config.highRiskGP())
        {
            return RiskCategory.HIGH;
        }
        else if (riskValue > config.mediumRiskGP())
        {
            return RiskCategory.MEDIUM;
        }
        else if (riskValue > config.lowRiskGP())
        {
            return RiskCategory.LOW;
        }
        return RiskCategory.NONE;
    }

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
