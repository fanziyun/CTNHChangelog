package com.mmyddd.mcmod.changelog;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "ctnhchangelog")
public class ModConfig implements ConfigData {
    public String changelogUrl = "https://example.com/changelog.json";
    public String modpackVersion = "1.0.0";
    public boolean enableVersionCheck = true;
    public boolean showOnTitle = true;
    public boolean showOnSelectWorld = true;
}