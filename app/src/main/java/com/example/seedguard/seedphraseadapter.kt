package com.example.seedguard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView

class seedphraseadapter (private val seedPhraseCount: Int) :
    RecyclerView.Adapter<seedphraseadapter.ViewHolder>() {

    private val seedPhrases: MutableList<String> = MutableList(seedPhraseCount) { "" }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val seedPhraseInput: EditText = itemView.findViewById(R.id.seedPhraseEditText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_seed_phrase, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.seedPhraseInput.hint = "Word ${position + 1}" // Set hint as "Word 1", "Word 2", etc.

        holder.seedPhraseInput.setText(seedPhrases[position])

        holder.seedPhraseInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                seedPhrases[position] = holder.seedPhraseInput.text.toString()
            }
        }
    }

    override fun getItemCount(): Int = seedPhraseCount

    fun getSeedPhrases(): List<String> {
        return seedPhrases
    }
}
