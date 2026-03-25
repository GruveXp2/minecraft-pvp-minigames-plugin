package gruvexp.bbminigames.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import gruvexp.bbminigames.Main
import gruvexp.bbminigames.model.preset.BattlePreset
import java.io.File

class BattlePresetService {
    private val battlePresets: MutableMap<String, BattlePreset> = mutableMapOf()// Her definerer vi Gson-objektet (det er dette vi bruker til alt)
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()
    private val folder = File(Main.getPlugin().dataFolder, "presets")
    var totalPresets: Int = 0

    fun loadPresetsFromFile() {
        val file = folder.resolve("battlepresets.json")
        if (!file.exists()) return

        val json = file.readText()

        // Vi forteller Gson: "Dette er en Map med String som nøkkel og BattlePreset som verdi"
        val type = object : TypeToken<Map<String, BattlePreset>>() {}.type
        val loadedPresets: Map<String, BattlePreset> = gson.fromJson(json, type)

        battlePresets.clear()
        battlePresets.putAll(loadedPresets)
    }

    fun savePresetsToFile() {
        if (battlePresets.isEmpty()) return // if something wrong happened when loading such that the presets werent loaded in, dont save
        if (!folder.exists()) {
            folder.mkdirs()
        }

        val file = folder.resolve("battlepresets.json")

        try {
            val json = gson.toJson(battlePresets)
            file.writeText(json)

            Main.getPlugin().logger.info("Saved ${battlePresets.size} presets to battlepresets.json")
        } catch (e: Exception) {
            Main.getPlugin().logger.severe("Error when saving presets: ${e.message}")
            e.printStackTrace()
        }
    }

    fun addPreset(battlePreset: BattlePreset): Boolean {
        val id = battlePreset.name.lowercase()

        if (battlePresets.containsKey(id)) {
            return false
        }

        battlePresets[id] = battlePreset
        savePresetsToFile()
        return true
    }
}