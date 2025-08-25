package com.example.chatbot.ui.login

import androidx.lifecycle.ViewModelProvider
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.chatbot.databinding.FragmentLoginBinding
import com.example.chatbot.R

class LoginFragment : Fragment() {

    private lateinit var loginViewModel: LoginViewModel
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        val usernameEditText = binding.username
        val passwordEditText = binding.password
        val loginButton = binding.login
        val loadingProgressBar = binding.loading

        loginViewModel.loginFormState.observe(viewLifecycleOwner) { loginFormState ->
            loginFormState ?: return@observe
            loginButton.isEnabled = loginFormState.isDataValid
            loginFormState.usernameError?.let {
                usernameEditText.error = getString(it)
            }
            loginFormState.passwordError?.let {
                passwordEditText.error = getString(it)
            }
        }

        loginViewModel.loginResult.observe(viewLifecycleOwner) { loginResult ->
            loginResult ?: return@observe
            loadingProgressBar.visibility = View.GONE
            loginResult.error?.let {
                showLoginFailed(it)
            }
            loginResult.success?.let {
                updateUiWithUser(it)
                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            }
        }

        // EditText listener
        val afterTextChangedListener = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                loginViewModel.loginDataChanged(
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        }
        usernameEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.addTextChangedListener(afterTextChangedListener)

        passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginViewModel.login(
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
            false
        }

        loginButton.setOnClickListener {
            loadingProgressBar.visibility = View.VISIBLE
            loginViewModel.login(
                usernameEditText.text.toString(),
                passwordEditText.text.toString()
            )
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome) + model.displayName
        Toast.makeText(requireContext().applicationContext, welcome, Toast.LENGTH_LONG).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(requireContext().applicationContext, errorString, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
