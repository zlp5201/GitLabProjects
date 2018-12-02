package com.ppolivka.gitlabprojects.common;

import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.ppolivka.gitlabprojects.configuration.SettingsState;
import com.ppolivka.gitlabprojects.dto.GitlabServer;
import com.ppolivka.gitlabprojects.util.GitLabUtil;
import git4idea.GitLocalBranch;
import git4idea.repo.GitRepository;

import java.util.Collection;

/**
 * @author zhanglp
 * @date 2018/12/2
 **/
public class OpenGitLabAction extends GitLabApiAction {

    public OpenGitLabAction() {
        super("open git address", "List of all merge requests for this project", AllIcons.Vcs.MergeSourcesTree);
    }

    /**
     * Implement this method to provide your action handler.
     *
     * @param e Carries information on the invocation place
     */
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        project = anActionEvent.getData(CommonDataKeys.PROJECT);
        file = anActionEvent.getData(CommonDataKeys.VIRTUAL_FILE);
        if (!GitLabUtil.testGitExecutable(project)) {
            return;
        }

        GitRepository gitRepository = GitLabUtil.getGitRepository(project, file);

        if (gitRepository != null) {
            String firstUrl = GitLabUtil.findGitLabRemote(gitRepository).first.getFirstUrl();
            String branchName = gitRepository.getCurrentBranch().getName();
            firstUrl = firstUrl.replaceAll(".git", "");
            String fullname = firstUrl + "/tree/" + branchName;

           BrowserUtil.browse(fullname);
        }
    }

    /**
     * Abstract method that is called after GitLab Api is validated,
     * we can assume that login credentials are there and api valid
     *
     * @param anActionEvent event information
     */
    @Override
    public void apiValidAction(AnActionEvent anActionEvent) {

    }
}
