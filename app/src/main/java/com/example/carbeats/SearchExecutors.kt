package com.example.carbeats

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object SearchExecutors {
    val io: ExecutorService = Executors.newFixedThreadPool(2)
}