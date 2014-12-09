package com.contrast.jenkins.contrastjenkins;

import hudson.model.Action;

public abstract class ContrastJenkinsBaseAction implements Action {
    public static final String ICON_FILE_NAME = "/plugin/contrast-jenkins/contrast.png";
    public static final String DISPLAY_NAME = "Contrast Report";
    public static final String URL_NAME = "contrast-report";

    public String getIconFileName() {
        return ICON_FILE_NAME;
    }

    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    public String getUrlName() {
        return URL_NAME;
    }
}
