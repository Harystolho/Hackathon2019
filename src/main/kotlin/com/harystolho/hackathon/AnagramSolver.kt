package com.harystolho.hackathon

import com.harystolho.hackathon.data.WordRepository
import kotlinx.coroutines.*
import mu.KotlinLogging
import java.util.*
import java.util.concurrent.*
import kotlin.Comparator
import kotlin.coroutines.coroutineContext

private val logger = KotlinLogging.logger { }

class AnagramSolver(private val wordRepository: WordRepository) {

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

        val result = runAnagramBuilder(phraseToProcess, orderedWords)

        logger.info { "${result.size} possible words" }

        return result
    }

    /**
     * Removes words that have characters not present in [phrase]. If the phrase is 'car', the word
     * 'house' is removed because 'car' doesn't contain 'h'
     */
    private fun removeInvalidWords(phrase: String, validWords: List<String>): List<String> {
        val distinctPhrase = phrase.toCharArray().distinct() // Keep only distinct chars

        return validWords.filter { word ->
            word.toCharArray().distinct().forEach { char ->
                if (!distinctPhrase.contains(char)) return@filter false
            }

            true
        }
    }

    /**
     * Removes words that have the same chars as [phrase] but in greater amount. If the phrase is
     * 'maracuja azedo', the word 'armour' is removed because even though 'maracuja azedo' has all
     * chars 'armour' has, 'armour' has 2 'r' and 'maracuja azedo' has only 1
     */
    private fun removeDuplicateChars(phrase: String, validWords: List<String>): List<String> {
        return validWords.filter { word ->
            val equalChars = phrase.filter { word.contains(it) }

            if (equalChars.length < word.length) return@filter false

            val phraseChars = phrase.map { char -> char }.toMutableList()

            word.forEach { char -> if (!phraseChars.remove(char)) return@filter false }

            true
        }
    }

    /**
     * Executes [AnagramBuilder] using concurrency to improve the time it takes to find all anagrams
     */
    private fun runAnagramBuilder(phrase: String, orderedWords: List<String>): List<String> {
        val anagramBuilder = AnagramBuilder(phrase, orderedWords)

        runBlocking {
            withContext(Dispatchers.Default){
                orderedWords.forEachIndexed{ idx, word ->
                    launch { anagramBuilder.build(idx, mutableListOf(word)) }
                }
            }
        }

        return anagramBuilder.result
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

/**
 * This class is Thread safe
 */
private class AnagramBuilder(phrase: String, private val dictionary: List<String>) {

    val result: MutableList<String> = Collections.synchronizedList(mutableListOf<String>())

    private val actualResult = phrase.map { char -> char }.sorted().joinToString(separator = "")
    private val phraseLength = phrase.length
    private val phraseCharCount = phrase.groupBy { it }.entries.associate { it.key to it.value.size }
    private val dictionarySize = dictionary.size

    fun build(position: Int, builtSoFar: MutableList<String>) {
        val builtSoFarLength = builtSoFar.sumBy { it.length }

        if (builtSoFarLength >= phraseLength) {
            val possibleResult = builtSoFar.flatMap { it.map { char -> char } }.sorted().joinToString(separator = "")

            if (possibleResult == actualResult) {
                builtSoFar.sortWith(Comparator { a, b -> a.compareTo(b) })
                result.add(builtSoFar.joinToString(separator = " "))
            }

            return
        }

        for (i in position until dictionarySize) {
            val nextWord = dictionary[i]

            // The [dictionary] is a list ordered by word length, if adding [nextWord.length] to
            // [builtSoFarLength] results in a number greater than [phraseLength], all words
            // after [nextWord] will also result in greater number
            if (builtSoFarLength + nextWord.length <= phraseLength) {
                val clone = builtSoFar.toMutableList().apply { add(nextWord) }

                val builtSoFarCharCount = clone.joinToString(separator = "")
                        .groupBy { char -> char }.entries.associate { it.key to it.value.size }

                var skip = false

                for (entry in builtSoFarCharCount.entries) {
                    if (entry.value > phraseCharCount[entry.key] ?: 0) {
                        skip = true
                        break
                    }
                }

                if (!skip)
                    build(i + 1, clone)
            } else {
                break
            }
        }
    }

}