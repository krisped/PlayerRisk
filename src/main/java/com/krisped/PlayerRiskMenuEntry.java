package com.krisped;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.KeyCode;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.menus.MenuManager;

import javax.inject.Inject;
import java.awt.Color;
import java.text.NumberFormat;
import java.util.Locale;

public class PlayerRiskMenuEntry {

    public static final String INSPECT_RISK = "Risk Check";

    @Inject
    private Client client;

    @Inject
    private ItemManager itemManager;

    @Inject
    private MenuManager menuManager;

    @Inject
    private PlayerRiskConfig config;

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event) {
        // Sjekk at menyvalget gjelder en spiller (type RUNELITE_PLAYER)
        if (event.getType() != MenuAction.RUNELITE_PLAYER.getId()) {
            return;
        }

        // Hent spillerobjektet fra cache basert på event.getIdentifier()
        Player player = client.getCachedPlayers()[event.getIdentifier()];
        if (player == null) {
            return;
        }

        // Hvis spilleren er local player og config ikke tillater at du inkluderer deg selv, avslutt
        if (player.equals(client.getLocalPlayer()) && !config.highlightLocalPlayer()) {
            return;
        }

        // Hvis Risk Check er deaktivert i config, gjør ingenting
        if (config.riskMenuMode() == PlayerRiskConfig.RiskMenuMode.DISABLED) {
            return;
        }

        // Hvis SHIFT + Right Click kreves, men SHIFT ikke er trykket ned, avslutt
        if (config.riskMenuMode() == PlayerRiskConfig.RiskMenuMode.SHIFT_RIGHT_CLICK &&
                !client.isKeyPressed(KeyCode.KC_SHIFT)) {
            return;
        }

        // Sjekk om det allerede finnes et "Risk Check"-alternativ for denne spilleren
        for (MenuEntry entry : client.getMenuEntries()) {
            if (entry.getOption().contains(INSPECT_RISK) &&
                    entry.getIdentifier() == event.getIdentifier()) {
                return;
            }
        }

        // Hent fargen fra config og konverter til HEX-format
        Color menuColor = config.riskMenuColor();
        String colorHex = toHex(menuColor);

        // Opprett "Risk Check"-menyvalget med farge
        client.createMenuEntry(-1)
                .setOption("<col=" + colorHex + ">" + INSPECT_RISK + "</col>")
                .setTarget(event.getTarget())
                .setType(MenuAction.RUNELITE_PLAYER)
                .setIdentifier(event.getIdentifier());
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (!event.getMenuOption().contains(INSPECT_RISK)) {
            return;
        }

        // Hent riktig spiller basert på identifier fra menyvalget
        Player targetPlayer = client.getCachedPlayers()[event.getMenuEntry().getIdentifier()];
        if (targetPlayer == null) {
            return;
        }

        // Hvis target er local player og config ikke tillater det, avslutt
        if (targetPlayer.equals(client.getLocalPlayer()) && !config.highlightLocalPlayer()) {
            return;
        }

        int risk = RiskCalculator.calculateRisk(targetPlayer, itemManager);
        String formattedRisk = NumberFormat.getNumberInstance(Locale.US).format(risk);

        // Bruk Dark Slate Blue (#483D8B) for en profesjonell, tydelig melding.
        String message = "<col=483D8B>Player " + targetPlayer.getName() + " is risking " + formattedRisk + " GP.</col>";
        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);
    }

    private String toHex(Color color) {
        return String.format("%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }
}
