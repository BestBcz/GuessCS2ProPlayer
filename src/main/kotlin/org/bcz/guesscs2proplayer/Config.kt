package org.bcz.guesscs2proplayer

import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.Properties

object Config {
    private const val CONFIG_FILE = "config.properties"
    private val properties = Properties()
    private var configFile: File? = null

    // 默认配置
    private var networkModeEnabled: Boolean = false
    private var hltvApiEnabled: Boolean = true
    private var liquipediaApiEnabled: Boolean = true
    private var requestTimeout: Int = 10000
    private var maxRetries: Int = 3

    fun initialize(dataFolder: File) {
        configFile = File(dataFolder, CONFIG_FILE)
        loadConfig()
    }

    private fun loadConfig() {
        try {
            if (configFile?.exists() == true) {
                FileReader(configFile!!).use { reader ->
                    properties.load(reader)
                }
            }
            
            // 加载配置值
            networkModeEnabled = properties.getProperty("network.mode.enabled", "false").toBoolean()
            hltvApiEnabled = properties.getProperty("hltv.api.enabled", "true").toBoolean()
            liquipediaApiEnabled = properties.getProperty("liquipedia.api.enabled", "true").toBoolean()
            requestTimeout = properties.getProperty("request.timeout", "10000").toInt()
            maxRetries = properties.getProperty("max.retries", "3").toInt()
            
            GuessCS2ProPlayer.logger.info("Configuration loaded: networkMode=$networkModeEnabled, hltvApi=$hltvApiEnabled, liquipediaApi=$liquipediaApiEnabled")
        } catch (e: Exception) {
            GuessCS2ProPlayer.logger.error("Failed to load configuration", e)
        }
    }

    private fun saveConfig() {
        try {
            properties.setProperty("network.mode.enabled", networkModeEnabled.toString())
            properties.setProperty("hltv.api.enabled", hltvApiEnabled.toString())
            properties.setProperty("liquipedia.api.enabled", liquipediaApiEnabled.toString())
            properties.setProperty("request.timeout", requestTimeout.toString())
            properties.setProperty("max.retries", maxRetries.toString())
            
            FileWriter(configFile!!).use { writer ->
                properties.store(writer, "CS2 Guess Pro Player Configuration")
            }
            
            GuessCS2ProPlayer.logger.info("Configuration saved successfully")
        } catch (e: Exception) {
            GuessCS2ProPlayer.logger.error("Failed to save configuration", e)
        }
    }

    fun isNetworkModeEnabled(): Boolean = networkModeEnabled

    fun setNetworkModeEnabled(enabled: Boolean) {
        networkModeEnabled = enabled
        saveConfig()
    }

    fun isHLTVApiEnabled(): Boolean = hltvApiEnabled

    fun setHLTVApiEnabled(enabled: Boolean) {
        hltvApiEnabled = enabled
        saveConfig()
    }

    fun isLiquipediaApiEnabled(): Boolean = liquipediaApiEnabled

    fun setLiquipediaApiEnabled(enabled: Boolean) {
        liquipediaApiEnabled = enabled
        saveConfig()
    }

    fun getRequestTimeout(): Int = requestTimeout

    fun setRequestTimeout(timeout: Int) {
        requestTimeout = timeout
        saveConfig()
    }

    fun getMaxRetries(): Int = maxRetries

    fun setMaxRetries(retries: Int) {
        maxRetries = retries
        saveConfig()
    }

    fun getConfigSummary(): String {
        return buildString {
            appendLine("📋 配置信息：")
            appendLine("• 网络模式：${if (networkModeEnabled) "✅ 开启" else "❌ 关闭"}")
            appendLine("• HLTV API：${if (hltvApiEnabled) "✅ 启用" else "❌ 禁用"}")
            appendLine("• 液体百科 API：${if (liquipediaApiEnabled) "✅ 启用" else "❌ 禁用"}")
            appendLine("• 请求超时：${requestTimeout}ms")
            appendLine("• 最大重试：${maxRetries}次")
        }
    }
} 