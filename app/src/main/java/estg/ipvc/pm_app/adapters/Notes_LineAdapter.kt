package estg.ipvc.pm_app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import estg.ipvc.pm_app.R
import estg.ipvc.pm_app.dataclasses.note
import kotlinx.android.synthetic.main.notes_helper.view.*

class NoteLineAdapter (val list: ArrayList<note>): RecyclerView.Adapter<LineViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineViewHolder {
        val itemView = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.notes_helper, parent, false )
        return LineViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: LineViewHolder, position: Int) {
        val currentplace = list[position]

        holder.title.text = currentplace.title
        holder.text.text = currentplace.text
    }
}

class LineViewHolder( itemView: View): RecyclerView.ViewHolder(itemView) {
    val title = itemView.notes_recycle_title
    val text = itemView.notes_recycle_text
}