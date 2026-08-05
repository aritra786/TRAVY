package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.room.Room
import com.example.data.local.TravyDatabase
import com.example.data.repository.TravyRepository
import com.example.navigation.TravyAppNavigation
import com.example.ui.theme.TravyTheme

class MainActivity : ComponentActivity() {

    private lateinit var database: TravyDatabase
    private lateinit var repository: TravyRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = Room.databaseBuilder(
            applicationContext,
            TravyDatabase::class.java,
            "travy_database"
        ).fallbackToDestructiveMigration().build()

        repository = TravyRepository(dao = database.travyDao())

        setContent {
            TravyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TravyAppNavigation(repository = repository)
                }
            }
        }
    }
}
