package com.ppolivka.gitlabprojects.branch;

import com.intellij.openapi.project.Project;
import com.ppolivka.gitlabprojects.brnach.ui.AbstractBranchStartDialog;
import git4idea.repo.GitRepository;

public class GitflowStartFeatureDialog extends AbstractBranchStartDialog {

    public GitflowStartFeatureDialog(Project project, GitRepository repo) {
        super(project, repo);
    }

    @Override
    protected String getLabel() {
        return "MergeBranch";
    }

    @Override
    protected String getDefaultBranch() {
        return "dev";
    }
}
