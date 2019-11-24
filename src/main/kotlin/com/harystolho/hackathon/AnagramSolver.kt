package com.harystolho.hackathon

import com.harystolho.hackathon.data.WordRepository
import mu.KotlinLogging
import kotlin.Comparator

private val logger = KotlinLogging.logger { }

class AnagramSolver(private val wordRepository: WordRepository) {

    //private lateinit var processedPhrase : String

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

        val anagramBuilder = AnagramBuilder(phraseToProcess, orderedWords)

        noDuplicateChars.forEachIndexed { idx, word ->
            anagramBuilder.build(idx, mutableListOf(word))
        }

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

    val result = mutableListOf<String>()

    fun build(position: Int, builtSoFar: MutableList<String>) {
        if (position >= dictionary.size - 1) return

        val builtSoFarLength = builtSoFar.sumBy { it.length }

        if (builtSoFarLength >= phrase.length) {
            val possibleResult = builtSoFar.flatMap { it.map { char -> char } }.sorted().joinToString(separator = "")
            val actualResult = phrase.map { it }.sorted().joinToString(separator = "")

            if (possibleResult == actualResult) {
                builtSoFar.sortWith(Comparator { a, b -> a.compareTo(b) })
                result.add(builtSoFar.joinToString(separator = " "))
                return
            }
        }

        for (i in position until dictionary.size) {
            val otherWord = dictionary[i]

            if (builtSoFarLength + otherWord.length <= phrase.length) {
                val clone = builtSoFar.toMutableList().apply { add(otherWord) }
                build(i, clone)
            } else {
                break
            }
        }
    }

}