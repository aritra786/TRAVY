package com.example.data.repository

import com.example.data.local.TravyDao
import com.example.data.remote.GeminiRemoteDataSource

/**
 * TravyRepository maintains backward compatibility across the application
 * while inheriting data operations from [AppRepository].
 */
class TravyRepository(
    dao: TravyDao,
    geminiSource: GeminiRemoteDataSource = GeminiRemoteDataSource()
) : AppRepository(dao, geminiSource)
