package com.example.a25172022067_practical7_mad.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.a25172022067_practical7_mad.R
import com.example.a25172022067_practical7_mad.model.Person

class PersonAdapter(
    private val onDeleteClicked: (Person, Int) -> Unit
) : RecyclerView.Adapter<PersonAdapter.PersonViewHolder>() {

    private val persons = mutableListOf<Person>()

    fun submitList(newPersons: List<Person>) {
        persons.clear()
        persons.addAll(newPersons)
        notifyDataSetChanged()
    }

    fun removeItemAt(position: Int) {
        if (position in 0 until persons.size) {
            persons.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, persons.size - position)
        }
    }

    fun getItems(): List<Person> = persons

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_person, parent, false)
        return PersonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        val person = persons[position]
        holder.bind(person)
    }

    override fun getItemCount(): Int = persons.size

    inner class PersonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvPhone: TextView = itemView.findViewById(R.id.tvPhone)
        private val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        private val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(person: Person) {
            tvName.text = person.name
            tvPhone.text = person.phone
            tvEmail.text = person.email
            tvAddress.text = person.address

            btnDelete.setOnClickListener {
                val currentPosition = bindingAdapterPosition
                if (currentPosition != RecyclerView.NO_POSITION && currentPosition < persons.size) {
                    onDeleteClicked(persons[currentPosition], currentPosition)
                }
            }
        }
    }
}
