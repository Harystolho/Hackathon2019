package com.harystolho.hackathon

import com.harystolho.hackathon.data.WordRepository
import com.harystolho.hackathon.data.WordRepositoryImpl
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

class Main {

    private lateinit var wordRepository: WordRepository

    fun run() {
        initDependencies()

        val solver = AnagramSolver(wordRepository)

        var phrase = ""

        try {
            phrase = readPhraseFromConsole()
        } catch (e: IllegalArgumentException) {
            println("A palavra digitada nao é aceita por este programa")
        }

        val startTime = System.currentTimeMillis()

        val anagrams = solver.findAnagrams(phrase)
        anagrams.forEach { println(it) }

        logger.info { "End: ${(System.currentTimeMillis() - startTime) / 1000.0}s" }
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
// TODO handle exceptions