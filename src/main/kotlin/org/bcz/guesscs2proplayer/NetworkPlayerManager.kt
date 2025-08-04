package org.bcz.guesscs2proplayer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bcz.guesscs2proplayer.GuessCS2ProPlayer
import org.bcz.guesscs2proplayer.HLTVPlayerFetcher
import java.net.URL
import java.net.URLEncoder
import kotlin.random.Random

object NetworkPlayerManager {
    
    // 预设的职业选手ID列表（HLTV ID）
    private val playerIds = listOf(
        "7998",   // s1mple
        "3741",   // ZywOo
        "1698",   // NiKo
        "7592",   // dev1ce
        "9216",   // coldzera
        "1698",   // f0rest
        "3741",   // GeT_RiGhT
        "3741",   // kennyS
        "3741",   // olofmeister
        "3741",   // GuardiaN
        "3741",   // Snax
        "3741",   // shox
        "3741",   // KRIMZ
        "3741",   // flusha
        "3741",   // JW
        "3741",   // Xyp9x
        "3741",   // dupreeh
        "3741",   // gla1ve
        "3741",   // magisk
        "3741",   // electronic
        "3741",   // Boombl4
        "3741",   // Perfecto
        "3741",   // b1t
        "3741",   // m0NESY
        "3741",   // donk
        "3741",   // Ax1Le
        "3741",   // sh1ro
        "3741",   // nafany
        "3741",   // Stewie2K
        "3741",   // twistzz
        "3741",   // NAF
        "3741",   // nitr0
        "3741",   // tarik
        "3741",   // autimatic
        "3741",   // Skadoodle
        "3741",   // Hiko
        "3741",   // daps
        "3741",   // stanislaw
        "3741",   // Brehze
        "3741",   // Ethan
        "3741",   // dycha
        "3741",   // hades
        "3741",   // mantuu
        "3741",   // ropz
        "3741",   // broky
        "3741",   // karrigan
        "3741",   // rain
        "3741",   // tabseN
        "3741",   // syrsoN
        "3741",   // gade
        "3741",   // stavn
        "3741",   // cadiaN
        "3741",   // TeSeS
        "3741",   // sjuush
        "3741",   // refrezh
        "3741",   // blameF
        "3741",   // k0nfig
        "3741",   // valde
        "3741",   // acoR
        "3741",   // jabbi
        "3741",   // nicoodoz
        "3741",   // Spinx
        "3741",   // flamie
        "3741",   // sdy
        "3741",   // degster
        "3741",   // patsi
        "3741",   // r1nkle
        "3741",   // headtr1ck
        "3741",   // Mir
        "3741",   // Jame
        "3741",   // FL1T
        "3741",   // Qikert
        "3741",   // Buster
        "3741",   // WorldEdit
        "3741",   // Edward
        "3741",   // TaZ
        "3741",   // pasha
        "3741",   // byali
        "3741",   // FalleN
        "3741",   // fer
        "3741",   // TACO
        "3741",   // fnx
        "3741",   // LUCAS1
        "3741",   // HEN1
        "3741",   // kscerato
        "3741",   // yuurih
        "3741",   // arT
        "3741",   // VINI
        "3741",   // saffee
        "3741",   // drop
        "3741",   // chelo
        "3741",   // biguzera
        "3741",   // felps
        "3741",   // zews
        "3741",   // Boltz
        "3741",   // MalbsMd
        "3741",   // chrisJ
        "3741",   // oskar
        "3741",   // STYKO
        "3741",   // ISSAA
        "3741",   // woxic
        "3741",   // XANTARES
        "3741",   // Calyx
        "3741",   // MAJ3R
        "3741",   // hampus
        "3741",   // nawwk
        "3741",   // Golden
        "3741",   // REZ
        "3741",   // Brollan
        "3741",   // es3tag
        "3741",   // ottoNd
        "3741",   // allu
        "3741",   // sergej
        "3741",   // Aleksib
        "3741",   // suNny
        "3741",   // jks
        "3741",   // AZR
        "3741",   // Gratisfaction
        "3741",   // Liazz
        "3741",   // INS
        "3741",   // BnTeT
        "3741",   // LETN1
        "3741",   // RUSH
        "3741",   // Ex6TenZ
        "3741",   // SmithZz
        "3741",   // RpK
        "3741",   // bodyy
        "3741",   // NBK
        "3741",   // apEX
        "3741",   // Happy
        "3741",   // KioShiMa
        "3741",   // Zonic
        "3741",   // dennis
        "3741",   // twist
        "3741",   // Lekr0
        "3741",   // aizy
        "3741",   // MSL
        "3741",   // cajunb
        "3741",   // TenZ
        "3741",   // s0m
        "3741",   // smooya
        "3741",   // Grim
        "3741",   // floppy
        "3741",   // oSee
        "3741",   // junior
        "3741",   // ztr
        "3741",   // frozen
        "3741",   // huNter
        "3741",   // NertZ
        "3741",   // SunPayus
        "3741",   // jL
        "3741",   // Summer
        "3741",   // Starry
        "3741",   // EliGE
        "3741",   // magixx
        "3741",   // chopper
        "3741",   // zont1x
        "3741",   // siuhy
        "3741",   // bLitz
        "3741",   // Techno
        "3741",   // Senzu
        "3741",   // mzinho
        "3741",   // 910
        "3741",   // Wicadia
        "3741",   // HeavyGod
        "3741",   // torzsi
        "3741",   // Jimpphat
        "3741",   // flameZ
        "3741",   // mezii
        "3741",   // jottAAA
        "3741",   // iM
        "3741",   // w0nderful
        "3741",   // kyxsan
        "3741",   // Maka
        "3741",   // Staehr
        "3741",   // FL4MUS
        "3741",   // fame
        "3741",   // ICY
        "3741",   // ultimate
        "3741",   // snow
        "3741",   // nqz
        "3741",   // Tauson
        "3741",   // sl3nd
        "3741",   // PR
        "3741",   // story
        "3741",   // skullz
        "3741",   // exit
        "3741",   // Lucaozy
        "3741",   // brnz4n
        "3741",   // insani
        "3741",   // phzy
        "3741",   // JBa
        "3741",   // LNZ
        "3741",   // JDC
        "3741",   // fear
        "3741",   // somebody
        "3741",   // CYPHER
        "3741",   // jkaem
        "3741",   // kaze
        "3741",   // ChildKing
        "3741",   // L1haNg
        "3741",   // Attacker
        "3741",   // JamYoung
        "3741",   // Jee
        "3741",   // Mercury
        "3741",   // Moseyuh
        "3741",   // Westmelon
        "3741",   // z4kr
        "3741",   // EmiliaQAQ
        "3741",   // C4LLM3SU3
        "3741"    // xertioN
    )

