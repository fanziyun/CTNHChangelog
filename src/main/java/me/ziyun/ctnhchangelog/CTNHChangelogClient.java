package me.ziyun.ctnhchangelog;

import com.mmyddd.mcmod.changelog.ModConfig; // 确保 import 了正确的配置类
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CTNHChangelogClient implements ClientModInitializer {
    public static ModConfig config;
    public static final String MOD_ID = "ctnhchangelog";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("CTNH Changelog Fabric 版正在初始化...");

        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        LOGGER.info("Cloth Config 注册完成！");

    }

}