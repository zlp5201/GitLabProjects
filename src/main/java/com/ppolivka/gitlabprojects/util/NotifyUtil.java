package com.ppolivka.gitlabprojects.util;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowId;

public class NotifyUtil
{
    private static final NotificationGroup TOOLWINDOW_NOTIFICATION = NotificationGroup.toolWindowGroup(
            "Gitflow Errors", ToolWindowId.VCS, true);
    private static final NotificationGroup STICKY_NOTIFICATION = new NotificationGroup(
            "Gitflow Errors", NotificationDisplayType.STICKY_BALLOON, true);
    private static final NotificationGroup BALLOON_NOTIFICATION = new NotificationGroup(
            "Gitflow Notifications", NotificationDisplayType.BALLOON, true);

    public static void notifySuccess(Project project, String title, String message) {
        notify(NotificationType.INFORMATION, BALLOON_NOTIFICATION, project, title, message);
    }

	public static void notifyInfo(Project project, String title, String message) {
		notify(NotificationType.INFORMATION, TOOLWINDOW_NOTIFICATION, project, title, message);
	}

    public static void notifyError(Project project, String title, String message) {
        notify(NotificationType.ERROR, TOOLWINDOW_NOTIFICATION, project, title, message);
    }

    public static void notifyError(Project project, String title, Exception exception) {
        notify(NotificationType.ERROR, STICKY_NOTIFICATION, project, title, exception.getMessage());
    }

    private static void notify(NotificationType type, NotificationGroup group, Project project, String title, String message) {
        group.createNotification(title, message, type, null).notify(project);
    }
}
