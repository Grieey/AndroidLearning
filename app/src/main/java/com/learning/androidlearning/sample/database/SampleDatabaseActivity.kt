package com.learning.androidlearning.sample.database

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.learning.androidlearning.R

class SampleDatabaseActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var resultTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample_database)

        dbHelper = DatabaseHelper(this)
        resultTextView = findViewById(R.id.textViewResult)

        // Create database
        findViewById<Button>(R.id.buttonCreateDb).setOnClickListener {
            try {
                dbHelper.writableDatabase
                showResult("Database created successfully!")
            } catch (e: Exception) {
                showError("Failed to create database: ${e.message}")
            }
        }

        // Insert data
        findViewById<Button>(R.id.buttonInsert).setOnClickListener {
            try {
                val id = dbHelper.insertUser("John", 25)
                dbHelper.insertUser("Mike", 30)
                showResult("Data inserted successfully! New ID: $id")
            } catch (e: Exception) {
                showError("Failed to insert data: ${e.message}")
            }
        }

        // Query data
        findViewById<Button>(R.id.buttonQuery).setOnClickListener {
            try {
                val users = dbHelper.getAllUsers()
                val result = StringBuilder()
                users.forEach { user ->
                    result.append("ID: ${user.id}, Name: ${user.name}, Age: ${user.age}\n")
                }
                showResult("Query Results:\n$result")
            } catch (e: Exception) {
                showError("Failed to query data: ${e.message}")
            }
        }

        // Update data
        findViewById<Button>(R.id.buttonUpdate).setOnClickListener {
            try {
                val count = dbHelper.updateUser(1, "John (Updated)", 26)
                showResult("Update completed, affected rows: $count")
            } catch (e: Exception) {
                showError("Failed to update data: ${e.message}")
            }
        }

        // Delete data
        findViewById<Button>(R.id.buttonDelete).setOnClickListener {
            try {
                val count = dbHelper.deleteUser(1)
                showResult("Delete completed, affected rows: $count")
            } catch (e: Exception) {
                showError("Failed to delete data: ${e.message}")
            }
        }

        // Delete all data
        findViewById<Button>(R.id.buttonDeleteAll).setOnClickListener {
            try {
                dbHelper.deleteAllUsers()
                showResult("All data has been deleted")
            } catch (e: Exception) {
                showError("Failed to delete all data: ${e.message}")
            }
        }
    }

    private fun showResult(message: String) {
        resultTextView.text = message
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showError(error: String) {
        resultTextView.text = "Error: $error"
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
} 