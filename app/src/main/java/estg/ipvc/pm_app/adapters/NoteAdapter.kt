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
        context: Context, val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var notes = emptyList<Note>()

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noteItemViewTitle: TextView = itemView.findViewById(R.id.notes_recycle_title)
        val noteItemViewText: TextView = itemView.findViewById(R.id.notes_recycle_text)

        fun bind( note: Note, clickListener: OnItemClickListener ) {
            noteItemViewTitle.text = note.title
            noteItemViewText.text = note.text

            itemView.setOnClickListener {
                clickListener.onItemClicked(note)
            }
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = inflater.inflate(R.layout.notes_helper, parent, false)
        return NoteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val current = notes[position]
        holder.noteItemViewTitle.tag = current.id
        holder.noteItemViewTitle.text = current.title
        holder.noteItemViewText.text = current.text

        holder.bind( current, itemClickListener )
    }

    internal fun setNote(notes: List<Note>){
        this.notes=notes
        notifyDataSetChanged()
    }

    fun getNoteAt(position: Int): Note {
        return notes[position]
    }

    interface OnItemClickListener {
        fun onItemClicked( note: Note )
    }

    override fun getItemCount() = notes.size
}