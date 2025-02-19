package com.krisped;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.util.ImageUtil;

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

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private ClientThread clientThread; // For oppdatering av risk-panelet

    // Opprett RiskPanel-instans og lagre i statiske variabler
    private static RiskPanel riskPanel;
    private static NavigationButton riskNavigationButton;
    private static ClientToolbar staticClientToolbar;

    @Override
    protected void startUp() throws Exception {
        overlayManager.add(playerRiskOverlay);
        overlayManager.add(playerRiskMinimapOverlay);
        overlayManager.add(riskSummaryOverlay);

        eventBus.register(playerRiskMenuEntry);

        // Opprett RiskPanel med Client, ClientThread og ItemManager
        riskPanel = new RiskPanel(client, clientThread, itemManager);
        BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/Coins_10000.png");
        riskNavigationButton = NavigationButton.builder()
                .tooltip("Risk Panel")
                .icon(icon)
                .priority(1)
                .panel(riskPanel)
                .build();
        clientToolbar.addNavigation(riskNavigationButton);
        // Lagre en statisk referanse til clientToolbar for bruk i PlayerRiskMenuEntry
        staticClientToolbar = clientToolbar;
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(playerRiskOverlay);
        overlayManager.remove(playerRiskMinimapOverlay);
        overlayManager.remove(riskSummaryOverlay);

        eventBus.unregister(playerRiskMenuEntry);

        if (riskNavigationButton != null) {
            clientToolbar.removeNavigation(riskNavigationButton);
        }
    }

    @Provides
    PlayerRiskConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(PlayerRiskConfig.class);
    }

    // Statisk getter for RiskPanel-instansen
    public static RiskPanel getRiskPanel() {
        return riskPanel;
    }

    // Statisk getter for navigasjonsknappen
    public static NavigationButton getRiskNavigationButton() {
        return riskNavigationButton;
    }

    // Statisk getter for clientToolbar (for bruk i PlayerRiskMenuEntry)
    public static ClientToolbar getClientToolbar() {
        return staticClientToolbar;
    }
}
