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
import hudson.model.Descriptor;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class LatchAccountProperty extends UserProperty {

    private String accountId;

    @DataBoundConstructor
    public LatchAccountProperty() {}

    private LatchAccountProperty(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountId() {
        return accountId;
    }

    @Override
    public UserProperty reconfigure(StaplerRequest red, net.sf.json.JSONObject form) throws Descriptor.FormException {
        return this;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    /**
     * Descriptor for {@link LatchAccountProperty}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     */
    @Extension
    public static class DescriptorImpl extends UserPropertyDescriptor {

        private static Map<String, ArrayList<String>> tokens = new HashMap<String, ArrayList<String>>();

        private String accountId;

        @Override
        public String getDisplayName() {
            return "Latch";
        }

        public String getCsrf() {
            String csrf = UUID.randomUUID().toString();
            CsrfCache.getInstance().put(csrf, Jenkins.getAuthentication().getName());
            return csrf;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            return super.configure(req, json);
        }

        @Override
        public boolean isEnabled() {
            return Jenkins.getInstance().getPluginManager().getPlugin(LatchAppConfig.class).isEnabled() && LatchAppConfig.getInstance().isEnabled();
        }

        @Override
        public LatchAccountProperty newInstance(User user) {
            return new LatchAccountProperty();
        }

        public FormValidation doLatchPairConnection(@QueryParameter("pairToken") final String pairToken,
                                                    @AncestorInPath User user,
                                                    @QueryParameter("csrf") final String csrf) throws IOException {
            if (validCSRF(csrf)) {
                if (pairToken != null && !pairToken.isEmpty()) {
                    LatchApp latchApp = LatchSDK.getInstance();
                    if (latchApp != null) {
                        LatchResponse pairResponse = latchApp.pair(pairToken);

                        if (pairResponse == null) {
                            return FormValidation.error(Messages.LatchAccountProperty_UnreachableConnection());
                        } else if (pairResponse.getError() != null && pairResponse.getError().getCode() != 205) {
                            return FormValidation.error(Messages.LatchAccountProperty_Invalid_Token());
                        } else {
                            LatchAccountProperty lap = newInstance(user);
                            lap.accountId = pairResponse.getData().get("accountId").getAsString();
                            user.addProperty(lap);
                            return FormValidation.ok(Messages.LatchAccountProperty_Pair());
                        }
                    }
                    return FormValidation.ok(Messages.LatchAccountProperty_PluginDisabled());
                }
                return FormValidation.error(Messages.LatchAccountProperty_Invalid_Token());
            }
            return FormValidation.error(Messages.LatchAccountProperty_Csrf());
        }

        public FormValidation doLatchUnpairConnection(@AncestorInPath User user,
                                                      @QueryParameter("csrf") final String csrf) throws IOException {
            if (validCSRF(csrf)) {
                LatchAccountProperty lap = user.getProperty(LatchAccountProperty.class);
                LatchApp latchApp = LatchSDK.getInstance();
                if (latchApp != null) {
                    LatchResponse unpairResponse = latchApp.unpair(lap.getAccountId());

                    if (unpairResponse == null) {
                        return FormValidation.error(Messages.LatchAccountProperty_UnreachableConnection());
                    } else if (unpairResponse.getError() != null && unpairResponse.getError().getCode() != 201) {
                        return FormValidation.error(unpairResponse.getError().getMessage());
                    } else {
                        lap.accountId = null;
                        lap.user.save();
                        return FormValidation.ok(Messages.LatchAccountProperty_Unpair());
                    }
                }
                return FormValidation.ok(Messages.LatchAccountProperty_PluginDisabled());
            }
            return FormValidation.error(Messages.LatchAccountProperty_Csrf());
        }

        private boolean validCSRF(String csrf) {
            boolean valid = CsrfCache.getInstance().contains(csrf, Jenkins.getAuthentication().getName());
            CsrfCache.getInstance().clear(csrf);
            return valid;
        }
    }
}
