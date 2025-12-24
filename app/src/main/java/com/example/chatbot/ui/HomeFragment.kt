package com.example.chatbot.ui

import com.example.chatbot.ui.HomeViewModel
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatbot.QuizActivity
import com.example.chatbot.R
import com.example.chatbot.data.message.MessageAdapter
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var drawerButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        drawerLayout = view.findViewById(R.id.drawer_layout)
        navigationView = view.findViewById(R.id.nav_view)
        drawerButton = view.findViewById(R.id.drawerButton)

        drawerButton.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        setupNavigationMenu()

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val messageAdapter = MessageAdapter(mutableListOf(), currentUserId)
        recyclerView.adapter = messageAdapter

        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            messageAdapter.submitList(messages.toList())
            if (messages.isNotEmpty()) {
                recyclerView.scrollToPosition(messages.size - 1)
            }
        }

        val button = view.findViewById<ImageButton>(R.id.sendButton)
        val input = view.findViewById<EditText>(R.id.textView)

        button.setOnClickListener {
            val messageText = input.text.toString().trim()
            if (messageText.isNotEmpty()) {
                viewModel.sendMessage(messageText, currentUserId)
                input.text.clear()
            }
        }

        viewModel.conversations.observe(viewLifecycleOwner) { conversations ->
            updateNavigationMenu(conversations)
        }
    }

    private fun setupNavigationMenu() {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_new_chat -> {
                    viewModel.startNewConversation()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_start_quiz -> {
                    val intent = Intent(activity, QuizActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> false
            }
        }
    }

    private fun updateNavigationMenu(conversations: List<com.example.chatbot.data.message.Conversation>) {
        val conversationsMenu = navigationView.menu.findItem(R.id.conversations_item).subMenu
        conversationsMenu?.clear()

        conversations.forEach { conversation ->
            conversationsMenu?.add(conversation.name)?.setOnMenuItemClickListener {
                viewModel.selectConversation(conversation.id)
                drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
        }
    }
}