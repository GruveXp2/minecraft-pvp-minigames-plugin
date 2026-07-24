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

    fun loadPresetsFromFile() {
        val file = folder.resolve("battlepresets.json")
        if (!file.exists()) return

        val json = file.readText()

        val type = object : TypeToken<Set<BattlePreset>>() {}.type
        val loadedPresets: Set<BattlePreset> = gson.fromJson(json, type)

        battlePresets.clear()
        loadedPresets.forEach { preset -> battlePresets[preset.name.lowercase()] = preset } //TODO: add eget id felt istedenfor name.lowercase()
    }

    fun savePresetsToFile() {
        if (battlePresets.isEmpty()) return // if something wrong happened when loading such that the presets werent loaded in, dont save
        if (!folder.exists()) {
            folder.mkdirs()
        }

        val file = folder.resolve("battlepresets.json")

        try {
            val json = gson.toJson(battlePresets.values)
            file.writeText(json)

            Main.getPlugin().logger.info("Saved ${battlePresets.size} presets to battlepresets.json")
        } catch (e: Exception) {
            Main.getPlugin().logger.severe("Error when saving presets: ${e.message}")
            e.printStackTrace()
        }
    }

    fun addPreset(battlePreset: BattlePreset): Boolean {
        val id = battlePreset.name.lowercase() //TODO: add eget id felt istedenfor name.lowercase() (da vil navnet kunna ha mellomrom og store bokstaver, mens iden vil kun ha små bokstaver og bruke _)

        if (battlePresets.containsKey(id)) {
            return false
        }

        battlePresets[id] = battlePreset
        savePresetsToFile()
        return true
    }

    fun getPreset(presetName: String): BattlePreset? {
        return battlePresets[presetName.lowercase()] //TODO: add eget id felt istedenfor name.lowercase()
    }

    fun getPresets(): Set<BattlePreset> {
        return battlePresets.values.toSet()
    }

    fun getPresetNames(): MutableSet<String> {
        return battlePresets.keys;
    }
}