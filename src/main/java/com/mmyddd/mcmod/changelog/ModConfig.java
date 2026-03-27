package com.mmyddd.mcmod.changelog;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "ctnhchangelog")
public class ModConfig implements ConfigData {
    public String changelogUrl = "https://example.com/changelog.json";
    public String modpackVersion = "0.3.3";
    public String modpackName = "Modpack";
    public boolean enableVersionCheck = true;
    public boolean showOnTitle = true;
    public boolean showOnSelectWorld = false;

    public int versionYOffset = 20;
    public int buttonYOffset = 160;

    public String externalLinkName = "项目主页";
    public String externalLinkUrl = "https://github.com/FanZiyun";

    public String getExternalLinkName() {
        return externalLinkName != null ? externalLinkName : "项目主页";
    }

    public String getExternalLinkUrl() {
        return externalLinkUrl;
    }
}