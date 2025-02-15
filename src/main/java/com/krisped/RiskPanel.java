package com.krisped;

import net.runelite.api.kit.KitType;
import net.runelite.api.ItemComposition;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.QuantityFormatter;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class RiskPanel extends PluginPanel
{
    private static final String NO_PLAYER_SELECTED = "No player selected";
    private JPanel equipmentPanels;
    private JPanel header;
    public JLabel nameLabel;
    private final ItemManager itemManager;

    public RiskPanel(ItemManager itemManager)
    {
        this.itemManager = itemManager;

        // Bruk GroupLayout for hovedpanelet
        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        // Transparent bakgrunn
        setBackground(new Color(0, 0, 0, 0));
        setOpaque(false);

        // Equipment-panelet med GridBagLayout
        equipmentPanels = new JPanel(new GridBagLayout());
        equipmentPanels.setOpaque(false);

        // Headerpanelet med spillernavn
        header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(0, 0, 10, 0));
        header.setOpaque(false);

        nameLabel = new JLabel(NO_PLAYER_SELECTED, SwingConstants.CENTER);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(FontManager.getRunescapeFont());
        nameLabel.setOpaque(false);

        header.add(nameLabel, BorderLayout.CENTER);

        layout.setHorizontalGroup(
                layout.createParallelGroup()
                        .addComponent(header)
                        .addComponent(equipmentPanels)
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(header)
                        .addGap(10)
                        .addComponent(equipmentPanels)
        );

        // Start med et tomt oppsett
        updateEquipment(new java.util.HashMap<>(), new java.util.HashMap<>(), "");
    }

    /**
     * Oppdaterer panelet med spillerens utstyr og risiko per slot.
     */
    public void updateEquipment(Map<KitType, ItemComposition> equipment, Map<KitType, Integer> equipmentRisks, String playerName)
    {
        if (playerName == null || playerName.isEmpty())
        {
            nameLabel.setText(NO_PLAYER_SELECTED);
        }
        else
        {
            nameLabel.setText("Player: " + playerName);
        }

        SwingUtilities.invokeLater(() ->
        {
            equipmentPanels.removeAll();
            // Opprett en ny GridBagConstraints for hvert kall slik at vi starter øverst
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;
            gbc.gridx = 0;
            gbc.gridy = 0;

            AtomicLong totalRisk = new AtomicLong();
            for (Map.Entry<KitType, ItemComposition> entry : equipment.entrySet())
            {
                KitType kit = entry.getKey();
                ItemComposition item = entry.getValue();
                int risk = equipmentRisks.getOrDefault(kit, 0);
                totalRisk.addAndGet(risk);
                AsyncBufferedImage image = itemManager.getImage(item.getId());
                JPanel itemPanel = createRiskItemPanel(item, kit, image, risk);
                itemPanel.setOpaque(false);
                equipmentPanels.add(itemPanel, gbc);
                gbc.gridy++;
            }
            if (!nameLabel.getText().equals(NO_PLAYER_SELECTED))
            {
                JPanel totalPanel = createTotalPanel(totalRisk.get());
                totalPanel.setOpaque(false);
                equipmentPanels.add(totalPanel, gbc);
                gbc.gridy++;
            }
            equipmentPanels.revalidate();
            equipmentPanels.repaint();
        });
    }

    /**
     * Lager et panel for et enkelt utstyrs-slot med risikovurdering.
     */
    private JPanel createRiskItemPanel(ItemComposition item, KitType kit, AsyncBufferedImage image, int risk)
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(new EmptyBorder(3, 3, 3, 3));
        panel.setOpaque(false);

        JLabel iconLabel = new JLabel();
        image.addTo(iconLabel);
        panel.add(iconLabel);
        panel.add(Box.createHorizontalStrut(8));

        // Delpanel for tekst
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel name = new JLabel(item.getName());
        name.setFont(FontManager.getRunescapeFont());
        name.setForeground(Color.WHITE);
        name.setOpaque(false);

        // Formatér kit-typen med stor forbokstav
        String kitName = StringUtils.capitalize(kit.toString().toLowerCase());
        JLabel slot = new JLabel(kitName);
        slot.setFont(FontManager.getRunescapeSmallFont());
        slot.setForeground(Color.GRAY);
        slot.setOpaque(false);

        JLabel riskLabel = new JLabel(QuantityFormatter.quantityToStackSize(risk));
        riskLabel.setFont(FontManager.getRunescapeFont());
        if (risk > 1_000_000)
        {
            riskLabel.setForeground(Color.GREEN);
        }
        else if (risk > 100_000)
        {
            riskLabel.setForeground(Color.WHITE);
        }
        else if (risk == 0)
        {
            riskLabel.setForeground(Color.LIGHT_GRAY);
        }
        else
        {
            riskLabel.setForeground(Color.YELLOW);
        }
        riskLabel.setToolTipText(java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(risk));
        riskLabel.setOpaque(false);

        textPanel.add(name);
        textPanel.add(slot);
        textPanel.add(riskLabel);

        panel.add(textPanel);
        return panel;
    }

    /**
     * Lager et panel for totalrisiko.
     */
    private JPanel createTotalPanel(long totalRisk)
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(new EmptyBorder(3, 3, 3, 3));
        panel.setOpaque(false);

        JLabel label = new JLabel("Total: ");
        label.setFont(FontManager.getRunescapeFont());
        label.setForeground(Color.WHITE);
        label.setOpaque(false);
        JLabel totalLabel = new JLabel(QuantityFormatter.quantityToStackSize(totalRisk));
        totalLabel.setFont(FontManager.getRunescapeFont());
        if (totalRisk > 1_000_000)
        {
            totalLabel.setForeground(Color.GREEN);
        }
        else if (totalRisk > 100_000)
        {
            totalLabel.setForeground(Color.WHITE);
        }
        else if (totalRisk == 0)
        {
            totalLabel.setForeground(Color.LIGHT_GRAY);
        }
        else
        {
            totalLabel.setForeground(Color.YELLOW);
        }
        totalLabel.setOpaque(false);
        panel.add(Box.createHorizontalStrut(8));
        panel.add(label);
        panel.add(totalLabel);
        return panel;
    }
}
