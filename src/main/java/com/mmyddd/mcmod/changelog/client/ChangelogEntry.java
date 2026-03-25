package com.mmyddd.mcmod.changelog.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mmyddd.mcmod.changelog.CTNHChangelog;

import java.io.StringReader;
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

        String url = Config.getChangelogUrl().trim();
        if (url.contains("github.com") && url.contains("/blob/")) {
            url = url.replace("github.com", "raw.githubusercontent.com").replace("/blob/", "/");
        }

        String finalUrl = url + (url.contains("?") ? "&" : "?") + "v=" + System.currentTimeMillis();

        System.out.println("[整合包更新日志] 正在从远程获取数据...");

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(finalUrl))
                .header("User-Agent", "Mozilla/5.0")
                .header("Cache-Control", "no-cache")
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        parseJson(response.body());
                    } else {
                        System.err.println("[整合包更新日志] 刷新失败，错误码: " + response.statusCode());
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

    // 这里改成了“整合包”
    public static String getFooterText() {
        return (remoteFooter == null || remoteFooter.isEmpty()) ? "§b整合包更新日志 §7| §f" + Config.getModpackVersion() : remoteFooter;
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