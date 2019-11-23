package com.harystolho.hackathon

import com.harystolho.hackathon.data.WordRepository
import mu.KotlinLogging

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

        val result = mutableListOf<String>()

        return noDuplicateChars
        //return emptyList()
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

    companion object {
        private val VOWELS = listOf('A', 'E', 'I', 'O', 'U')
    }

}