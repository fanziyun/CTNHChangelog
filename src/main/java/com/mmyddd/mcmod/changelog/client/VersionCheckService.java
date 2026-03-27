package com.mmyddd.mcmod.changelog.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mmyddd.mcmod.changelog.CTNHChangelog;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Environment(EnvType.CLIENT)
public class VersionCheckService {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    private static volatile boolean hasUpdate = false;
    private static volatile boolean checkDone = false;
    private static volatile String latestChangelogVersion = "";

    public static void checkForUpdate() {
        if (CTNHChangelog.config == null) return;
        if (!CTNHChangelog.config.enableVersionCheck) {
            checkDone = true;
            return;
        }

        String currentVersion = CTNHChangelog.config.modpackVersion;
        String urlStr = CTNHChangelog.config.changelogUrl;

        if (currentVersion.isEmpty() || urlStr.isEmpty() || urlStr.contains("example.com")) {
            checkDone = true;
            return;
        }

        if (urlStr.contains("github.com") && urlStr.contains("/blob/")) {
            urlStr = urlStr.replace("github.com", "raw.githubusercontent.com").replace("/blob/", "/");
        }

        final String finalUrl = urlStr;

        CompletableFuture.runAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(finalUrl))
                        .header("User-Agent", "CTNH-Changelog-Fabric/1.21.1")
                        .timeout(Duration.ofSeconds(10))
                        .build();

                HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
                    if (root.has("entries")) {
                        JsonArray entries = root.getAsJsonArray("entries");
                        if (!entries.isEmpty()) {
                            JsonObject latestEntry = entries.get(0).getAsJsonObject();
                            if (latestEntry.has("version")) {
                                String remoteVer = latestEntry.get("version").getAsString();
                                latestChangelogVersion = remoteVer;
                                hasUpdate = isVersionNewer(currentVersion, remoteVer);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                CTNHChangelog.LOGGER.error("[VersionCheck] 检查更新失败: {}", e.getMessage());
            } finally {
                checkDone = true;
            }
        }, EXECUTOR);
    }

    private static boolean isVersionNewer(String local, String remote) {
        if (local == null || remote == null || local.equalsIgnoreCase(remote)) return false;
        try {
            String v1 = local.replaceAll("[^\\d.]", "");
            String v2 = remote.replaceAll("[^\\d.]", "");
            String[] vals1 = v1.split("\\.");
            String[] vals2 = v2.split("\\.");
            int length = Math.max(vals1.length, vals2.length);
            for (int i = 0; i < length; i++) {
                int num1 = i < vals1.length ? Integer.parseInt(vals1[i]) : 0;
                int num2 = i < vals2.length ? Integer.parseInt(vals2[i]) : 0;
                if (num2 > num1) return true;
                if (num1 > num2) return false;
            }
        } catch (Exception e) {
            return !local.equalsIgnoreCase(remote);
        }
        return false;
    }

    public static boolean isCheckDone() { return checkDone; }

    public static String getLatestChangelogVersion() { return latestChangelogVersion; }

    public static boolean hasUpdate() {
        return CTNHChangelog.config != null && CTNHChangelog.config.enableVersionCheck && hasUpdate;
    }

    public static void reset() {
        hasUpdate = false;
        checkDone = false;
        latestChangelogVersion = "";
    }
}