package app.android.adreal.vault.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import app.android.adreal.vault.MainActivity
import app.android.adreal.vault.databinding.FragmentAddBinding
import app.android.adreal.vault.encryption.EncryptionHandler
import app.android.adreal.vault.model.Data
import app.android.adreal.vault.model.Filter
import app.android.adreal.vault.model.Item
import app.android.adreal.vault.sharedpreferences.SharedPreferences
import app.android.adreal.vault.utils.Constants
import app.android.adreal.vault.utils.GlobalFunctions
import app.android.adreal.vault.viewmodel.MainViewModel
import java.util.UUID

class Add : Fragment() {

    private val binding by lazy {
        FragmentAddBinding.inflate(layoutInflater)
    }

    private val viewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    private val mainActivityRef by lazy {
        activity as MainActivity
    }

    private var noteId : UUID? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        noteId = if(arguments?.getString("id") != null) {
            UUID.fromString(arguments?.getString("id"))
        }else{
            null
        }

        binding.title.setText(arguments?.getString("title") ?: "")
        binding.description.setText(arguments?.getString("description") ?: "")

        binding.close.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.save.setOnClickListener {
            if (binding.title.text.toString().isEmpty() || binding.description.text.toString()
                    .isEmpty()
            ) {
                mainActivityRef.showSnackbar("Please fill all the fields!")
                return@setOnClickListener
            } else {
                if (arguments?.getString("title")
                        .equals(
                            binding.title.text.toString().trim()
                        ) && arguments?.getString("description")
                        .equals(binding.description.text.toString().trim())
                ) {
                    findNavController().popBackStack()
                    return@setOnClickListener
                }

                if (noteId == null) {
                    viewModel.insert(
                        Item(
                            UUID.randomUUID(),
                            SharedPreferences.read(Constants.USER_ID, "").toString(),
                            EncryptionHandler(requireContext()).byteArrayToHexString(
                                EncryptionHandler(
                                    requireContext()
                                ).encrypt(binding.title.text.toString().encodeToByteArray())
                            ),
                            EncryptionHandler(requireContext()).byteArrayToHexString(
                                EncryptionHandler(
                                    requireContext()
                                ).encrypt(binding.description.text.toString().encodeToByteArray())
                            ),
                        )
                    )
                } else {
                    viewModel.update(
                        Item(
                            noteId!!,
                            SharedPreferences.read(Constants.USER_ID, "").toString(),
                            EncryptionHandler(requireContext()).byteArrayToHexString(
                                EncryptionHandler(
                                    requireContext()
                                ).encrypt(binding.title.text.toString().encodeToByteArray())
                            ),
                            EncryptionHandler(requireContext()).byteArrayToHexString(
                                EncryptionHandler(
                                    requireContext()
                                ).encrypt(binding.description.text.toString().encodeToByteArray())
                            ),
                        )
                    )
                }

                GlobalFunctions().sendNotification(
                    "Syncing Network", Data(
                        SharedPreferences.read(
                            Constants.USER_ID, ""
                        ).toString(), 0
                    ), Filter(
                        "tag",
                        Constants.ONE_SIGNAL_GENERAL_TAG,
                        "=",
                        Constants.ONE_SIGNAL_GENERAL_TAG
                    )
                )

                findNavController().popBackStack()
            }
        }

        return binding.root
    }
}