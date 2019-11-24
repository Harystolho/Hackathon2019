package com.harystolho.hackathon.data

import mu.KotlinLogging
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.stream.Collectors

private val logger = KotlinLogging.logger { }

class WordRepositoryImpl : WordRepository {

    override fun readWords(): List<String> {
        var lines = emptyList<String>()

        try {
            val inputStream = this.javaClass::class.java.getResourceAsStream(FILE_NAME)
            val reader = InputStreamReader(inputStream)
            val bufferedReader = BufferedReader(reader)

            bufferedReader.use { br ->
                lines = br.lines().collect(Collectors.toList())
            }
        } catch (e: NullPointerException) {
            logger.error(e) { "Error reading words from file" }
            return emptyList()
        }

        return lines
    }

    companion object {
        private const val FILE_NAME = "/palavras.txt"
    }

}