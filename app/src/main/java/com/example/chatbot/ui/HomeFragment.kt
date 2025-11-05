package com.example.chatbot.ui

import HomeViewModel
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatbot.R
import com.example.chatbot.data.message.MessageAdapter
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawerButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        setHasOptionsMenu(true)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        drawerLayout = view.findViewById(R.id.drawer_layout)
        navigationView = view.findViewById(R.id.nav_view)
        drawerButton = view.findViewById(R.id.drawerButton)

        drawerToggle = ActionBarDrawerToggle(
            activity,
            drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

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
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun updateNavigationMenu(conversations: List<com.example.chatbot.data.message.Conversation>) {
        navigationView.menu.clear()

        // "Yeni Sohbet" butonu
        navigationView.menu.add(Menu.NONE, R.id.nav_new_chat, Menu.NONE, "New Chat").setIcon(R.drawable.ic_add)
            .setOnMenuItemClickListener {
                viewModel.startNewConversation()
                drawerLayout.closeDrawer(GravityCompat.START)
                true
            }


        val conversationsMenu = navigationView.menu.addSubMenu("Conversations")
        conversations.forEach { conversation ->
            conversationsMenu.add(conversation.name).setOnMenuItemClickListener {
                viewModel.selectConversation(conversation.id)
                drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}