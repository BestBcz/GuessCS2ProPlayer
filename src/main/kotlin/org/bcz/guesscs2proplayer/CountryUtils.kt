package org.bcz.guesscs2proplayer.utils

import org.bcz.guesscs2proplayer.GuessCS2ProPlayer
import org.jetbrains.skia.Data
import org.jetbrains.skia.svg.SVGDOM
import java.io.File

object CountryUtils {
    val countryContinents = mapOf(
        // 欧洲
        "ba" to "Europe", "dk" to "Europe", "sk" to "Europe", "pl" to "Europe", "be" to "Europe",
        "nl" to "Europe", "de" to "Europe", "fr" to "Europe", "se" to "Europe", "no" to "Europe",
        "fi" to "Europe", "gb" to "Europe", "es" to "Europe", "it" to "Europe", "pt" to "Europe",
        "at" to "Europe", "ch" to "Europe", "cz" to "Europe", "hu" to "Europe", "ro" to "Europe",
        "bg" to "Europe", "rs" to "Europe", "hr" to "Europe", "si" to "Europe", "mk" to "Europe",
        "al" to "Europe", "me" to "Europe", "ee" to "Europe", "lv" to "Europe", "lt" to "Europe",
        "ie" to "Europe", "is" to "Europe", "gr" to "Europe", "cy" to "Europe",
        // 独联体 (CIS)
        "ru" to "CIS", "ua" to "CIS", "by" to "CIS", "kz" to "CIS", "uz" to "CIS",
        "tm" to "CIS", "kg" to "CIS", "tj" to "CIS", "am" to "CIS", "az" to "CIS",
        "ge" to "CIS", "md" to "CIS",
        // 北美洲
        "us" to "North America", "ca" to "North America", "mx" to "North America", "gt" to "North America",
        // 南美洲
        "br" to "South America", "ar" to "South America", "cl" to "South America", "pe" to "South America",
        "co" to "South America", "ve" to "South America", "bo" to "South America", "py" to "South America",
        "uy" to "South America", "ec" to "South America", "gy" to "South America", "sr" to "South America",
        // 亚洲（包含中东和大洋洲）
        "tr" to "Asia", "il" to "Asia", "cn" to "Asia", "jp" to "Asia", "kr" to "Asia",
        "au" to "Asia", "nz" to "Asia", "in" to "Asia", "id" to "Asia", "ph" to "Asia",
        "th" to "Asia", "vn" to "Asia", "my" to "Asia", "sg" to "Asia", "sa" to "Asia",
        "ae" to "Asia", "qa" to "Asia", "kw" to "Asia", "bh" to "Asia", "om" to "Asia",
        "ye" to "Asia", "jo" to "Asia", "lb" to "Asia", "sy" to "Asia", "iq" to "Asia",
        "ir" to "Asia", "pk" to "Asia", "af" to "Asia", "bd" to "Asia", "lk" to "Asia",
        "np" to "Asia", "bt" to "Asia", "mm" to "Asia", "kh" to "Asia", "la" to "Asia",
        "mn" to "Asia", "pg" to "Asia", "fj" to "Asia"
    )

