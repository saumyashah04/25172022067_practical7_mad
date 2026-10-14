package com.example.a25172022067_practical7_mad

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a25172022067_practical7_mad.adapter.PersonAdapter
import com.example.a25172022067_practical7_mad.db.DatabaseHelper
import com.example.a25172022067_practical7_mad.network.ApiService
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var personAdapter: PersonAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private lateinit var fabRefresh: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        dbHelper = DatabaseHelper(this)

        initViews()
        setupRecyclerView()
        loadData()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        fabRefresh = findViewById(R.id.fabRefresh)

        fabRefresh.setOnClickListener {
            fetchFromApiAndStoreInDb(isManualRefresh = true)
        }
    }

    private fun setupRecyclerView() {
        personAdapter = PersonAdapter { person, position ->
            deletePersonRecord(person.id, position)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = personAdapter
    }

    /**
     * Loads records from SQLite database. If database is empty or has stale data (>4),
     * it automatically triggers API retrieval to seed the database with 4 persons.
     */
    private fun loadData() {
        val existingPersons = dbHelper.getAllPersons()
        if (existingPersons.isNotEmpty() && existingPersons.size <= 4) {
            personAdapter.submitList(existingPersons)
            updateEmptyState()
        } else {
            fetchFromApiAndStoreInDb(isManualRefresh = false)
        }
    }

    /**
     * Retrieves person data in JSON format from the internet API endpoint,
     * stores the retrieved records (max 4) into SQLite database, and updates the UI.
     */
    private fun fetchFromApiAndStoreInDb(isManualRefresh: Boolean) {
        progressBar.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE

        thread {
            // 1. Fetch JSON from API (with automatic fallback to assets if offline)
            val result = ApiService.fetchPersons(this@MainActivity)

            if (result.persons.isNotEmpty()) {
                // 2. Store retrieved data into SQLite database
                dbHelper.clearAllPersons()
                dbHelper.insertPersons(result.persons.take(4))
            }

            // 3. Read data back from SQLite database to populate UI
            val dbPersons = dbHelper.getAllPersons()

            runOnUiThread {
                progressBar.visibility = View.GONE
                personAdapter.submitList(dbPersons)
                updateEmptyState()

                val toastMessage = if (isManualRefresh) {
                    "${result.message} & saved to SQLite"
                } else {
                    result.message
                }
                Toast.makeText(this@MainActivity, toastMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Deletes a record from the SQLite database and removes it from the adapter.
     */
    private fun deletePersonRecord(id: Long, position: Int) {
        val success = dbHelper.deletePerson(id)
        if (success) {
            personAdapter.removeItemAt(position)
            updateEmptyState()
            Toast.makeText(this, getString(R.string.person_deleted), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateEmptyState() {
        val count = personAdapter.itemCount
        tvEmptyState.visibility = if (count == 0) View.VISIBLE else View.GONE
        recyclerView.visibility = if (count == 0) View.GONE else View.VISIBLE
    }
}
