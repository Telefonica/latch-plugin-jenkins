/**
 * Latch Plugin for Jenkins
 * Copyright (C) 2015 ElevenPaths
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.jenkinsci.plugins.latch;

import com.elevenpaths.latch.LatchApp;
import com.elevenpaths.latch.LatchResponse;
import hudson.Extension;
import hudson.Plugin;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import jenkins.model.GlobalConfigurationCategory;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;


public class LatchAppConfig extends Plugin {

    public static LatchAppConfig getInstance() {
        return Jenkins.getInstance().getPlugin(LatchAppConfig.class);
    }

    private boolean enabled;
    private String appId;
    private String secret;

    @Override
    public void postInitialize() throws Exception {
        load();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    @Extension
    public static class GlobalConfigurationImpl extends GlobalConfiguration {

        private boolean enabled;
        private String appId;
        private String secret;

        @DataBoundConstructor
        public GlobalConfigurationImpl() {
            this.enabled = Jenkins.getInstance().getPlugin(LatchAppConfig.class).isEnabled();
            this.appId = Jenkins.getInstance().getPlugin(LatchAppConfig.class).getAppId();
            this.secret = Jenkins.getInstance().getPlugin(LatchAppConfig.class).getSecret();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            try {
                enabled = json.getBoolean("enabled");
                appId = json.getString("appId");
                secret = json.getString("secret");
                req.bindJSON(this, json);
                save();
                savePluginConfiguration();
                return super.configure(req, json);
            } catch (IOException e) {
                throw new FormException(e, e.getMessage());
            }
        }

        private void savePluginConfiguration() throws IOException {
            LatchAppConfig latchAppConfig = LatchAppConfig.getInstance();
            latchAppConfig.setEnabled(enabled);
            latchAppConfig.setAppId(appId);
            latchAppConfig.setSecret(secret);
            latchAppConfig.save();
        }

        @Override
        public GlobalConfigurationCategory getCategory() {
            return GlobalConfigurationCategory.get(GlobalConfigurationCategory.Security.class);
        }

        public FormValidation doLatchAppTestConnection(@QueryParameter("appId") final String appId,
                                                       @QueryParameter("secret") final String secret) throws IOException, ServletException {

            LatchApp latchApp = new LatchApp(appId, secret);
            LatchResponse pairResponse = latchApp.pair("");

            if (appId.length() != 20) {
                return FormValidation.error(Messages.LatchAppConfig_Invalid_AppId());
            } else if (secret.length() != 40) {
                return FormValidation.error(Messages.LatchAppConfig_Invalid_Secret());
            } else if (pairResponse == null) {
                return FormValidation.error(Messages.LatchAccountProperty_UnreachableConnection());
            } else if (pairResponse.getError().getCode() != 401) {
                return FormValidation.error(pairResponse.getError().toString());
            }
            return FormValidation.ok(Messages.LatchAppConfig_Working());
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }
    }
}