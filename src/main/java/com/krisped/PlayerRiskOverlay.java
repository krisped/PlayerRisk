package com.krisped;

import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.kit.KitType;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import javax.inject.Inject;
import java.awt.*;

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
            Color highlightColor = getRiskColor(totalRisk);

            if (highlightColor != null)
            {
                modelOutlineRenderer.drawOutline(player, config.outlineThickness(), highlightColor, 0);
                drawRiskText(graphics, player, totalRisk);
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
        if (riskValue > config.insaneRiskThreshold()) return Color.PINK;
        else if (riskValue > config.highRiskThreshold()) return Color.ORANGE;
        else if (riskValue > config.mediumRiskThreshold()) return Color.GREEN;
        else if (riskValue > config.lowRiskThreshold()) return Color.BLUE;
        return null;
    }

    private void drawRiskText(Graphics2D graphics, Player player, int totalRisk)
    {
        String riskText = formatRiskValue(totalRisk);
        Point textLocation = getTextLocation(graphics, player, riskText);

        if (textLocation != null)
        {
            graphics.setColor(Color.WHITE);
            graphics.drawString(riskText, textLocation.getX(), textLocation.getY());
        }
    }

    private String formatRiskValue(int value)
    {
        if (value >= 1_000_000) return String.format("%.1fM", value / 1_000_000.0);
        else if (value >= 1_000) return String.format("%dK", value / 1_000);
        else return String.valueOf(value);
    }

    private Point getTextLocation(Graphics2D graphics, Player player, String riskText)
    {
        if (player == null || riskText == null)
        {
            return null;
        }

        int offsetY;
        switch (config.textPosition())
        {
            case ABOVE:
                offsetY = -40;
                break;
            case MIDDLE:
                offsetY = -20;
                break;
            case BELOW:
                offsetY = 0;
                break;
            default:
                offsetY = 0;
        }

        return player.getCanvasTextLocation(graphics, riskText, offsetY);
    }
}
