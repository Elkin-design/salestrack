package com.salestrack.util

object CsvParser {
    /**
     * Simple CSV parser that handles basic quoted strings and commas.
     * In a production app, consider a more robust multiplatform library if requirements grow.
     */
    fun parse(csvData: String): List<List<String>> {
        return csvData.lines()
            .filter { it.isNotBlank() }
            .map { line ->
                parseLine(line)
            }
    }

    private fun parseLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        
        for (char in line) {
            when {
                char == '\"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    result.add(current.toString().trim())
                    current = StringBuilder()
                }
                else -> current.append(char)
            }
        }
        result.add(current.toString().trim())
        return result
    }

    fun toCsv(headers: List<String>, rows: List<List<String>>): String {
        val sb = StringBuilder()
        sb.append(headers.joinToString(",") { "\"$it\"" })
        sb.append("\n")
        rows.forEach { row ->
            sb.append(row.joinToString(",") { "\"$it\"" })
            sb.append("\n")
        }
        return sb.toString()
    }
}
