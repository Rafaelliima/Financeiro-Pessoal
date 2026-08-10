package com.example.gmail.parser

import com.example.gmail.GmailMessageHeader

/**
 * Interface base para parsers de e-mails de instituições financeiras.
 */
interface EmailParser {
    fun parse(header: GmailMessageHeader): ParserResult
}
