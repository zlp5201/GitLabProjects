package com.ppolivka.gitlabprojects.configuration;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.ppolivka.gitlabprojects.common.EditableView;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Dialog for GitLab setting configuration
 *
 * @author ppolivka
 * @since 27.10.2015
 */
public class ConfigurationDialog extends DialogWrapper implements SearchableConfigurable, EditableView<SettingsState, String[]> {

    private static final String DIALOG_TITLE = "GitLab Settings";
    SettingsState settingsState = SettingsState.getInstance();

    private JPanel mainPanel;
    private JTextField textHost;
    private JTextField textAPI;
    private JButton apiHelpButton;

    public ConfigurationDialog(@NotNull Component parent, boolean canBeParent) {
        super(parent, canBeParent);
        init();
    }

    public ConfigurationDialog(@Nullable Project project) {
        super(project);
        init();
    }

    //region Dialog Wrapper Override methods
    @Override
    protected void init() {
        super.init();
        setTitle(DIALOG_TITLE);
        setSize(600, 300);
        setAutoAdjustable(false);
        onServerChange();
        textHost.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onServerChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onServerChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onServerChange();
            }
        });
        apiHelpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openWebPage(generateHelpUrl());
            }
        });
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return createComponent();
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        final String hostText = textHost.getText();
        final String apiText = textAPI.getText();
        try {
            if (isModified() && isNotBlank(hostText) && isNotBlank(apiText)) {
                if (!isValidUrl(hostText)) {
                    return new ValidationInfo(SettingError.NOT_A_URL.message(), textHost);
                } else {
                    try {
                        settingsState.isApiValid(hostText, apiText);
                    } catch (UnknownHostException e) {
                        return new ValidationInfo(SettingError.SERVER_CANNOT_BE_REACHED.message(), textHost);
                    } catch (IOException e) {
                        return new ValidationInfo(SettingError.INVALID_API_TOKEN.message(), textAPI);
                    }
                }
            }
        } catch (Exception e) {
            return new ValidationInfo(SettingError.GENERAL_ERROR.message());
        }
        return null;
    }
    //endregion

    //region Searchable Configurable interface methods
    @NotNull
    @Override
    public String getId() {
        return this.getClass().getName();
    }

    @Nullable
    @Override
    public Runnable enableSearch(String s) {
        return null;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return DIALOG_TITLE;
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        reset();
        return mainPanel;
    }

    @Override
    public boolean isModified() {
        String[] save = save();
        return save == null
                || !save[0].equals(settingsState.getHost())
                || !save[1].equals(settingsState.getToken());
    }

    @Override
    public void apply() throws ConfigurationException {
        String[] save = save();
        settingsState.setHost(save[0]);
        settingsState.setToken(save[1]);
    }

    @Override
    public void reset() {
        fill(settingsState);
    }

    @Override
    public void disposeUIResources() {

    }
    //endregion

    //region Editable View interface methods
    @Override
    public void fill(SettingsState settingsState) {
        textHost.setText(settingsState == null ? "" : settingsState.getHost());
        textAPI.setText(settingsState == null ? "" : settingsState.getToken());
    }

    @Override
    public String[] save() {
        return new String[]{textHost.getText(), textAPI.getText()};
    }
    //endregion

    //region Private methods
    private String generateHelpUrl() {
        final String hostText = textHost.getText();
        StringBuilder helpUrl = new StringBuilder();
        helpUrl.append(hostText);
        if (!hostText.endsWith("/")) {
            helpUrl.append("/");
        }
        helpUrl.append("profile/account");
        return helpUrl.toString();
    }

    private void onServerChange() {
        ValidationInfo validationInfo = doValidate();
        if (validationInfo == null || (validationInfo != null && !validationInfo.message.equals(SettingError.NOT_A_URL.message))) {
            apiHelpButton.setEnabled(true);
            apiHelpButton.setToolTipText("API Key can be find in your profile setting inside GitLab Server: \n" + generateHelpUrl());
        } else {
            apiHelpButton.setEnabled(false);
        }
    }

    private static boolean isValidUrl(String s) {
        Pattern urlPattern = Pattern.compile("^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
        Matcher matcher = urlPattern.matcher(s);
        return matcher.matches();
    }

    private static void openWebPage(String uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(new URI(uri));
            } catch (Exception ignored) {
            }
        }
    }
    //endregion
}