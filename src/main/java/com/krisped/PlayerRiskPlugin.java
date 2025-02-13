package com.krisped;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
        name = "[KP] Player Risk Highlight",
        description = "Highlights players based on their risk",
        tags = {"pvp", "risk", "outline"}
)
public class PlayerRiskPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private PlayerRiskOverlay playerRiskOverlay;

    @Inject
    private PlayerRiskMinimapOverlay playerRiskMinimapOverlay;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private PlayerRiskConfig config;

    @Inject
    private ItemManager itemManager;

    private RiskSummaryOverlay riskSummaryOverlay;

    @Override
    protected void startUp() throws Exception
    {
        log.info("Player Risk Plugin started!");
        playerRiskOverlay.setEnabled(true);
        playerRiskMinimapOverlay.setEnabled(true);
        overlayManager.add(playerRiskOverlay);
        overlayManager.add(playerRiskMinimapOverlay);

        riskSummaryOverlay = new RiskSummaryOverlay(client, itemManager, config);
        overlayManager.add(riskSummaryOverlay);
    }

    @Override
    protected void shutDown() throws Exception
    {
        log.info("Player Risk Plugin stopped!");
        overlayManager.remove(playerRiskOverlay);
        overlayManager.remove(playerRiskMinimapOverlay);
        overlayManager.remove(riskSummaryOverlay);
        overlayManager.removeIf(o -> o instanceof PlayerRiskOverlay || o instanceof PlayerRiskMinimapOverlay);
        playerRiskOverlay.setEnabled(false);
        playerRiskMinimapOverlay.setEnabled(false);
    }

    @Provides
    PlayerRiskConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(PlayerRiskConfig.class);
    }
}
