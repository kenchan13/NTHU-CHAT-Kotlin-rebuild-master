package com.google.firebase.codelab.nthuchat

import java.net.Inet4Address
import java.sql.Time
import java.util.*

class Schedule(
        val name: String,
        val startDate: String,
        val startTime: String,
        val endDate: String,
        val endTime: String,
        val address: String,
        val notes: String,
        val createrID: String,
        val type: String
)