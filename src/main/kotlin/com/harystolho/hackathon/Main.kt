package com.harystolho.hackathon

import com.harystolho.hackathon.data.WordRepository
import com.harystolho.hackathon.data.WordRepositoryImpl

class Main {

    private lateinit var wordRepository: WordRepository

    fun run() {
        initDependencies()

        val solver = AnagramSolver(wordRepository)

        try {
            val phrase = readPhraseFromConsole()
            val anagrams = solver.findAnagrams(phrase)
            anagrams.forEach { println(it) }
        } catch (e: IllegalArgumentException) {
            println("A palavra digitada nao é aceita por este programa")
        }
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