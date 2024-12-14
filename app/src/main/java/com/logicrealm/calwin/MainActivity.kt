package com.logicrealm.calwin

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.widget.GridLayout
import java.util.Locale

data class RowData(
    var team1Score: String,
    var team2Score: String
)

data class GameData(
    val round: Int,
    val team1: String,
    val team2: String
)

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RowAdapter
    private lateinit var tot1: TextView
    private lateinit var tot2: TextView
    private lateinit var addBtn: Button
    private lateinit var gameDataTable: GridLayout
    private var team1Total = 0
    private var team2Total = 0
    private var team1Name = "Team 1"
    private var team2Name = "Team 2"
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var clearBtn: Button
    private lateinit var saveBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.tableRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = RowAdapter { updateTotals() }
        recyclerView.adapter = adapter

        addBtn = findViewById(R.id.addRowButton)
        addBtn.setOnClickListener {
            val success = adapter.addRow()
            if (!success) {
                Toast.makeText(this, "Please fill in the previous row before adding a new one.", Toast.LENGTH_SHORT).show()
            }
        }

        clearBtn = findViewById(R.id.clearButton)
        clearBtn.setOnClickListener {
            adapter.clearRows()
            Toast.makeText(this, "Rounds Cleared", Toast.LENGTH_SHORT).show()
        }


        // TODO: Save Functionality
        saveBtn = findViewById(R.id.saveButton)

        saveBtn.setOnClickListener {
            Toast.makeText(this, "This feature will be implemented in future", Toast.LENGTH_SHORT).show()
//            saveData(this)
////            val gameDataListToSave = mutableListOf<GameData>()
////
////            // Iterate through rows in the adapter and save data
////            for (i in adapter.rows.indices) {
////                val rowData = adapter.rows[i]
////                val round = i + 1 // Round number (1-based)
////                val gameData = GameData(round, rowData.team1Score, rowData.team2Score)
////                gameDataListToSave.add(gameData)
////            }
////
////            // Save the data
////            saveData(this, gameDataListToSave)
////            Toast.makeText(this, "Data saved successfully", Toast.LENGTH_SHORT).show()
        }

//        val gameDataList = loadData(this)
//        Log.d("Got Data", gameDataList.toString())
//
//        for (gameData in gameDataList) {
//            addRowToTable(gameData)
//        }

        updateTotals()
    }

    // Add a row dynamically to the RecyclerView
    @SuppressLint("InflateParams")
    private fun addRowToTable(gameData: GameData) {
        val newRow = layoutInflater.inflate(R.layout.row_item, null)
        val roundText = newRow.findViewById<TextView>(R.id.roundText)
        val team1Input = newRow.findViewById<EditText>(R.id.team1Input)
        val team2Input = newRow.findViewById<EditText>(R.id.team2Input)

        // Set values to views
        val locale = Locale.getDefault()
        roundText.text = String.format(locale, "%d", gameData.round)
        team1Input.setText(gameData.team1)
        team2Input.setText(gameData.team2)

        // Add row to the table
        gameDataTable.addView(newRow)
    }

    private fun saveData(context: Context) {
        // TODO: for loop to get full data of recycler view

        val data = intArrayOf(7,7)

        sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val gson = Gson()
        val json = gson.toJson(data)

        Log.d("Json Data", json)

        editor.putString("game_data", json)
        editor.apply()
    }

    private fun loadData(context: Context) {
        sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("game_data", "[]")

        val gson = Gson()
        val type = object : TypeToken<List<GameData>>() {}.type
        return gson.fromJson(json, type)
    }

    @SuppressLint("SetTextI18n")
    private fun updateTotals() {
        team1Total = adapter.rows.sumOf { it.team1Score.toIntOrNull() ?: 0 }
        team2Total = adapter.rows.sumOf { it.team2Score.toIntOrNull() ?: 0 }

        tot1 = findViewById(R.id.team1Total)
        tot2 = findViewById(R.id.team2Total)

        tot1.text = "$team1Name: $team1Total"
        tot2.text = "$team2Name: $team2Total"
    }
}

class RowAdapter(private val onRowUpdated: () -> Unit) :
    RecyclerView.Adapter<RowAdapter.RowViewHolder>() {

    val rows = mutableListOf<RowData>()

    inner class RowViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val roundText: TextView = view.findViewById(R.id.roundText)
        val team1Input: EditText = view.findViewById(R.id.team1Input)
        val team2Input: EditText = view.findViewById(R.id.team2Input)
        private val deleteButton: Button = view.findViewById(R.id.deleteButton)

        @SuppressLint("SetTextI18n")
        fun bind(position: Int, rowData: RowData) {
            roundText.text = (position + 1).toString()

            team1Input.setText(rowData.team1Score)
            team2Input.setText(rowData.team2Score)

            val watcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    rowData.team1Score = team1Input.text.toString()
                    rowData.team2Score = team2Input.text.toString()
                    onRowUpdated()
                }
            }

            team1Input.addTextChangedListener(watcher)
            team2Input.addTextChangedListener(watcher)

            deleteButton.setOnClickListener {
                rows.removeAt(adapterPosition)
                notifyItemRemoved(adapterPosition)
                notifyItemRangeChanged(adapterPosition, rows.size)
                onRowUpdated()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_item, parent, false)
        return RowViewHolder(view)
    }

    override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
        holder.bind(position, rows[position])
    }

    override fun getItemCount(): Int = rows.size

    fun addRow(score1: String = "", score2: String = ""): Boolean {
        // Validate last row before adding a new one
        if (rows.isNotEmpty()) {
            val lastRow = rows.last()
            if (lastRow.team1Score.isBlank() || lastRow.team2Score.isBlank()) {
                return false
            }
        }

        rows.add(RowData(score1, score2))
        notifyItemInserted(rows.size - 1)
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearRows() {
        rows.clear()
        notifyDataSetChanged()
    }

}