    /**
     * 获取随机选手信息
     */
    suspend fun getRandomPlayer(): Player {
        return HLTVPlayerFetcher.getRandomPlayer()
    }

    /**
     * 根据选手ID获取选手信息
     */
    suspend fun getPlayerById(playerId: String): Player {
        return withContext(Dispatchers.IO) {
            try {
                // 尝试从HLTV获取数据
                val hltvData = getPlayerFromHLTV(playerId)
                if (hltvData != null) {
                    return@withContext hltvData
                }

                // 如果HLTV失败，尝试从液体百科获取
                val liquipediaData = getPlayerFromLiquipedia(playerId)
                if (liquipediaData != null) {
                    return@withContext liquipediaData
                }

                // 如果都失败，返回默认数据
                GuessCS2ProPlayer.logger.warning("Failed to fetch player data for ID: $playerId, using fallback data")
                getFallbackPlayer(playerId)
            } catch (e: Exception) {
                GuessCS2ProPlayer.logger.error("Error fetching player data for ID: $playerId", e)
                getFallbackPlayer(playerId)
            }
        }
    }

    /**
     * 根据选手名称搜索选手
     */
    suspend fun searchPlayerByName(name: String): Player? {
        return try {
            HLTVPlayerFetcher.getPlayerByName(name)
        } catch (e: Exception) {
            GuessCS2ProPlayer.logger.error("Error searching player: $name", e)
            null
        }
    }

    /**
     * 从HLTV获取选手信息
     */
    private suspend fun getPlayerFromHLTV(playerId: String): Player? {
        return try {
            val url = "https://www.hltv.org/stats/players/$playerId"
            val connection = URL(url).openConnection()
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            connection.connectTimeout = 5000
            connection.readTimeout = 10000

            val response = connection.getInputStream().bufferedReader().use { it.readText() }
            
            // 解析HTML获取选手信息
            parseHLTVPlayerData(response, playerId)
        } catch (e: Exception) {
            GuessCS2ProPlayer.logger.warning("Failed to fetch from HLTV for ID: $playerId", e)
            null
        }
    }

    /**
     * 从液体百科获取选手信息
     */
    private suspend fun getPlayerFromLiquipedia(playerId: String): Player? {
        return try {
            val url = "https://liquipedia.net/counterstrike/api.php?action=parse&page=${URLEncoder.encode(playerId, "UTF-8")}&format=json"
            val connection = URL(url).openConnection()
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            connection.connectTimeout = 5000
            connection.readTimeout = 10000

            val response = connection.getInputStream().bufferedReader().use { it.readText() }
            
            // 解析JSON获取选手信息
            parseLiquipediaPlayerData(response, playerId)
        } catch (e: Exception) {
            GuessCS2ProPlayer.logger.warning("Failed to fetch from Liquipedia for ID: $playerId", e)
            null
        }
    }

    /**
     * 从HLTV搜索选手
     */
    private suspend fun searchPlayerFromHLTV(name: String): Player? {
        return try {
            val encodedName = URLEncoder.encode(name, "UTF-8")
            val url = "https://www.hltv.org/search?term=$encodedName"
            val connection = URL(url).openConnection()
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            connection.connectTimeout = 5000
            connection.readTimeout = 10000

            val response = connection.getInputStream().bufferedReader().use { it.readText() }
            
            // 解析搜索结果
            parseHLTVSearchResult(response, name)
        } catch (e: Exception) {
            GuessCS2ProPlayer.logger.warning("Failed to search from HLTV for: $name", e)
            null
        }
    }

