package com.ppolivka.gitlabprojects.branch;

import com.intellij.icons.AllIcons;
import com.intellij.notification.NotificationListener;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.VcsNotifier;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vfs.VirtualFile;
import com.ppolivka.gitlabprojects.common.GitLabApiAction;
import com.ppolivka.gitlabprojects.configuration.ProjectState;
import com.ppolivka.gitlabprojects.configuration.SettingsState;
import com.ppolivka.gitlabprojects.exception.MergeRequestException;
import com.ppolivka.gitlabprojects.merge.helper.GitLabProjectMatcher;
import com.ppolivka.gitlabprojects.util.GitLabUtil;
import com.ppolivka.gitlabprojects.util.NotifyUtil;
import git4idea.GitCommit;
import git4idea.branch.GitBranchUiHandler;
import git4idea.branch.GitBranchWorker;
import git4idea.branch.GitSmartOperationDialog;
import git4idea.commands.Git;
import git4idea.commands.GitCommandResult;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;
import org.apache.http.client.utils.DateUtils;
import org.gitlab.api.models.GitlabProject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.ppolivka.gitlabprojects.util.MessageUtil.showErrorDialog;

/**
 * @author zhanglp
 * @date 2018/12/2
 **/
public class StartMergeBranchAction extends GitLabApiAction {
    /**
     * Abstract method that is called after GitLab Api is validated,
     * we can assume that login credentials are there and api valid
     *
     * @param anActionEvent event information
     */

    private static SettingsState settingsState = SettingsState.getInstance();

    private static GitLabProjectMatcher projectMatcher = new GitLabProjectMatcher();


    private GitlabProject gitlabProject;


    public GitlabProject getGitlabProject() {
        return gitlabProject;
    }

    public void setGitlabProject(GitlabProject gitlabProject) {
        this.gitlabProject = gitlabProject;
    }

    @Override
    public void apiValidAction(AnActionEvent anActionEvent) {

    }

    public StartMergeBranchAction() {
        super("New mergeBranch", "New merge branch from projected branch", AllIcons.Vcs.Equal);
    }


    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        super.actionPerformed(anActionEvent);
        project = anActionEvent.getData(CommonDataKeys.PROJECT);
        if (!GitLabUtil.testGitExecutable(project)) {
            return;
        }

        GitRepository gitRepository = GitLabUtil.getGitRepository(project, file);


        GitflowStartFeatureDialog dialog = new GitflowStartFeatureDialog(project, gitRepository);
        dialog.show();

        if (dialog.getExitCode() != DialogWrapper.OK_EXIT_CODE) return;

        final String featureName = dialog.getNewBranchName();
        final String baseBranchName = dialog.getBaseBranchName();

        String currentDate = DateUtils.formatDate(new Date(), "yyyyMMdd");
        String newName = baseBranchName + "-merge-" + currentDate + "-" + featureName;

        this.runAction(anActionEvent.getProject(), baseBranchName, newName, null);

    }


    public void runAction(Project project, final String baseBranchName, final String featureName, @Nullable final Runnable callInAwtLater){

        new Task.Backgroundable(project, "Starting MergeBranch " + featureName, false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {



                Git git = ServiceManager.getService(Git.class);

                GitRepository repository = GitLabUtil.getGitRepository(project, file);
                Pair<GitRemote, String> remote = GitLabUtil.findGitLabRemote(repository);
                if (remote == null) {
                    showErrorDialog(project, "Can't find GitLab remote", "can not create branch");
                    return;
                }

                ProjectState projectState = ProjectState.getInstance(project);

                Integer projectId;
                Optional<GitlabProject> gitlabProject = projectMatcher.resolveProject(projectState, remote.getFirst(), repository);
                projectId = gitlabProject.orElseThrow(() -> new RuntimeException("No project found")).getId();
                try {
                    GitlabProject gitlabProject1 = settingsState.api(repository).getProject(projectId);
                    settingsState.api(repository).createBranch(gitlabProject1, featureName, baseBranchName);
                } catch (IOException e) {
                    e.printStackTrace();
                    NotifyUtil.notifyError(project, " Cannot create branch " + featureName, featureName + " can not create");
                }
//                final GitCommandResult commandResult = GitLabUtil.createFeatureBranch(project, baseBranchName, featureName);
//                if (callInAwtLater != null && commandResult.success()) {
//                    callInAwtLater.run();
//                }

                if (GitLabUtil.findGitLabRemote(repository) == null) {
                    NotifyUtil.notifySuccess(project, featureName + " create success.",featureName + " create success");

                } else {
                    String firstUrl = GitLabUtil.findGitLabRemote(repository).first.getFirstUrl();
                    firstUrl = firstUrl.replaceAll(".git", "");
                    String branchUrl = firstUrl + "/tree/" + featureName;

                    VcsNotifier.getInstance(project)
                            .notifyImportantInfo(featureName + " create success.", "<a href='" + branchUrl + "'>" + featureName + " create success.</a>", NotificationListener.URL_OPENING_LISTENER);

                    // add to ClipBoard
                    CopyPasteManager.getInstance().setContents(new StringSelection(branchUrl));
                }




            }
        }.queue();
    }

}
