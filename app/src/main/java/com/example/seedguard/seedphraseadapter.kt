import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.example.seedguard.R

class seedphraseadapter(private val itemCount: Int) : RecyclerView.Adapter<seedphraseadapter.SeedPhraseViewHolder>() {

    private val seedPhrases = MutableList(itemCount) { "" }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeedPhraseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_seed_phrase, parent, false)
        return SeedPhraseViewHolder(view)
    }

    override fun onBindViewHolder(holder: SeedPhraseViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = itemCount

    fun getSeedPhrases(): List<String> {
        return seedPhrases
    }

    inner class SeedPhraseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val editText: EditText = itemView.findViewById(R.id.seedPhraseEditText)

        fun bind(position: Int) {
            editText.hint = "Word ${position + 1}"
            editText.setText(seedPhrases[position])
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    seedPhrases[position] = s.toString().trim()
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }
}
