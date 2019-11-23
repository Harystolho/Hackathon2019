package com.harystolho.hackathon.data

import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.streams.toList

class WordRepositoryImpl : WordRepository {

    override fun readWords(): List<String> {
        var lines = emptyList<String>()

        val inputStream = this.javaClass::class.java.getResourceAsStream(FILE_NAME)
        val reader = InputStreamReader(inputStream)
        val bufferedReader = BufferedReader(reader)

        bufferedReader.use { br ->
            lines = br.lines().toList()
        }

        return lines
    }

    companion object {
        private const val FILE_NAME = "palavras.txt"
    }

}