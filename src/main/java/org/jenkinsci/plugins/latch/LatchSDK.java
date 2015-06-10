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

public class LatchSDK extends LatchApp {

    static {
        setHost("http://path2.test.11paths.com");
    }

    private static LatchSDK instance = null;

    public static LatchSDK getInstance() {
        if (instance ==  null) {
            LatchAppConfig latchAppConfig = LatchAppConfig.getInstance();
            if (latchAppConfig.isEnabled()) {
                instance = new LatchSDK(latchAppConfig.getAppId(), latchAppConfig.getSecret());
            }
        }
        return instance;
    }

    private LatchSDK(String appId, String secretKey) {
        super(appId, secretKey);
    }

}
