/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.atlauncher.App;
import com.atlauncher.data.Account;
import com.atlauncher.data.Downloadable;
import com.atlauncher.utils.Utils;

@SuppressWarnings("serial")
public class BottomBar extends JPanel {

    private JPanel leftSide;
    private JPanel middle;
    private JPanel rightSide;

    private Account fillerAccount;
    private boolean dontSave = false;

    private JButton toggleConsole;
    private JButton openFolder;
    private JButton updateData;
    private JComboBox<Account> username;
    private JButton facebookIcon;
    private JButton youtubeIcon;
    private JButton twitterIcon;
    private JButton flickrIcon;

    public BottomBar() {
        setBorder(BorderFactory.createEtchedBorder());
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 50)); // Make the bottom bar at least
                                                // 50 pixels high

        leftSide = new JPanel();
        leftSide.setLayout(new GridBagLayout());
        middle = new JPanel();
        middle.setLayout(new GridBagLayout());
        rightSide = new JPanel();
        rightSide.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        createButtons();
        setupListeners();

        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(0, 0, 0, 5);
        leftSide.add(toggleConsole, gbc);
        gbc.gridx++;
        leftSide.add(openFolder, gbc);
        gbc.gridx++;
        leftSide.add(updateData, gbc);

        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(0, 0, 0, 5);
        middle.add(username, gbc);

        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(0, 0, 0, 5);
        rightSide.add(facebookIcon, gbc);
        gbc.gridx++;
        rightSide.add(flickrIcon, gbc);
        gbc.gridx++;
        rightSide.add(youtubeIcon, gbc);
        gbc.gridx++;
        rightSide.add(twitterIcon, gbc);

        add(leftSide, BorderLayout.WEST);
        add(middle, BorderLayout.CENTER);
        add(rightSide, BorderLayout.EAST);
    }

    /**
     * Sets up the listeners on the buttons
     */
    private void setupListeners() {
        toggleConsole.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (App.settings.isConsoleVisible()) {
                    App.settings.log("Hiding console");
                    App.settings.setConsoleVisible(false);
                    toggleConsole.setText(App.settings.getLocalizedString("console.show"));
                } else {
                    App.settings.log("Showing console");
                    App.settings.setConsoleVisible(true);
                    toggleConsole.setText(App.settings.getLocalizedString("console.hide"));
                }
            }
        });
        openFolder.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Utils.openExplorer(App.settings.getBaseDir());
            }
        });
        updateData.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final ProgressDialog dialog = new ProgressDialog(App.settings
                        .getLocalizedString("common.checkingforupdates"), 0, App.settings
                        .getLocalizedString("common.checkingforupdates"), "Aborting Update Check!");
                dialog.addThread(new Thread() {
                    public void run() {
                        if (App.settings.hasUpdatedFiles()) {
                            App.settings.reloadLauncherData();
                        }
                        dialog.close();
                    };
                });
                dialog.start();
            }
        });
        username.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (!dontSave) {
                        App.settings.switchAccount((Account) username.getSelectedItem());
                    }
                }
            }
        });
        facebookIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                App.settings.log("Opening Up Techno Chaos Facebook Page");
                Utils.openBrowser("https://www.facebook.com/TechnoChaos");
            }
        });
        flickrIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                App.settings.log("Opening Up Techno Chaos Flickr Page");
                Utils.openBrowser("http://www.flickr.com/photos/techno_chaos/sets/");
            }
        });
        youtubeIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                App.settings.log("Opening Up Techno Chaos YouTube Page");
                Utils.openBrowser("http://www.youtube.com/user/technochaosrobotics");
            }
        });
        twitterIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                App.settings.log("Opening Up Techno Chaos Twitter Page");
                Utils.openBrowser("https://twitter.com/technochaos");
            }
        });
    }

    /**
     * Creates the JButton's for use in the bar
     */
    private void createButtons() {
        if (App.settings.isConsoleVisible()) {
            toggleConsole = new JButton(App.settings.getLocalizedString("console.hide"));
        } else {
            toggleConsole = new JButton(App.settings.getLocalizedString("console.show"));
        }

        openFolder = new JButton(App.settings.getLocalizedString("common.openfolder"));
        updateData = new JButton(App.settings.getLocalizedString("common.update"));

        username = new JComboBox<Account>();
        username.setRenderer(new AccountsDropDownRenderer());
        fillerAccount = new Account(App.settings.getLocalizedString("account.select"));
        username.addItem(fillerAccount);
        for (Account account : App.settings.getAccounts()) {
            username.addItem(account);
        }
        Account active = App.settings.getAccount();
        if (active == null) {
            username.setSelectedIndex(0);
        } else {
            username.setSelectedItem(active);
        }

        facebookIcon = new JButton(Utils.getIconImage("/resources/FacebookIcon.png"));
        facebookIcon.setBorder(BorderFactory.createEmptyBorder());
        facebookIcon.setContentAreaFilled(false);
        facebookIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));

        youtubeIcon = new JButton(Utils.getIconImage("/resources/YouTubeIcon.png"));
        youtubeIcon.setBorder(BorderFactory.createEmptyBorder());
        youtubeIcon.setContentAreaFilled(false);
        youtubeIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));

        flickrIcon = new JButton(Utils.getIconImage("/resources/FlickrIcon.png"));
        flickrIcon.setBorder(BorderFactory.createEmptyBorder());
        flickrIcon.setContentAreaFilled(false);
        flickrIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));

        twitterIcon = new JButton(Utils.getIconImage("/resources/TwitterIcon.png"));
        twitterIcon.setBorder(BorderFactory.createEmptyBorder());
        twitterIcon.setContentAreaFilled(false);
        twitterIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /**
     * Changes the text on the toggleConsole button when the console is hidden
     */
    public void hideConsole() {
        toggleConsole.setText(App.settings.getLocalizedString("console.show"));
    }

    public void reloadAccounts() {
        dontSave = true;
        username.removeAllItems();
        username.addItem(fillerAccount);
        for (Account account : App.settings.getAccounts()) {
            username.addItem(account);
        }
        if (App.settings.getAccount() == null) {
            username.setSelectedIndex(0);
        } else {
            username.setSelectedItem(App.settings.getAccount());
        }
        dontSave = false;
    }
}
