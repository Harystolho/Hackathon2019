package com.harystolho.hackathon.data

interface WordRepository {

    fun readWords() : List<String>

}