package com.yelloelefant.compx551a4.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

class SessionRepository(private val context: Context) {
    private val gson = Gson()
    private val sessionsFile: File
        get() = File(context.filesDir, "polar_h10_sessions.json")

    private val _sessions = MutableStateFlow<List<SessionEntity>>(emptyList())
    val sessions: StateFlow<List<SessionEntity>> = _sessions.asStateFlow()

    suspend fun loadSessions(): List<SessionEntity> = withContext(Dispatchers.IO) {
        if (!sessionsFile.exists()) {
            _sessions.value = emptyList()
            return@withContext emptyList()
        }
        try {
            val json = sessionsFile.readText()
            val type = object : TypeToken<List<SessionEntity>>() {}.type
            val list: List<SessionEntity> = gson.fromJson(json, type) ?: emptyList()
            val sortedList = list.sortedByDescending { it.startTimeMs }
            _sessions.value = sortedList
            sortedList
        } catch (e: Exception) {
            e.printStackTrace()
            _sessions.value = emptyList()
            emptyList()
        }
    }

    suspend fun saveSession(session: SessionEntity) = withContext(Dispatchers.IO) {
        val currentList = _sessions.value.toMutableList()
        currentList.removeAll { it.id == session.id }
        currentList.add(0, session)
        writeListToDisk(currentList)
        _sessions.value = currentList
    }

    suspend fun deleteSession(id: String) = withContext(Dispatchers.IO) {
        val currentList = _sessions.value.toMutableList()
        currentList.removeAll { it.id == id }
        writeListToDisk(currentList)
        _sessions.value = currentList
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        writeListToDisk(emptyList())
        _sessions.value = emptyList()
    }

    private fun writeListToDisk(list: List<SessionEntity>) {
        try {
            val json = gson.toJson(list)
            sessionsFile.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