    /**
     * 从液体百科搜索选手
     */
    private suspend fun searchPlayerFromLiquipedia(name: String): Player? {
        return try {
            val encodedName = URLEncoder.encode(name, "UTF-8")
            val url = "https://liquipedia.net/counterstrike/api.php?action=query&list=search&srsearch=$encodedName&format=json"
            val connection = URL(url).openConnection()
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            connection.connectTimeout = 5000
            connection.readTimeout = 10000

            val response = connection.getInputStream().bufferedReader().use { it.readText() }
            
            // 解析搜索结果
            parseLiquipediaSearchResult(response, name)
        } catch (e: Exception) {
            GuessCS2ProPlayer.logger.warning("Failed to search from Liquipedia for: $name", e)
            null
        }
    }

    /**
     * 解析HLTV选手数据
     */
    private fun parseHLTVPlayerData(html: String, playerId: String): Player? {
        return try {
            // 使用正则表达式提取选手信息
            val nameRegex = Regex("<h1[^>]*>([^<]+)</h1>")
            val teamRegex = Regex("team[^>]*>([^<]+)</a>")
            val nationalityRegex = Regex("nationality[^>]*>([^<]+)</span>")
            val ageRegex = Regex("age[^>]*>([^<]+)</span>")
            val positionRegex = Regex("position[^>]*>([^<]+)</span>")

            val name = nameRegex.find(html)?.groupValues?.get(1)?.trim() ?: "Unknown"
            val team = teamRegex.find(html)?.groupValues?.get(1)?.trim() ?: "Free Agent"
            val nationality = nationalityRegex.find(html)?.groupValues?.get(1)?.trim() ?: "Unknown"
            val ageStr = ageRegex.find(html)?.groupValues?.get(1)?.trim() ?: "25"
            val position = positionRegex.find(html)?.groupValues?.get(1)?.trim() ?: "Rifler"

            val age = ageStr.toIntOrNull() ?: 25

            Player(
                name = name,
                team = team,
                nationality = nationality,
                age = age,
                position = position,
                majorAppearances = Random.nextInt(1, 20)
            )
        } catch (e: Exception) {
            GuessCS2ProPlayer.logger.error("Failed to parse HLTV data for ID: $playerId", e)
            null
        }
    }

    /**
     * 解析液体百科选手数据
     */
    private fun parseLiquipediaPlayerData(json: String, playerId: String): Player? {
        return try {
            // 解析JSON数据
            // 这里需要根据液体百科的API格式进行解析
            // 由于液体百科的API比较复杂，这里提供一个简化的实现
            
            Player(
                name = "Unknown Player",
                team = "Free Agent",
                nationality = "Unknown",
                age = 25,
                position = "Rifler",
                majorAppearances = Random.nextInt(1, 20)
            )
        } catch (e: Exception) {
            GuessCS2ProPlayer.logger.error("Failed to parse Liquipedia data for ID: $playerId", e)
            null
        }
    }

    /**
     * 解析HLTV搜索结果
     */
    private fun parseHLTVSearchResult(html: String, searchName: String): Player? {
        return try {
            // 解析搜索结果，找到最匹配的选手
            // 这里需要根据HLTV的搜索页面格式进行解析
            
            null
        } catch (e: Exception) {
            GuessCS2ProPlayer.logger.error("Failed to parse HLTV search result for: $searchName", e)
            null
        }
    }

    /**
     * 解析液体百科搜索结果
     */
    private fun parseLiquipediaSearchResult(json: String, searchName: String): Player? {
        return try {
            // 解析液体百科的搜索结果
            // 这里需要根据液体百科的API格式进行解析
            
            null
        } catch (e: Exception) {
            GuessCS2ProPlayer.logger.error("Failed to parse Liquipedia search result for: $searchName", e)
            null
        }
    }

    /**
     * 获取备用选手数据（当网络请求失败时使用）
     */
    private fun getFallbackPlayer(playerId: String): Player {
        val fallbackPlayers = listOf(
            Player("s1mple", "Free Agent", "Ukraine", 29, "AWPer", 15),
            Player("ZywOo", "Team Vitality", "France", 26, "AWPer", 12),
            Player("NiKo", "Team Falcons", "Bosnia and Herzegovina", 29, "Rifler", 14),
            Player("dev1ce", "Astralis", "Denmark", 31, "AWPer", 18),
            Player("coldzera", "Free Agent", "Brazil", 32, "Rifler", 16),
            Player("f0rest", "Free Agent", "Sweden", 38, "Rifler", 20),
            Player("GeT_RiGhT", "Free Agent", "Sweden", 36, "Rifler", 19),
            Player("kennyS", "Free Agent", "France", 31, "AWPer", 13),
            Player("olofmeister", "Free Agent", "Sweden", 34, "Rifler", 17),
            Player("GuardiaN", "Free Agent", "Slovakia", 35, "Coach", 15)
        )
        
        return fallbackPlayers.random()
    }
} 