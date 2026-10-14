package com.example.a25172022067_practical7_mad.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.a25172022067_practical7_mad.model.Person

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "persons.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_PERSONS = "persons"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_PHONE = "phone"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_ADDRESS = "address"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_PERSONS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_PHONE TEXT,
                $COLUMN_EMAIL TEXT,
                $COLUMN_ADDRESS TEXT
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PERSONS")
        onCreate(db)
    }

    /**
     * Inserts a single person into SQLite database.
     */
    fun insertPerson(person: Person): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, person.name)
            put(COLUMN_PHONE, person.phone)
            put(COLUMN_EMAIL, person.email)
            put(COLUMN_ADDRESS, person.address)
        }
        return db.insert(TABLE_PERSONS, null, values)
    }

    /**
     * Batch inserts a list of persons using a transaction.
     */
    fun insertPersons(persons: List<Person>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (person in persons) {
                val values = ContentValues().apply {
                    put(COLUMN_NAME, person.name)
                    put(COLUMN_PHONE, person.phone)
                    put(COLUMN_EMAIL, person.email)
                    put(COLUMN_ADDRESS, person.address)
                }
                db.insert(TABLE_PERSONS, null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    /**
     * Retrieves all persons stored in SQLite database.
     */
    fun getAllPersons(): List<Person> {
        val personList = mutableListOf<Person>()
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_PERSONS,
            arrayOf(COLUMN_ID, COLUMN_NAME, COLUMN_PHONE, COLUMN_EMAIL, COLUMN_ADDRESS),
            null, null, null, null,
            "$COLUMN_ID ASC"
        )

        cursor.use {
            if (it.moveToFirst()) {
                val idIndex = it.getColumnIndexOrThrow(COLUMN_ID)
                val nameIndex = it.getColumnIndexOrThrow(COLUMN_NAME)
                val phoneIndex = it.getColumnIndexOrThrow(COLUMN_PHONE)
                val emailIndex = it.getColumnIndexOrThrow(COLUMN_EMAIL)
                val addressIndex = it.getColumnIndexOrThrow(COLUMN_ADDRESS)

                do {
                    val id = it.getLong(idIndex)
                    val name = it.getString(nameIndex) ?: ""
                    val phone = it.getString(phoneIndex) ?: ""
                    val email = it.getString(emailIndex) ?: ""
                    val address = it.getString(addressIndex) ?: ""

                    personList.add(Person(id, name, phone, email, address))
                } while (it.moveToNext())
            }
        }
        return personList
    }

    /**
     * Deletes a person by their SQLite record ID.
     */
    fun deletePerson(id: Long): Boolean {
        val db = writableDatabase
        val rowsDeleted = db.delete(
            TABLE_PERSONS,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
        return rowsDeleted > 0
    }

    /**
     * Deletes all persons in the database.
     */
    fun clearAllPersons() {
        val db = writableDatabase
        db.delete(TABLE_PERSONS, null, null)
    }

    /**
     * Returns total number of persons in the database.
     */
    fun getPersonCount(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_PERSONS", null)
        var count = 0
        cursor.use {
            if (it.moveToFirst()) {
                count = it.getInt(0)
            }
        }
        return count
    }
}
