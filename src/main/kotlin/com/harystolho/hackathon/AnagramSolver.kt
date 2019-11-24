package com.harystolho.hackathon

import com.harystolho.hackathon.data.WordRepository
import com.sun.corba.se.impl.orbutil.closure.Future
import mu.KotlinLogging
import java.sql.Time
import java.util.*
import java.util.concurrent.*
import kotlin.Comparator

private val logger = KotlinLogging.logger { }

class AnagramSolver(private val wordRepository: WordRepository) {

    //private lateinit var processedPhrase : String
    private val threadPool by lazy { Executors.newFixedThreadPool(6) }

    /**
     * @throws IllegalArgumentException if the given [phrase] is not accepted by this solver
     */
    fun findAnagrams(phrase: String): List<String> {
        if (phrase.isEmpty()) return emptyList()

        val phraseToProcess = formatPhrase(phrase)

        verifyPhrase(phraseToProcess)

        val validWords = wordRepository.readWords()
        logger.info { "${validWords.size} valid words" }

        val possibleWords = removeInvalidWords(phraseToProcess, validWords)
        logger.info { "${possibleWords.size} possible words" }

        val noDuplicateChars = removeDuplicateChars(phraseToProcess, possibleWords)
        logger.info { "${noDuplicateChars.size} possible words" }

        val orderedWords = noDuplicateChars.sortedWith(Comparator { a, b ->
            if (a.length == b.length) return@Comparator 0
            if (a.length < b.length) -1 else 1
        })

        orderedWords.forEach { println(it) }

        val anagramBuilder = AnagramBuilder(phraseToProcess, orderedWords)

        val futures = noDuplicateChars.mapIndexed { idx, word ->
            threadPool.submit { anagramBuilder.build(idx, mutableListOf(word)) }
        }

        for (f in futures) f.get() // Await for tasks to finish

        threadPool.shutdown()

        val result = anagramBuilder.result

        logger.info { "${result.size} possible words" }

        return result
    }

    private fun removeInvalidWords(phrase: String, validWords: List<String>): List<String> {
        val distinctPhrase = phrase.toCharArray().distinct() // Keep only distinct chars

        return validWords.filter { word ->
            word.toCharArray().distinct().forEach { char ->
                if (!distinctPhrase.contains(char)) return@filter false
            }

            true
        }
    }

    private fun removeDuplicateChars(phrase: String, validWords: List<String>): List<String> {
        return validWords.filter { word ->
            val equalChars = phrase.filter { word.contains(it) }

            if (equalChars.length < word.length) return@filter false

            val phraseChars = phrase.map { char -> char }.toMutableList()

            word.forEach { char -> if (!phraseChars.remove(char)) return@filter false }

            true
        }
    }

    private fun formatPhrase(phrase: String): String {
        return phrase
                .replace(" ", "")
                .toUpperCase()
    }

    /**
     * @throws IllegalArgumentException if the given [phrase] contains an invalid character
     */
    private fun verifyPhrase(phrase: String) {
        val upperCasePhrase = phrase.toUpperCase()

        val chars = upperCasePhrase.toByteArray()

        chars.forEach { char ->
            // 'A' is 65 and 'Z' is 90 in the ASCII table
            if (char < 65 || char > 90) throw IllegalArgumentException("Invalid character")
        }
    }

}

private class AnagramBuilder(private val phrase: String, private val dictionary: List<String>) {

    val result = Collections.synchronizedList(mutableListOf<String>())

    private val actualResult = phrase.map { char -> char }.sorted().joinToString(separator = "")
    private val phraseLength = phrase.length

    fun build(position: Int, builtSoFar: MutableList<String>) {
        val builtSoFarLength = builtSoFar.sumBy { it.length }

        if (builtSoFarLength >= phrase.length) {
            val possibleResult = builtSoFar.flatMap { it.map { char -> char } }.sorted().joinToString(separator = "")
            // println("possible: $possibleResult")

            if (possibleResult == actualResult) {
                //     println("actual: $actualResult")
                builtSoFar.sortWith(Comparator { a, b -> a.compareTo(b) })
                result.add(builtSoFar.joinToString(separator = " "))
            }

            return
        }

        for (i in position until dictionary.size) {
            val otherWord = dictionary[i]

            if (builtSoFarLength + otherWord.length <= phraseLength) {
                val clone = builtSoFar.toMutableList().apply { add(otherWord) }
                build(i + 1, clone)
            } else {
                break
            }
        }
    }

}