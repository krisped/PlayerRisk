package com.krisped;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
        name = "[KP] Player Risk Highlighter",
        description = "Highlights players based on their risk",
        tags = {"pvp", "risk", "outline"}
)
public class PlayerRiskPlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private ItemManager itemManager;

    @Inject
    private MenuManager menuManager;

    @Inject
    private EventBus eventBus;

    @Inject
    private PlayerRiskConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private PlayerRiskOverlay playerRiskOverlay;

    @Inject
    private PlayerRiskMinimapOverlay playerRiskMinimapOverlay;

    @Inject
    private RiskSummaryOverlay riskSummaryOverlay;

    @Inject
    private PlayerRiskMenuEntry playerRiskMenuEntry;

    @Override
    protected void startUp() throws Exception {
        overlayManager.add(playerRiskOverlay);
        overlayManager.add(playerRiskMinimapOverlay);
        overlayManager.add(riskSummaryOverlay);

        eventBus.register(playerRiskMenuEntry);
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(playerRiskOverlay);
        overlayManager.remove(playerRiskMinimapOverlay);
        overlayManager.remove(riskSummaryOverlay);

        eventBus.unregister(playerRiskMenuEntry);
    }

    @Provides
    PlayerRiskConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(PlayerRiskConfig.class);
    }
}
