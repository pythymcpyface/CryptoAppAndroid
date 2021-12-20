package com.example.CryptoAppAndroid.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.CryptoAppAndroid.databinding.FragmentMainBinding
import com.example.CryptoAppAndroid.model.Elo
import com.example.CryptoAppAndroid.model.Price
import kotlinx.coroutines.CoroutineExceptionHandler
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * A placeholder fragment containing a simple view.
 */
class PlaceholderFragment : Fragment() {

    private lateinit var pageViewModel: PageViewModel
    private var _binding: FragmentMainBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProvider(this).get(PageViewModel::class.java).apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMainBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getRecyclerData()

        pageViewModel.recyclerData.observe(viewLifecycleOwner, Observer { recyclerData ->

            binding.refresh.isRefreshing = false
            updateRecyclerView(
                recyclerData,
                binding.recycler,
                "USDT",
                3,
                pageViewModel.filter
            )
        })

        binding.button.setOnClickListener {
            getRecyclerData()
        }

        binding.refresh.setOnRefreshListener {
            getRecyclerData()
        }

        binding.filter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                pageViewModel.recyclerData.value?.let {
                    updateRecyclerView(
                        it,
                        binding.recycler,
                        "USDT",
                        3,
                        s.toString().uppercase(
                            Locale.ROOT
                        )
                    )
                }
            }

            override fun afterTextChanged(s: Editable?) {
                pageViewModel.filter = s.toString().uppercase(
                    Locale.ROOT
                )
            }

        })
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): PlaceholderFragment {
            return PlaceholderFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun updateRecyclerView(
        recyclerData: RecyclerData,
        recyclerView: RecyclerView,
        quoteAsset: String,
        stdDevs: Int,
        filter: String
    ) {
        val filteredRecyclerData = prepareRecyclerData(recyclerData, filter)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            val recyclerAdapter = RecyclerAdapter(filteredRecyclerData, quoteAsset, stdDevs, requireContext())
            adapter = recyclerAdapter
            recyclerAdapter.notifyDataSetChanged()
        }
    }

    private fun getRecyclerData() {
        binding.refresh.isRefreshing = true
        pageViewModel.getRecyclerData(requireContext(), object: CoroutineExceptionHandler {
            override val key: CoroutineContext.Key<*>
                get() = CoroutineExceptionHandler.Key

            override fun handleException(context: CoroutineContext, exception: Throwable) {
                binding.refresh.isRefreshing = false
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error: $exception", Toast.LENGTH_LONG).show()
                }
            }

        })
    }

    private fun prepareRecyclerData(recyclerData: RecyclerData, filter: String): RecyclerData {
        val originalElos: List<Elo> = recyclerData.elos
        val originalPrices: List<Price> = recyclerData.prices
        val originalCoinList: List<String> = recyclerData.coinList

        val newElos = originalElos.mapNotNull { elo -> if (elo.coin.contains(filter)) {elo} else {null}}
        val newPrices = originalPrices.mapNotNull { price ->
            if (price.pair.split("-")[0].contains(filter)) {price} else {null}
        }
        val newCoinList = originalCoinList.mapNotNull { coin -> if (coin.contains(filter)) {coin} else {null}}

        return RecyclerData(
            newElos,
            recyclerData.stats,
            newPrices,
            newCoinList
        )
    }

}