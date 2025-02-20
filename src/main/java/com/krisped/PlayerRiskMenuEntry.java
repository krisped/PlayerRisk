package com.krisped;

import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.PlayerComposition;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.kit.KitType;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.util.Text;

@Slf4j
@Singleton
public class PlayerRiskMenuEntry
{
    // Selve basisteksten for menypunktet (uten farger)
    private static final String BASE_RISK_CHECK_OPTION = "Risk Check";

    @Inject
    private Client client;

    @Inject
    private Provider<MenuManager> menuManager;

    @Inject
    private ItemManager itemManager;

    @Inject
    private PlayerRiskConfig config;

    // Cache for spillerinfo
    private final Map<Integer, PlayerInfo> storedPlayers = new HashMap<>();

    /**
     * Når menyen åpnes, lagre spillerinfo fra oppføringene.
     */
    @Subscribe
    public void onMenuOpened(MenuOpened event)
    {
        // Tøm den gamle cachen for sikkerhets skyld.
        storedPlayers.clear();

        for (MenuEntry entry : event.getMenuEntries())
        {
            if (entry.getActor() instanceof Player)
            {
                Player p = (Player) entry.getActor();
                storedPlayers.put(p.getId(), new PlayerInfo(p.getId(), p.getName(), p.getPlayerComposition()));
            }
        }
    }

    /**
     * Legger til menypunktet hvis riktig betingelse (holdShift av eller shift holdes nede).
     */
    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event)
    {
        // Vi vil bare endre MENY for RUNELITE_PLAYER
        if (event.getType() != MenuAction.RUNELITE_PLAYER.getId())
        {
            return;
        }

        // "holdShift" = av -> menypunktet skal være der alltid,
        // eller "holdShift" = på -> menypunktet skal bare dukke opp når shift holdes.
        if (!config.holdShift() || client.isKeyPressed(KeyCode.KC_SHIFT))
        {
            addMenuItem();
        }
        else
        {
            removeMenuItem();
        }
    }

    /**
     * Hver ClientTick: Hvis holdShift er på, men shift ikke trykkes, fjern menypunktet.
     */
    @Subscribe
    public void onClientTick(ClientTick event)
    {
        if (config.holdShift() && !client.isKeyPressed(KeyCode.KC_SHIFT))
        {
            removeMenuItem();
        }
    }

    /**
     * Håndterer klikk på menyvalget "Risk Check" (farget eller ei).
     */
    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        // Må være av typen RUNELITE_PLAYER
        if (event.getMenuAction() != MenuAction.RUNELITE_PLAYER)
        {
            return;
        }

        // Fjern fargetagene fra event.getMenuOption()
        // og sjekk om det matcher vår ufargede basisstreng.
        String clickedOption = Text.removeTags(event.getMenuOption());
        if (!clickedOption.equals(BASE_RISK_CHECK_OPTION))
        {
            return;
        }

        // Slå opp spillerinfo
        PlayerInfo info = storedPlayers.get(event.getId());
        if (info == null)
        {
            return;
        }

        // Prøv å hente spilleren direkte
        Player target = client.getTopLevelWorldView().players().byIndex(event.getId());
        if (target == null)
        {
            // fallback: let i client.getPlayers()
            target = getPlayerFromInfo(info);
        }
        if (target == null)
        {
            return;
        }

        // Beregn risiko
        long risk = RiskCalculator.calculateRisk(target, itemManager);
        String formattedRisk = NumberFormat.getNumberInstance().format(risk);

        // Avhengig av config, send chatmelding og/eller åpne sidepanel
        if (config.riskMenuAction() == PlayerRiskConfig.RiskMenuAction.CHAT ||
                config.riskMenuAction() == PlayerRiskConfig.RiskMenuAction.ALL)
        {
            String message = "Player " + target.getName() + " is risking " + formattedRisk + " GP.";
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);
        }

        // Åpne sidepanelet
        try
        {
            SwingUtilities.invokeAndWait(() ->
                    PlayerRiskPlugin.getClientToolbar().openPanel(PlayerRiskPlugin.getRiskNavigationButton()));
        }
        catch (InterruptedException | InvocationTargetException ex)
        {
            log.warn("Feil ved åpning av sidepanel: {}", ex.getMessage());
        }

        // Bygg utstyrsmapping
        Map<KitType, net.runelite.api.ItemComposition> equipment = new HashMap<>();
        Map<KitType, Integer> equipmentPrices = new HashMap<>();
        if (target.getPlayerComposition() != null)
        {
            for (KitType kit : KitType.values())
            {
                int itemId = target.getPlayerComposition().getEquipmentId(kit);
                if (itemId > 0)
                {
                    net.runelite.api.ItemComposition comp = client.getItemDefinition(itemId);
                    equipment.put(kit, comp);
                    equipmentPrices.put(kit, itemManager.getItemPrice(itemId));
                }
            }
        }

        final Player finalTarget = target;
        SwingUtilities.invokeLater(() ->
                PlayerRiskPlugin.getRiskPanel().updateEquipment(equipment, equipmentPrices, finalTarget.getName()));
    }

    /**
     * Legger til menypunktet i farget versjon – og husk å bruke samme streng ved fjerning.
     */
    private void addMenuItem()
    {
        // Hent farge fra config
        // (Hvis du bare vil ha en fast farge, kan du hardkode en hex-streng)
        // NB: Her henter vi fargen fra config.riskMenuColor().
        // Du kan også gjøre "String colorHex = "FF0000";" for å hardkode f.eks. rød.
        String colorHex = String.format("%02X%02X%02X",
                config.riskMenuColor().getRed(),
                config.riskMenuColor().getGreen(),
                config.riskMenuColor().getBlue());

        // Bygg farget streng
        String coloredOption = "<col=" + colorHex + ">" + BASE_RISK_CHECK_OPTION + "</col>";

        // Sjekk om menypunktet allerede finnes
        if (Arrays.stream(client.getPlayerOptions()).noneMatch(coloredOption::equals))
        {
            menuManager.get().addPlayerMenuItem(coloredOption);
        }
    }

    /**
     * Fjern menypunktet – og bruk akkurat samme fargede streng vi brukte i addMenuItem().
     */
    private void removeMenuItem()
    {
        // Bygger opp den fargede strengen på nytt, identisk som i addMenuItem()
        String colorHex = String.format("%02X%02X%02X",
                config.riskMenuColor().getRed(),
                config.riskMenuColor().getGreen(),
                config.riskMenuColor().getBlue());
        String coloredOption = "<col=" + colorHex + ">" + BASE_RISK_CHECK_OPTION + "</col>";

        menuManager.get().removePlayerMenuItem(coloredOption);
    }

    /**
     * Hjelpemetode for fallback: Finn en player i client.getPlayers() med matchende ID.
     */
    private Player getPlayerFromInfo(PlayerInfo info)
    {
        for (Player p : client.getPlayers())
        {
            if (p != null && p.getId() == info.getId())
            {
                return p;
            }
        }
        return null;
    }

    /**
     * Enkel klasse for å holde på spillerinfo.
     */
    private static class PlayerInfo
    {
        private final int id;
        private final String name;
        private final PlayerComposition playerComposition;

        PlayerInfo(int id, String name, PlayerComposition composition)
        {
            this.id = id;
            this.name = name;
            this.playerComposition = composition;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public PlayerComposition getPlayerComposition() { return playerComposition; }
    }
}
