package com.example.chatbot.ui

import com.example.chatbot.data.message.MessageAdapter
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatbot.R
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val button = view.findViewById<ImageButton>(R.id.sendButton)
        val input = view.findViewById<EditText>(R.id.textView)

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        val messageAdapter = MessageAdapter(
            messages = mutableListOf(),
            currentUserId = currentUserId
        )
        recyclerView.adapter = messageAdapter

        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            messageAdapter.submitList(messages)
            recyclerView.scrollToPosition(messages.size - 1)
        }

        button.setOnClickListener {
            val message = input.text.toString().trim()
            if (message.isNotEmpty()) {
                viewModel.sendMessage(message, currentUserId)
                Log.d("HomeFragment", "Message sent: $message")
                input.text.clear()
            }
        }
    }
}