    val countryToCode = mapOf(
        "Bosnia and Herzegovina" to "ba", "Denmark" to "dk", "Slovakia" to "sk", "Poland" to "pl", "Belgium" to "be",
        "Netherlands" to "nl", "Germany" to "de", "France" to "fr", "Sweden" to "se", "Norway" to "no",
        "Finland" to "fi", "United Kingdom" to "gb", "Spain" to "es", "Italy" to "it", "Portugal" to "pt",
        "Austria" to "at", "Switzerland" to "ch", "Czech Republic" to "cz", "Czechia" to "cz", "Hungary" to "hu", "Romania" to "ro",
        "Bulgaria" to "bg", "Serbia" to "rs", "Croatia" to "hr", "Slovenia" to "si", "North Macedonia" to "mk",
        "Albania" to "al", "Montenegro" to "me", "Estonia" to "ee", "Latvia" to "lv", "Lithuania" to "lt",
        "Ireland" to "ie", "Iceland" to "is", "Greece" to "gr", "Cyprus" to "cy",
        "Russia" to "ru", "Ukraine" to "ua", "Belarus" to "by", "Kazakhstan" to "kz", "Uzbekistan" to "uz",
        "Turkmenistan" to "tm", "Kyrgyzstan" to "kg", "Tajikistan" to "tj", "Armenia" to "am", "Azerbaijan" to "az",
        "Georgia" to "ge", "Moldova" to "md",
        "United States" to "us", "Canada" to "ca", "Mexico" to "mx", "Guatemala" to "gt",
        "Brazil" to "br", "Argentina" to "ar", "Chile" to "cl", "Peru" to "pe", "Colombia" to "co",
        "Venezuela" to "ve", "Bolivia" to "bo", "Paraguay" to "py", "Uruguay" to "uy", "Ecuador" to "ec",
        "Guyana" to "gy", "Suriname" to "sr",
        "Turkey" to "tr", "Israel" to "il", "China" to "cn", "Japan" to "jp", "South Korea" to "kr",
        "Australia" to "au", "New Zealand" to "nz", "India" to "in", "Indonesia" to "id", "Philippines" to "ph",
        "Thailand" to "th", "Vietnam" to "vn", "Malaysia" to "my", "Singapore" to "sg", "Saudi Arabia" to "sa",
        "United Arab Emirates" to "ae", "Qatar" to "qa", "Kuwait" to "kw", "Bahrain" to "bh", "Oman" to "om",
        "Yemen" to "ye", "Jordan" to "jo", "Lebanon" to "lb", "Syria" to "sy", "Iraq" to "iq",
        "Iran" to "ir", "Pakistan" to "pk", "Afghanistan" to "af", "Bangladesh" to "bd", "Sri Lanka" to "lk",
        "Nepal" to "np", "Bhutan" to "bt", "Myanmar" to "mm", "Cambodia" to "kh", "Laos" to "la",
        "Mongolia" to "mn", "Papua New Guinea" to "pg", "Fiji" to "fj",
        "South Africa" to "za", "Nigeria" to "ng", "Egypt" to "eg", "Kenya" to "ke", "Ghana" to "gh",
        "Algeria" to "dz", "Morocco" to "ma", "Tunisia" to "tn", "Uganda" to "ug", "Ethiopia" to "et"
    )

    fun loadSVGFromFile(dataFolder: File, nationality: String): SVGDOM {
        val flagsDir = File(dataFolder, "flags")
        GuessCS2ProPlayer.logger.info("Attempting to load flag for nationality: $nationality")
        GuessCS2ProPlayer.logger.info("Flags directory: ${flagsDir.absolutePath}")

        val countryCode = countryToCode[nationality] ?: run {
            GuessCS2ProPlayer.logger.warning("No country code mapping found for nationality: $nationality, using lowercase as fallback")
            nationality.lowercase().replace(" ", "_")
        }
        GuessCS2ProPlayer.logger.info("Mapped country code: $countryCode")

        if (!flagsDir.exists()) {
            GuessCS2ProPlayer.logger.warning("Flags directory does not exist, creating: ${flagsDir.absolutePath}")
            flagsDir.mkdirs()
        }

        val svgFile = File(flagsDir, "$countryCode.svg")
        GuessCS2ProPlayer.logger.info("Looking for SVG file: ${svgFile.absolutePath}")

        if (!svgFile.exists()) {
            GuessCS2ProPlayer.logger.error("SVG file not found: ${svgFile.absolutePath}")
            throw IllegalStateException("SVG file not found: ${svgFile.absolutePath}")
        }

        if (!svgFile.canRead()) {
            GuessCS2ProPlayer.logger.error("Cannot read SVG file: ${svgFile.absolutePath}")
            throw IllegalStateException("Cannot read SVG file: ${svgFile.absolutePath}")
        }

        try {
            val bytes = svgFile.readBytes()
            GuessCS2ProPlayer.logger.info("Successfully read SVG file: ${svgFile.absolutePath}, size: ${bytes.size} bytes")
            return SVGDOM(Data.makeFromBytes(bytes))
        } catch (e: Exception) {
            GuessCS2ProPlayer.logger.error("Failed to parse SVG file: ${svgFile.absolutePath}, error: ${e.message}", e)
            throw e
        }
    }
}