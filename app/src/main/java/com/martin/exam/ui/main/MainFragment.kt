package com.martin.exam.ui.main

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.martin.exam.R
import com.martin.exam.base.BaseFragment
import com.martin.exam.repository.model.ZooCenterDataModel
import com.martin.exam.repository.model.ZooCenterResponse
import com.zhy.adapter.recyclerview.CommonAdapter
import com.zhy.adapter.recyclerview.base.ViewHolder
import kotlinx.android.synthetic.main.fragment_main.view.*
import org.koin.android.viewmodel.ext.android.viewModel

class MainFragment : BaseFragment() {

    lateinit var adapter: CommonAdapter<ZooCenterDataModel>
    private var zooPlaceList : MutableList<ZooCenterDataModel> = mutableListOf()

    private val viewModel by viewModel<MainViewModel>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        rootView =  inflater.inflate(R.layout.fragment_main, container, false)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
        observeViewModel()
    }

    private fun initData(){
        viewModel.getZooPlace()
    }

    private fun initView(){
        rootView.rvZooCenter.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        rootView.rvZooCenter.isNestedScrollingEnabled = true
        adapter = object : CommonAdapter<ZooCenterDataModel>(
            context,
            R.layout.item_zoo_center_list,
            zooPlaceList
        ) {
            override fun convert(
                holder: ViewHolder,
                t: ZooCenterDataModel,
                position: Int
            ) {
                Glide.with(requireContext()).load(t.pictureUrl)
                    .into(holder.getView(R.id.ivZooCenter))
                holder.setText(R.id.tvTitle,t.name)
                holder.setText(R.id.tvContent,t.longDescription)
                holder.setText(R.id.tvTime,t.memo)
                holder.itemView.setOnClickListener {
                    var bundle = bundleOf("zoo" to t)
                    NavHostFragment.findNavController(this@MainFragment)
                        .navigate(R.id.action_mainFragment_to_zooDetailFragment,bundle)
                }
            }

        }
        rootView.rvZooCenter.adapter = adapter

    }

    private fun observeViewModel(){
        viewModel.zooPlace.observe(viewLifecycleOwner, Observer {
            zooPlaceList.clear()
            zooPlaceList.addAll(it)
            adapter.notifyDataSetChanged()
        })
    }

}