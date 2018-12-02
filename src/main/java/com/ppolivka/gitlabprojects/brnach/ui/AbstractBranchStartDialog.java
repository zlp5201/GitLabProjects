package com.ppolivka.gitlabprojects.brnach.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;

import javax.swing.*;
import com.ppolivka.gitlabprojects.util.GitflowBranchUtil;
import git4idea.repo.GitRepository;

import java.util.Arrays;
import java.util.List;

/**
 * Base class for a "start" dialog. Such a dialog prompts the user to enter a name for a new branch
 * and select a base branch. See {@link } for an example implementation.
 */
public abstract class AbstractBranchStartDialog extends DialogWrapper {

    private JPanel contentPane;
    private JTextField branchNameTextField;
    private JComboBox branchFromCombo;
    private JLabel branchNameLabel;
    private JLabel spacesLabel;

    private Project project;
    protected GitRepository myRepo;

    public AbstractBranchStartDialog(Project project, GitRepository repo) {
        super(project, false);
        this.project = project;
        this.myRepo = repo;

        init();
        final String label = getLabel();
        setTitle("New " + label + "...");
        branchNameLabel.setText(String.format("Enter a name for the new %s...", label));
        setModal(true);
        branchFromCombo.setModel(createBranchComboModel(getDefaultBranch()));
        spacesLabel.setText(getBaseBranchName() + "-20181202-" + branchNameTextField);
    }


    public ComboBoxModel createBranchComboModel(String defaultBranch) {
        final List<String> branchList = Arrays.asList("dev", "qa");

        ComboEntry[] entries = new ComboEntry[branchList.size() + 1];
        entries[0] = new ComboEntry(defaultBranch, defaultBranch + " (default)");
        for (int i = 1; i <= branchList.size(); i++) {
            String branchName = branchList.get(i - 1);
            entries[i] = new ComboEntry(branchName, branchName);
        }

        return new DefaultComboBoxModel(entries);
    }

    /**
     * An entry for the branch selection dropdown/combo.
     */
    public static class ComboEntry {
        private String branchName, label;

        public ComboEntry(String branchName, String label) {
            this.branchName = branchName;
            this.label = label;
        }

        public String getBranchName() {
            return branchName;
        }

        @Override
        public String toString() {
            return label;
        }
    }


    @Override
    public JComponent getPreferredFocusedComponent() {
        return branchNameTextField;
    }

    /**
     * @return The name of the new branch as specified by the user
     */
    public String getNewBranchName() {
        return branchNameTextField.getText().trim().replaceAll(" ", "_");
    }

    /**
     * @return The name of the base branch (the branch on which the new hotfix or feature should be
     * based on)
     */
    public String getBaseBranchName() {
        ComboEntry selectedBranch = (ComboEntry)branchFromCombo.getModel().getSelectedItem();
        return selectedBranch.getBranchName();
    }

    /**
     * @return The label for this dialog (e.g. "hotfix" or "feature"). Will be used for the window
     * title and other labels.
     */
    protected abstract String getLabel();

    /**
     * @return The name of the default branch, i.e. the branch that is selected by default when
     * opening the dialog.
     */
    protected abstract String getDefaultBranch();

    protected Project getProject() {
        return this.project;
    }

    @Override
    protected ValidationInfo doValidate() {
        boolean isBranchNameSpecified = branchNameTextField.getText().trim().length() > 0;
        if (!isBranchNameSpecified) {
            return new ValidationInfo("No name specified", branchNameTextField);
        } else {
            return null;
        }
    }

    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }
}
