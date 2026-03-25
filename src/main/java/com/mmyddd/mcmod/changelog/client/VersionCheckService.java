package com.mmyddd.mcmod.changelog.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mmyddd.mcmod.changelog.CTNHChangelog;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Environment(EnvType.CLIENT)
public class VersionCheckService {
    // 使用守护线程池，防止程序关闭时被阻塞
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    private static volatile boolean hasUpdate = false;
    private static volatile boolean checkDone = false;
    private static volatile String latestChangelogVersion = "";

    public static void checkForUpdate() {
        if (!Config.isEnableVersionCheck()) {
            CTNHChangelog.LOGGER.info("[VersionCheck] 配置文件中已禁用版本检查");
            checkDone = true;
            return;
        }

        String currentVersion = Config.getModpackVersion();
        String urlStr = Config.getChangelogUrl();

        if (currentVersion.isEmpty() || urlStr.isEmpty() || urlStr.contains("example.com")) {
            CTNHChangelog.LOGGER.info("[VersionCheck] 版本号或 URL 未配置，跳过检查");
            checkDone = true;
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                String changelogVersion = fetchChangelogVersion(urlStr);
                if (changelogVersion != null && !changelogVersion.isEmpty()) {
                    latestChangelogVersion = changelogVersion;
                    // 版本比对：如果不一致则视为有更新
                    hasUpdate = !changelogVersion.equalsIgnoreCase(currentVersion);
                }
            } catch (Exception e) {
                CTNHChangelog.LOGGER.error("[VersionCheck] 检查更新时发生异常: ", e);
            } finally {
                checkDone = true;
                CTNHChangelog.LOGGER.info("[VersionCheck] 检查完成: 有新版本={}, 当前={}, 最新={}",
                        hasUpdate, currentVersion, latestChangelogVersion);
            }
        }, EXECUTOR);
    }

    private static String fetchChangelogVersion(String urlStr) throws Exception {
        HttpURLConnection conn = null;
        try {
            URL url = URI.create(urlStr).toURL();
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            conn.setRequestProperty("User-Agent", "CTNH-Changelog-Fabric/1.21.1");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                CTNHChangelog.LOGGER.warn("[VersionCheck] 无法获取更新日志, HTTP 响应码: {}", responseCode);
                return null;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                if (root.has("entries")) {
                    JsonArray entries = root.getAsJsonArray("entries");
                    if (entries.size() > 0) {
                        // 假设第一个 entry 是最新版本
                        JsonObject latestEntry = entries.get(0).getAsJsonObject();
                        if (latestEntry.has("version")) {
                            return latestEntry.get("version").getAsString();
                        }
                    }
                }
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    // --- Getter 方法 ---

    public static boolean isCheckDone() {
        return checkDone;
    }

    public static String getLatestChangelogVersion() {
        return latestChangelogVersion;
    }

    public static boolean hasUpdate() {
        // 只有开启了检查且检查到更新时才返回 true
        return Config.isEnableVersionCheck() && hasUpdate;
    }

    public static void reset() {
        hasUpdate = false;
        checkDone = false;
        latestChangelogVersion = "";
    }
}