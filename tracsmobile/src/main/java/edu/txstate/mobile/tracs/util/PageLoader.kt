package edu.txstate.mobile.tracs.util

import edu.txstate.mobile.tracs.AnalyticsApplication
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class PageLoader private constructor() {
    companion object {
        @JvmStatic
        val instance = PageLoader()
    }

    fun loadHtml(fileName: String): String {
        val input: InputStream = AnalyticsApplication.getContext().assets.open(fileName)
        val reader = BufferedReader(InputStreamReader(input))
        val sb = StringBuilder()
        val ls = System.getProperty("line.separator")

        var line: String?

        line = reader.readLine()
        while (line != null) {
            sb.append(line)
            sb.append(ls)
            line = reader.readLine()
        }
        reader.close()
        return sb.toString()
    }
}