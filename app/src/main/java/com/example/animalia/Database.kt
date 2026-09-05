package com.example.animalia

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "species")
data class Species(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val emoji: String,
    val piComm: Double
)

@Entity(tableName = "vocalizations")
data class Vocalization(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val speciesId: Int,
    val signalType: String,
    val aAcoustic: Double,
    val bContext: Double,
    val cIntent: Double,
    val decodedMeaning: String
) {
    val starlingNorm: Double
        get() = Math.sqrt((aAcoustic * aAcoustic) + (bContext * bContext) + (0.1 * cIntent * cIntent))

    val confidence: Double
        get() = (aAcoustic * aAcoustic) / (starlingNorm * starlingNorm)
}

@Dao
interface AnimalDao {
    @Query("SELECT * FROM species ORDER BY piComm DESC")
    fun getAllSpecies(): Flow<List<Species>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpecies(species: List<Species>)

    @Query("SELECT * FROM vocalizations WHERE speciesId = :speciesId")
    fun getVocalizationsForSpecies(speciesId: Int): Flow<List<Vocalization>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVocalizations(vocalizations: List<Vocalization>)
    
    @Query("SELECT COUNT(*) FROM species")
    fun getSpeciesCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM vocalizations")
    fun getVocalizationCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM species")
    suspend fun getSpeciesCountSync(): Int
    
    @Query("SELECT AVG(piComm) FROM species")
    fun getAvgPiComm(): Flow<Double?>
}

@Database(entities = [Species::class, Vocalization::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun animalDao(): AnimalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "animalia_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class AnimalRepository(private val animalDao: AnimalDao) {
    val allSpecies: Flow<List<Species>> = animalDao.getAllSpecies()
    val speciesCount: Flow<Int> = animalDao.getSpeciesCount()
    val vocalizationCount: Flow<Int> = animalDao.getVocalizationCount()
    val avgPiComm: Flow<Double?> = animalDao.getAvgPiComm()
    
    fun getVocalizations(speciesId: Int) = animalDao.getVocalizationsForSpecies(speciesId)

    suspend fun populateInitialData() {
        if (animalDao.getSpeciesCountSync() > 0) return
        
        val species = listOf(
            Species(1, "Chimpanzee", "🦍", 7.82),
            Species(2, "Dolphin", "🐬", 8.94),
            Species(3, "Dog", "🐕", 4.63),
            Species(4, "Parrot", "🦜", 6.80),
            Species(5, "Wolf", "🐺", 5.12),
            Species(6, "Honeybee", "🐝", 3.85)
        )
        animalDao.insertSpecies(species)

        val vocalizations = listOf(
            Vocalization(0, 1, "Leaf-Clipping Sound", 247.0, 12.0, 8.5, "Pay attention to me"),
            Vocalization(0, 1, "Soft Food Bark", 185.0, 8.0, 12.3, "I want something"),
            Vocalization(0, 1, "Pant-Hoot", 312.0, 45.0, -18.7, "Where is everyone?"),
            
            Vocalization(0, 2, "Signature Whistle", 1843.0, 127.0, 0.25, "This is me, I am here"),
            Vocalization(0, 2, "Echolocation Click Train", 2450.0, 215.0, 8.7, "Investigating object"),
            
            Vocalization(0, 3, "Play Bark", 0.42, 0.18, 4.2, "Let's play!"),
            Vocalization(0, 3, "Whine with Tail Wag", 0.38, 0.15, 3.8, "I need/want something"),
            
            Vocalization(0, 4, "Contact Call", 1250.0, 85.0, 6.5, "Where are you?"),
            Vocalization(0, 4, "Alarm Call", 1650.0, 105.0, 18.2, "Danger! Threat detected!"),
            
            Vocalization(0, 5, "Howl", 2180.0, 145.0, 12.8, "I am here / Where is the pack?"),
            Vocalization(0, 5, "Growl", 680.0, 92.0, 25.3, "Back off / Warning"),
            
            Vocalization(0, 6, "Waggle Dance", 42.5, 8.2, 2.8, "Food source located approximately 2.8 km away")
        )
        animalDao.insertVocalizations(vocalizations)
    }
}
