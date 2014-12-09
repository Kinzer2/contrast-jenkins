package com.contrast.jenkins.contrastjenkins;

import com.contrastsecurity.rest.*;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ContrastReportPublisher extends Recorder {
    private String applicationID;

    @DataBoundConstructor
    public ContrastReportPublisher(String applicationID) {
        this.applicationID = applicationID;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        logMessage(listener, "Checking connection to Contrast API");

        DescriptorImpl descriptor = (DescriptorImpl) getDescriptor();

        logMessage(listener, "Configuring Contrast API");
        logMessage(listener, "Teamserver URL: " + descriptor.getTeamserverApiUrl());

        ContrastConnection connection = new ContrastConnection(descriptor.getTeamserverApiUsername(),
                    descriptor.getTeamserverApiServiceKey(),
                    descriptor.getTeamserverApiKey(),
                    descriptor.getTeamserverApiUrl() + "/Contrast/api");
        try {
            logMessage(listener, "Retrieving Application Info for Application ID: " + applicationID);
            Application app = connection.getApplication(applicationID);
            logMessage(listener, "Found " + app.getName());

            logMessage(listener, "Retrieving Traces for " + app.getName());
            List<Trace> traces = connection.getTraces(applicationID);
            logMessage(listener, "Found " + traces.size() + " traces");

            File reportFile = new File(build.getWorkspace().absolutize().child("contrast-report.txt").toURI());
            logMessage(listener, "Writing Report to " + reportFile.getAbsolutePath());
            FileWriter report = new FileWriter(reportFile);

            report.append("Contrast Build Report");
            report.append("\nBuild ID: " + build.getId());
            report.append("\nStart Time: " + build.getStartTimeInMillis());
            report.append("\nApplication: " + app.getName());
            report.append("\n---------------------------------------------------------------------------------------\nTraces:\n\n");

            for (Trace t : traces) {
                report.append("- " + t.getTitle() + " (Details: " + descriptor.getTeamserverApiUrl() + "/Contrast/apps/dashboard.html?id=" + app.getId() + "&trace=" + t.getUuid() + ")\n");
            }

            report.close();

        } catch (UnauthorizedException e) {
            e.printStackTrace();
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
        }

        return true;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    private void logMessage(BuildListener listener, String msg) {
        listener.getLogger().println("[ContrastReportPublisher] - " + msg);
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        private String teamserverApiUrl;
        private String teamserverApiUsername;
        private String teamserverApiServiceKey;
        private String teamserverApiKey;

/*      TODO - Implement on-the-fly API credential verification
        public FormValidation doCheck(@AncestorInPath AbstractProject project,
                                      @QueryParameter String value) {

        }
*/


        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            teamserverApiKey = json.getString("teamserverApiKey");
            teamserverApiServiceKey = json.getString("teamserverApiServiceKey");
            teamserverApiUrl = json.getString("teamserverUrl");
            teamserverApiUsername = json.getString("teamserverApiUsername");
            save();
            return super.configure(req, json);
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Contrast Report Publisher";
        }

        public String getTeamserverApiUrl() {
            return teamserverApiUrl;
        }

        public void setTeamserverApiUrl(String teamserverApiUrl) {
            this.teamserverApiUrl = teamserverApiUrl;
        }

        public String getTeamserverApiUsername() {
            return teamserverApiUsername;
        }

        public void setTeamserverApiUsername(String teamserverApiUsername) {
            this.teamserverApiUsername = teamserverApiUsername;
        }

        public String getTeamserverApiServiceKey() {
            return teamserverApiServiceKey;
        }

        public void setTeamserverApiServiceKey(String teamserverApiServiceKey) {
            this.teamserverApiServiceKey = teamserverApiServiceKey;
        }

        public String getTeamserverApiKey() {
            return teamserverApiKey;
        }

        public void setTeamserverApiKey(String teamserverApiKey) {
            this.teamserverApiKey = teamserverApiKey;
        }
    }
}
