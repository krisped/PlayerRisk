package com.krisped;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.EnumMap;
import java.util.Map;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import net.runelite.api.WorldType;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.ui.overlay.components.LineComponent;
import javax.inject.Inject;

public class RiskSummaryOverlay extends OverlayPanel {
    private final Client client;
    private final ItemManager itemManager;
    private final PlayerRiskConfig config;
    private final CombatManager combatManager;
    private static final int DISTANCE_THRESHOLD = 50;

    @Inject
    public RiskSummaryOverlay(Client client, ItemManager itemManager, PlayerRiskConfig config, CombatManager combatManager) {
        this.client = client;
        this.itemManager = itemManager;
        this.config = config;
        this.combatManager = combatManager;
        setPosition(OverlayPosition.TOP_LEFT);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!config.showOverlay() ||
                config.overlayDisplayType() == PlayerRiskConfig.OverlayDisplayType.DISABLED ||
                client.getLocalPlayer() == null)
        {
            return null;
        }
        if (config.disableHighlightInCombat() && combatManager.isInCombat()) {
            return null;
        }

        panelComponent.getChildren().clear();

        // Overskrift for Risk Summary
        TitleComponent header = TitleComponent.builder()
                .text("Risk Summary")
                .build();
        panelComponent.getChildren().add(header);

        // Vis PvP Mode-linje
        String pvpStatus;
        switch(config.pvpMode()) {
            case OFF:
                pvpStatus = "Off";
                break;
            case ON:
                pvpStatus = "On";
                break;
            case ATTACKABLE:
                pvpStatus = "Attackable";
                break;
            default:
                pvpStatus = "Unknown";
                break;
        }
        LineComponent pvpLine = LineComponent.builder()
                .left("PvP Mode:")
                .right(pvpStatus)
                .build();
        panelComponent.getChildren().add(pvpLine);

        // Hvis i PvP-modus og ikke i et PvP-område, vis advarsel
        if (config.pvpMode() == PlayerRiskConfig.PvPMode.ON ||
                config.pvpMode() == PlayerRiskConfig.PvPMode.ATTACKABLE) {
            boolean inPvPWorld = client.getWorldType().contains(WorldType.PVP);
            boolean inWilderness = client.getVar(Varbits.IN_WILDERNESS) > 0;
            if (!inPvPWorld && !inWilderness) {
                LineComponent warning = LineComponent.builder()
                        .left("<col=FF0000>Not in PvP world")
                        .build();
                panelComponent.getChildren().add(warning);
                return super.render(graphics);
            }
        }

        // Overskrift for risikotabellen
        LineComponent tableHeader = LineComponent.builder()
                .left("Risk:")
                .right("Players:")
                .build();
        panelComponent.getChildren().add(tableHeader);

        Map<PlayerRiskOverlay.RiskCategory, Integer> counts = calculateRiskCounts();
        PlayerRiskConfig.OverlayDisplayType displayType = config.overlayDisplayType();

        // Low Risk
        if (config.enableLowRisk()) {
            String colorHex = toHex(config.lowRiskColor());
            int effectiveLow = config.lowRiskGP();
            String leftLabel = (displayType == PlayerRiskConfig.OverlayDisplayType.RISK_CATEGORIES)
                    ? "<col=" + colorHex + ">Low"
                    : "<col=" + colorHex + ">" + formatRiskValue(effectiveLow);
            int count = counts.get(PlayerRiskOverlay.RiskCategory.LOW);
            LineComponent line = LineComponent.builder()
                    .left(leftLabel)
                    .right("<col=" + colorHex + ">" + count)
                    .build();
            panelComponent.getChildren().add(line);
        }

        // Medium Risk
        if (config.enableMediumRisk()) {
            String colorHex = toHex(config.mediumRiskColor());
            String leftLabel = (displayType == PlayerRiskConfig.OverlayDisplayType.RISK_CATEGORIES)
                    ? "<col=" + colorHex + ">Medium"
                    : "<col=" + colorHex + ">" + formatRiskValue(config.mediumRiskGP());
            int count = counts.get(PlayerRiskOverlay.RiskCategory.MEDIUM);
            LineComponent line = LineComponent.builder()
                    .left(leftLabel)
                    .right("<col=" + colorHex + ">" + count)
                    .build();
            panelComponent.getChildren().add(line);
        }

        // High Risk
        if (config.enableHighRisk()) {
            String colorHex = toHex(config.highRiskColor());
            String leftLabel = (displayType == PlayerRiskConfig.OverlayDisplayType.RISK_CATEGORIES)
                    ? "<col=" + colorHex + ">High"
                    : "<col=" + colorHex + ">" + formatRiskValue(config.highRiskGP());
            int count = counts.get(PlayerRiskOverlay.RiskCategory.HIGH);
            LineComponent line = LineComponent.builder()
                    .left(leftLabel)
                    .right("<col=" + colorHex + ">" + count)
                    .build();
            panelComponent.getChildren().add(line);
        }

        // Insane Risk
        if (config.enableInsaneRisk()) {
            String colorHex = toHex(config.insaneRiskColor());
            String leftLabel = (displayType == PlayerRiskConfig.OverlayDisplayType.RISK_CATEGORIES)
                    ? "<col=" + colorHex + ">Insane"
                    : "<col=" + colorHex + ">" + formatRiskValue(config.insaneRiskGP());
            int count = counts.get(PlayerRiskOverlay.RiskCategory.INSANE);
            LineComponent line = LineComponent.builder()
                    .left(leftLabel)
                    .right("<col=" + colorHex + ">" + count)
                    .build();
            panelComponent.getChildren().add(line);
        }

        return super.render(graphics);
    }

    private Map<PlayerRiskOverlay.RiskCategory, Integer> calculateRiskCounts() {
        Map<PlayerRiskOverlay.RiskCategory, Integer> counts = new EnumMap<>(PlayerRiskOverlay.RiskCategory.class);
        for (PlayerRiskOverlay.RiskCategory cat : PlayerRiskOverlay.RiskCategory.values()) {
            counts.put(cat, 0);
        }
        WorldPoint localLocation = client.getLocalPlayer().getWorldLocation();
        PlayerRiskConfig.PvPMode pvpMode = config.pvpMode();
        for (Player player : client.getPlayers()) {
            if (player == null || player.getName() == null)
                continue;
            // Ekskluder local player med mindre enabled i config
            if (player.equals(client.getLocalPlayer()) && !config.highlightLocalPlayer())
                continue;
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
            WorldPoint playerLocation = player.getWorldLocation();
            int dx = Math.abs(playerLocation.getX() - localLocation.getX());
            int dy = Math.abs(playerLocation.getY() - localLocation.getY());
            int distance = Math.max(dx, dy);
            if (distance > DISTANCE_THRESHOLD)
                continue;
            int risk = RiskCalculator.calculateRisk(player, itemManager);
            PlayerRiskOverlay.RiskCategory category = RiskCalculator.getRiskCategory(risk, config);
            if (category != PlayerRiskOverlay.RiskCategory.NONE)
                counts.put(category, counts.get(category) + 1);
        }
        return counts;
    }

    private String formatRiskValue(int value) {
        if (value >= 1_000_000)
            return String.format("%.1fM", value / 1_000_000.0);
        else if (value >= 1_000)
            return String.format("%dK", value / 1_000);
        else
            return String.valueOf(value);
    }

    private String toHex(Color color) {
        return String.format("%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }
}
