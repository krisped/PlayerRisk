package com.krisped;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
        name = "Player Risk Highlighter",
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
    private OverlayManager overlayManager;

    @Inject
    private PlayerRiskConfig config;

    @Override
    protected void startUp() throws Exception
    {
        log.info("Player Risk Plugin started!");
        overlayManager.add(playerRiskOverlay);
    }

    @Override
    protected void shutDown() throws Exception
    {
        log.info("Player Risk Plugin stopped!");
        overlayManager.remove(playerRiskOverlay);
    }

    @Provides
    PlayerRiskConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(PlayerRiskConfig.class);
    }
}
