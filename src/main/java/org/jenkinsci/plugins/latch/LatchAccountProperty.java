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
import hudson.model.*;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.*;
import java.io.IOException;


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

        private String accountId;

        @Override
        public String getDisplayName() {
            return "Latch";
        }

        @Override
        public boolean isEnabled() {
            return Jenkins.getInstance().getPluginManager().getPlugin(LatchAppConfig.class).isEnabled() && LatchAppConfig.getInstance().isEnabled();
        }

        @Override
        public LatchAccountProperty newInstance(User user) {
            return new LatchAccountProperty(accountId);
        }

        public FormValidation doLatchPairConnection(@QueryParameter("pairToken") final String pairToken,
                                                    @AncestorInPath User user) throws IOException {
            if (pairToken != null && !pairToken.isEmpty()) {
                LatchApp latchApp = LatchSDK.getInstance();
                if (latchApp != null) {
                    LatchResponse pairResponse = latchApp.pair(pairToken);

                    if (pairResponse == null) {
                        return FormValidation.error(Messages.LatchAccountProperty_UnreachableConnection());
                    } else if (pairResponse.getError() != null && pairResponse.getError().getCode() != 205) {
                        return FormValidation.error(Messages.LatchAccountProperty_Invalid_Token());
                    } else {
                        accountId = pairResponse.getData().get("accountId").getAsString();
                        LatchAccountProperty lap = newInstance(user);
                        user.addProperty(lap);
                        return FormValidation.ok(Messages.LatchAccountProperty_Pair());
                    }
                }
                return FormValidation.ok(Messages.LatchAccountProperty_PluginDisabled());
            }
            return FormValidation.error(Messages.LatchAccountProperty_Invalid_Token());
        }

        public FormValidation doLatchUnpairConnection(@AncestorInPath User user) throws IOException {
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
                    return FormValidation.ok(Messages.LatchAccountProperty_Unpair());
                }
            }
            return FormValidation.ok(Messages.LatchAccountProperty_PluginDisabled());
        }
    }
}
