package com.mmyddd.mcmod.changelog;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "ctnhchangelog")
public class ModConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip
    public String changelogUrl = "https://raw.githubusercontent.com/example/ctnh/main/changelog.json";

    @ConfigEntry.Gui.Tooltip
    public String modpackVersion = "1.0.0";

    @ConfigEntry.Gui.Tooltip
    public String modpackName = "Modpack";

    public boolean enableVersionCheck = true;

    public boolean showOnTitle = true;

}