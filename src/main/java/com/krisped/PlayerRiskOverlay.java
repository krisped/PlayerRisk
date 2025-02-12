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
import java.awt.Rectangle;
import java.awt.Shape;

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
            Color riskColor = getRiskColor(totalRisk);

            if (riskColor != null)
            {
                modelOutlineRenderer.drawOutline(player, config.outlineThickness(), riskColor, 0);
                drawRiskText(graphics, player, totalRisk, riskColor);
            }
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
        if (riskValue > config.insaneValueRisk()) return config.insaneValueColor();
        else if (riskValue > config.highValueRisk()) return config.highValueColor();
        else if (riskValue > config.mediumValueRisk()) return config.mediumValueColor();
        else if (riskValue > config.lowValueRisk()) return config.lowValueColor();
        return null;
    }

    private void drawRiskText(Graphics2D graphics, Player player, int totalRisk, Color riskColor)
    {
        String riskText = formatRiskValue(totalRisk);
        // Bruker java.awt.Point for å unngå konflikt med net.runelite.api.Point
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

        // Bruk spillerens convex hull for en nøyaktigere treffflate
        Shape hull = player.getConvexHull();
        if (hull == null)
        {
            return null;
        }
        Rectangle bounds = hull.getBounds();
        int centerX = bounds.x + bounds.width / 2;
        int centerY = bounds.y + bounds.height / 2;
        int y;
        switch (config.textPosition())
        {
            case ABOVE:
                y = bounds.y - 5; // litt over hullens topp
                break;
            case MIDDLE:
                y = centerY;
                break;
            case BELOW:
                y = bounds.y + bounds.height + 5; // litt under hullens bunn
                break;
            default:
                y = centerY;
                break;
        }
        int textWidth = graphics.getFontMetrics().stringWidth(riskText);
        int x = centerX - textWidth / 2;
        return new java.awt.Point(x, y);
    }
}
