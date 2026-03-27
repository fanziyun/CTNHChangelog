package com.mmyddd.mcmod.changelog.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Config {
    private static final File CONFIG_FILE = new File(MinecraftClient.getInstance().runDirectory, "config/ctnhchangelog.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // 默认值
    private static String changelogUrl = "https://example.com/changelog.json";
    private static String modpackVersion = "1.0.0";
    private static boolean enableVersionCheck = true;
    private static boolean showOnTitle = true;
    private static boolean showOnSelectWorld = false;

    public static void load() {
        if (!CONFIG_FILE.exists()) {
            save(); // 如果文件不存在，保存一份默认的
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            if (json.has("changelogUrl")) changelogUrl = json.get("changelogUrl").getAsString();
            if (json.has("modpackVersion")) modpackVersion = json.get("modpackVersion").getAsString();
            if (json.has("enableVersionCheck")) enableVersionCheck = json.get("enableVersionCheck").getAsBoolean();
            if (json.has("showOnTitle")) showOnTitle = json.get("showOnTitle").getAsBoolean();
            if (json.has("showOnSelectWorld")) showOnSelectWorld = json.get("showOnSelectWorld").getAsBoolean();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        JsonObject json = new JsonObject();
        json.addProperty("changelogUrl", changelogUrl);
        json.addProperty("modpackVersion", modpackVersion);
        json.addProperty("enableVersionCheck", enableVersionCheck);
        json.addProperty("showOnTitle", showOnTitle);
        json.addProperty("showOnSelectWorld", showOnSelectWorld);

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(json, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public class CTNHChangelogConfig {
        // 按钮显示的文字
        public String externalLinkName = "项目主页";
        // 按钮跳转的链接
        public String externalLinkUrl = "https://github.com/FanZiyun";

        public String getExternalLinkName() {
            return externalLinkName != null ? externalLinkName : "Link";
        }

        public String getExternalLinkUrl() {
            return externalLinkUrl != null ? externalLinkUrl : "";
        }
    }
    // --- 供其他类调用的 Getter ---
    public static String getChangelogUrl() { return changelogUrl; }
    public static String getModpackVersion() { return modpackVersion; }
    public static boolean isEnableVersionCheck() { return enableVersionCheck; }
    public static boolean showButtonOnTitleScreen() { return showOnTitle; }
    public static boolean showButtonOnSelectWorld() { return showOnSelectWorld; }
    public static boolean isChangelogTabEnabled() { return true; }
}