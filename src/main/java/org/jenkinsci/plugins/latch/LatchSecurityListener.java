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

import hudson.Extension;
import hudson.model.User;
import jenkins.model.Jenkins;
import jenkins.security.SecurityListener;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.userdetails.UserDetails;

@Extension
public class LatchSecurityListener extends SecurityListener {

    @Override
    protected void authenticated(UserDetails userDetails) {
        if (!checkLatch(userDetails)) {
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    @Override
    protected void failedToAuthenticate(String s) {}

    @Override
    protected void loggedIn(String s) {}

    @Override
    protected void failedToLogIn(String s) {}

    @Override
    protected void loggedOut(String s) {}

    private static boolean checkLatch(UserDetails userDetails){
        boolean result = true;
        User user  = Jenkins.getInstance().getUser(userDetails.getUsername());
        LatchAccountProperty latchUserProperties = (user != null) ? user.getProperty(LatchAccountProperty.class) : null;
        if (latchUserProperties != null && latchUserProperties.getAccountId() != null) {
            result = AsyncLatchHandler.checkLatchUnlockedStatus(LatchSDK.getInstance(), latchUserProperties.getAccountId());
        }
        return result;
    }
}
