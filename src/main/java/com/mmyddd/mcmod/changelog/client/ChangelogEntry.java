package com.mmyddd.mcmod.changelog.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mmyddd.mcmod.changelog.CTNHChangelog;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ChangelogEntry {
    private final String version;
    private final String date;
    private final String title;
    private final List<String> types;
    private final List<String> tags;
    private final List<String> changes;
    private final int color;

    private static final List<ChangelogEntry> ENTRIES = new ArrayList<>();
    private static final Map<String, Integer> CUSTOM_TAG_COLORS = new HashMap<>();
    private static volatile boolean loading = false;
    private static volatile boolean loaded = false;
    private static String remoteFooter = "";

    public ChangelogEntry(String version, String date, String title, List<String> types, List<String> tags, List<String> changes, int color) {
        this.version = version;
        this.date = date;
        this.title = title;
        this.types = types;
        this.tags = tags;
        this.changes = changes;
        this.color = color;
    }

    public static void initLoader() { resetLoaded(); }

    public static void resetLoaded() {
        ENTRIES.clear();
        CUSTOM_TAG_COLORS.clear();
        loaded = false;
        loading = false;
        remoteFooter = "";
    }

    public static CompletableFuture<Void> loadAfterConfig() {
        if (loading) return CompletableFuture.completedFuture(null);
        loading = true;

        // 核心修复 1: 必须从主类注册的 config 实例拿数据，否则永远是默认值
        if (CTNHChangelog.config == null) {
            System.err.println("[整合包更新日志] 配置实例尚未初始化，取消下载。");
            loading = false;
            return CompletableFuture.completedFuture(null);
        }

        String url = CTNHChangelog.config.changelogUrl.trim();

        // 核心修复 2: 增强型 URL 容错处理 (解决 404)
        // 处理 GitHub 的 blob 链接转 raw 链接
        if (url.contains("github.com") && url.contains("/blob/")) {
            url = url.replace("github.com", "raw.githubusercontent.com")
                    .replace("/blob/", "/");
        }

        // 自动处理代理前缀导致的重复或格式问题
        if (url.startsWith("https://gh-proxy.org/https://raw.githubusercontent.com")) {
            // 这种拼接通常是安全的，但如果依然 404，建议直接在配置里填 raw 链接
        }

        String finalUrl = url + (url.contains("?") ? "&" : "?") + "v=" + System.currentTimeMillis();

        System.out.println("[整合包更新日志] 正在从远程获取数据: " + url);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(finalUrl))
                .header("User-Agent", "Mozilla/5.0 (CTNH-Changelog-Loader)")
                .header("Cache-Control", "no-cache")
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        parseJson(response.body());
                        System.out.println("[整合包更新日志] 数据解析完成，共加载 " + ENTRIES.size() + " 条更新。");
                    } else {
                        System.err.println("[整合包更新日志] 刷新失败，错误码: " + response.statusCode() + " (请检查 URL 是否为 Raw 格式)");
                    }
                    return null;
                })
                .exceptionally(ex -> {
                    System.err.println("[整合包更新日志] 网络连接异常: " + ex.getMessage());
                    return null;
                })
                .thenAccept(v -> {
                    loading = false;
                    loaded = true;
                });
    }

    private static void parseJson(String json) {
        try {
            ENTRIES.clear();
            JsonObject root = JsonParser.parseString(json.trim()).getAsJsonObject();
            if (root.has("footer")) remoteFooter = root.get("footer").getAsString();

            if (root.has("tagColors")) {
                JsonObject colorsObj = root.getAsJsonObject("tagColors");
                colorsObj.entrySet().forEach(entry -> {
                    try {
                        CUSTOM_TAG_COLORS.put(entry.getKey(), (int) Long.parseLong(entry.getValue().getAsString().replace("0x", ""), 16));
                    } catch (Exception ignored) {}
                });
            }

            if (root.has("entries")) {
                JsonArray array = root.getAsJsonArray("entries");
                for (JsonElement el : array) {
                    JsonObject obj = el.getAsJsonObject();
                    List<String> types = new ArrayList<>();
                    if (obj.has("type")) obj.getAsJsonArray("type").forEach(t -> types.add(t.getAsString()));
                    List<String> tags = new ArrayList<>();
                    if (obj.has("tags")) obj.getAsJsonArray("tags").forEach(t -> tags.add(t.getAsString()));
                    List<String> changes = new ArrayList<>();
                    if (obj.has("changes")) obj.getAsJsonArray("changes").forEach(c -> changes.add(c.getAsString()));

                    int entryColor = 0xFFFFFFFF;
                    if (obj.has("color")) {
                        try { entryColor = (int) Long.parseLong(obj.get("color").getAsString().replace("0x", ""), 16); } catch (Exception e) {}
                    }

                    ENTRIES.add(new ChangelogEntry(
                            obj.get("version").getAsString(),
                            obj.has("date") ? obj.get("date").getAsString() : "",
                            obj.has("title") ? obj.get("title").getAsString() : "",
                            types, tags, changes, entryColor
                    ));
                }
            }
        } catch (Exception e) {
            System.err.println("[整合包更新日志] 解析数据失败: " + e.getMessage());
        }
    }

    public static List<ChangelogEntry> getAllEntries() { return ENTRIES; }
    public static boolean isLoading() { return loading; }
    public static boolean isLoaded() { return loaded; }

    public static String getFooterText() {
        String ver = (CTNHChangelog.config != null) ? CTNHChangelog.config.modpackVersion : "Unknown";
        return (remoteFooter == null || remoteFooter.isEmpty()) ? "§b整合包更新日志 §7| §f" + ver : remoteFooter;
    }

    public static int getTagColor(String tag) { return CUSTOM_TAG_COLORS.getOrDefault(tag, 0xFFFFAA00); }
    public String getVersion() { return version; }
    public String getDate() { return date; }
    public String getTitle() { return title; }
    public List<String> getTypes() { return types; }
    public List<String> getTags() { return tags; }
    public List<String> getChanges() { return changes; }
    public int getColor() { return color; }
}