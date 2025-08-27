package com.example.chatbot.ui

import MessageAdapter
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    }




}