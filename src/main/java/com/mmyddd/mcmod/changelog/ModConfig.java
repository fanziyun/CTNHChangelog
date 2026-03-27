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

    // 外部链接配置 - 直接放在这里
    @ConfigEntry.Gui.Tooltip
    public String externalLinkName = "项目主页";

    @ConfigEntry.Gui.Tooltip
    public String externalLinkUrl = "https://github.com/FanZiyun";

    // 提供给 Screen 调用的接口方法
    public String getExternalLinkName() {
        return (externalLinkName == null || externalLinkName.isEmpty()) ? "Link" : externalLinkName;
    }

    public String getExternalLinkUrl() {
        return (externalLinkUrl == null || externalLinkUrl.isEmpty()) ? "" : externalLinkUrl;
    }
}