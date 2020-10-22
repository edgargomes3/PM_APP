package estg.ipvc.pm_app.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import estg.ipvc.pm_app.R
import estg.ipvc.pm_app.entity.*

class NoteAdapter internal constructor(
    context: Context
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var notes = emptyList<Note>()
    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noteItemView: TextView = itemView.findViewById(R.id.notes_recycle_title)
        val notesubItemView: TextView = itemView.findViewById(R.id.notes_recycle_text)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = inflater.inflate(R.layout.notes_helper, parent, false)
        return NoteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val current = notes[position]
        holder.noteItemView.text = current.name
        holder.notesubItemView.text = current.numero
    }
    internal fun setNote(notes: List<Note>){
        this.notes=notes
        notifyDataSetChanged()
    }

    override fun getItemCount() = notes.size
}