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

import com.elevenpaths.latch.LatchResponse;
import com.google.gson.JsonObject;
import jenkins.model.Jenkins;

import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AsyncLatchHandler implements Callable<Boolean> {

    private static final Logger LOGGER = Logger.getLogger(AsyncLatchHandler.class.getName());

    private final static int LATCH_SECONDS_TIMEOUT = 3;

    private LatchSDK api;
    private String accountId;

    private AsyncLatchHandler(LatchSDK api, String accountId) {
        this.api = api;
        this.accountId = accountId;
    }

    @Override
    public Boolean call() throws Exception {
        LatchResponse response = this.api.status(accountId);

        if (response != null && response.getData() != null && response.getData().has("operations")) {

            String appId = Jenkins.getInstance().getPlugin(LatchAppConfig.class).getAppId();
            JsonObject json = response.getData().getAsJsonObject("operations").getAsJsonObject(appId);

            if (json != null && json.has("status") && "off".equals(json.get("status").getAsString())) {
                return false;
            }
        }

        return true;
    }

    public static boolean checkLatchUnlockedStatus(LatchSDK api, String accountId){
        boolean result = true;
        try {
            if (api != null && accountId != null && !accountId.isEmpty()) {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Future<Boolean> future = executor.submit(new AsyncLatchHandler(api, accountId));
                result = future.get(LATCH_SECONDS_TIMEOUT, TimeUnit.SECONDS);
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "InterruptedException", e);
        } catch (ExecutionException e) {
            LOGGER.log(Level.SEVERE, "ExecutionException", e);
        } catch (TimeoutException e) {
            LOGGER.log(Level.SEVERE, "TimeoutException", e);
        }
        return result;
    }
}
