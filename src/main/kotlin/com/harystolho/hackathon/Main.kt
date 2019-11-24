package com.harystolho.hackathon

import com.harystolho.hackathon.data.WordRepository
import com.harystolho.hackathon.data.WordRepositoryImpl
import mu.KotlinLogging
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger { }

class Main {

    private lateinit var wordRepository: WordRepository

    fun run() {
        var elapsedTime = 0L
        initDependencies()

        val solver = AnagramSolver(wordRepository)

        val phrase = readPhraseFromConsole()
        var anagrams = emptySet<String>()

        try {
            elapsedTime = measureTimeMillis {
                anagrams = solver.findAnagrams(phrase)
            }
        } catch (e: IllegalArgumentException) {
            println("A palavra digitada nao é aceita por este programa")
        }

        anagrams.forEach { println(it) }

        logger.info { "Elapsed Time: ${elapsedTime / 1000.0}s" }
    }

    private fun initDependencies() {
        wordRepository = WordRepositoryImpl()
    }

    private fun readPhraseFromConsole(): String {
        print("Digite uma frase: ")
        return readLine() ?: "" // TODO read accentuated chars
    }

}

fun main() {
    Main().run()
}