package app.android.adreal.vault.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import app.android.adreal.vault.MainActivity
import app.android.adreal.vault.R
import app.android.adreal.vault.adapter.DataAdapter
import app.android.adreal.vault.databinding.FragmentDataBinding
import app.android.adreal.vault.model.Item
import app.android.adreal.vault.viewmodel.MainViewModel
import java.util.UUID

class Data : Fragment(), DataAdapter.OnItemClickListener, DataAdapter.OnItemLongClickListener {

    private val binding by lazy {
        FragmentDataBinding.inflate(layoutInflater)
    }

    private val viewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    private val adapter by lazy {
        DataAdapter(requireContext(), this, this)
    }

    private val recyclerView by lazy {
        binding.recyclerView
    }

    private val longPressArray = ArrayList<UUID>()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.delete.visibility = View.GONE

        initRecyclerAdapter()

        (activity as MainActivity).state.observe(viewLifecycleOwner) {
            if (it) {
                viewModel.decryptData(viewModel.repository.readData.value)
            }else{
                viewModel.setEncryptedData()
            }
        }

        binding.delete.setOnClickListener {
            for (i in longPressArray) {
                adapter.deleteItem(i)
                viewModel.delete(Item(i, "","", ""))
            }
            binding.delete.visibility = View.GONE
            longPressArray.clear()
        }

        viewModel.decryptedNotes.observe(viewLifecycleOwner) {
            adapter.setData(it)
        }

        binding.add.setOnClickListener {
            findNavController().navigate(R.id.action_data_to_add2)
        }

        return binding.root
    }

    private fun initRecyclerAdapter() {
        recyclerView.adapter = adapter
        recyclerView.hasFixedSize()
        recyclerView.layoutManager = StaggeredGridLayoutManager(
            2, LinearLayoutManager.VERTICAL
        )
    }

    private fun getBundle(id: UUID, title: String, description: String): Bundle {
        val bundle = Bundle()
        bundle.putString("id", id.toString())
        bundle.putString("title", title)
        bundle.putString("description", description)

        return bundle
    }

    override fun onItemClick(index: Int) {
        if (longPressArray.isNotEmpty()) {

        } else {
            val data = viewModel.decryptedNotes.value?.get(index)
            if (data != null) {
                try {
                    findNavController().navigate(
                        R.id.action_data_to_add2,
                        getBundle(data.id, data.title, data.description)
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onItemLongClick(primaryKey: UUID, status: Boolean) {
        if (status) {
            longPressArray.add(primaryKey)
        } else {
            longPressArray.remove(primaryKey)
        }

        if (longPressArray.isNotEmpty()) {
            binding.delete.visibility = View.VISIBLE
        } else {
            binding.delete.visibility = View.GONE
        }
    }
}