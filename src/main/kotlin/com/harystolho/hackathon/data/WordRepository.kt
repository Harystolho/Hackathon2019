package com.harystolho.hackathon.data

interface WordRepository {

    /**
     * @return the list of words read or an empty list if an exception occurred
     */
    fun readWords() : List<String>

}