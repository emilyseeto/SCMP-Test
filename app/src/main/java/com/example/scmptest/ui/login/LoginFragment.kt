package com.example.scmptest.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.scmptest.R
import com.example.scmptest.databinding.FragmentLoginBinding
import com.example.scmptest.ext.orFalse
import com.example.scmptest.ext.showErrorDialog
import com.example.scmptest.ext.visibleElseGone

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListener()
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.loginEmailInput.isEnabled = !isLoading
            binding.loginPwdInput.isEnabled = !isLoading
            binding.loginCtaBtn.isEnabled = !isLoading
            binding.loginLoadingContainer.visibleElseGone(isLoading)
        })

        viewModel.emailError.observe(viewLifecycleOwner, Observer { isError ->
            binding.loginEmailError.visibleElseGone(isError)
            setInputBg(
                view = binding.loginEmailInputContainer,
                isError = isError,
                isFocused = binding.loginEmailInput.isFocused
            )
        })

        viewModel.pwdError.observe(viewLifecycleOwner, Observer { isError ->
            binding.loginPwdError.visibleElseGone(isError)
            setInputBg(
                view = binding.loginPwdInputContainer,
                isError = isError,
                isFocused = binding.loginPwdInput.isFocused
            )
        })

        viewModel.loginToken.observe(viewLifecycleOwner, Observer { token ->
            if (!token.isNullOrEmpty()) {
                val bundle = Bundle().apply {
                    putString("token", token)
                }
                findNavController().navigate(R.id.action_LoginFragment_to_StaffListFragment, bundle)
                viewModel.clearLoginStatus()
            }
        })

        viewModel.loginError.observe(viewLifecycleOwner, Observer { error ->
            if (!error.isNullOrBlank()) {
                context?.showErrorDialog(error)
            }
        })
    }

    private fun setupListener() {
        binding.loginEmailInput.apply {
            setOnFocusChangeListener { _, hasFocus ->
                setInputBg(
                    view = binding.loginEmailInputContainer,
                    isError = viewModel.emailError.value.orFalse(),
                    isFocused = hasFocus
                )
            }
            addTextChangedListener { editable ->
                viewModel.clearEmailError()
                viewModel.setEmail(editable?.toString().orEmpty())
            }
        }

        binding.loginPwdInput.apply {
            setOnFocusChangeListener { _, hasFocus ->
                setInputBg(
                    view = binding.loginPwdInputContainer,
                    isError = viewModel.pwdError.value.orFalse(),
                    isFocused = hasFocus
                )
            }
            addTextChangedListener { editable ->
                viewModel.clearPwdError()
                viewModel.setPwd(editable?.toString().orEmpty())
            }
        }

        binding.loginCtaBtn.setOnClickListener { _ ->
            viewModel.login()
        }
    }

    private fun setInputBg(view: FrameLayout, isError: Boolean, isFocused: Boolean) {
        view.setBackgroundResource(
            when {
                isError -> R.drawable.bg_input_error
                isFocused -> R.drawable.bg_input_active
                else -> R.drawable.bg_input_inactive
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}