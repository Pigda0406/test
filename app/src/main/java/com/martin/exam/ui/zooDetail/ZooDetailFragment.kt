package com.martin.exam.ui.zooDetail

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
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
import com.martin.exam.repository.model.PlantsDataModel
import com.martin.exam.repository.model.ZooCenterDataModel
import com.martin.exam.ui.main.MainViewModel
import com.zhy.adapter.recyclerview.CommonAdapter
import com.zhy.adapter.recyclerview.base.ViewHolder
import kotlinx.android.synthetic.main.fragment_zoo_detail.*
import kotlinx.android.synthetic.main.fragment_zoo_detail.view.*
import org.koin.android.viewmodel.ext.android.viewModel

class ZooDetailFragment : BaseFragment() {

    companion object {
        fun newInstance() = ZooDetailFragment()
    }
    lateinit var adapter: CommonAdapter<PlantsDataModel>
    private var zooPlantsList : MutableList<PlantsDataModel> = mutableListOf()
    lateinit var data: ZooCenterDataModel

    private val zooDetailViewModel by viewModel<ZooDetailViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            data = (it.getSerializable("zoo") as ZooCenterDataModel?)!!
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        rootView =  inflater.inflate(R.layout.fragment_zoo_detail, container, false)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
        observeViewModel()
    }

    private fun initData(){
        zooDetailViewModel.getZooPlants(data.name)
    }

    private fun initView(){
        Glide.with(requireContext()).load(data.pictureUrl)
                    .into(rootView.ivZooDetail)
        rootView.tvBrief.text = data.longDescription
        rootView.tvCategory.text = data.category
        rootView.tvZooName.text = data.name
        if (data.memo.isEmpty()) {
            rootView.tvMemo.text = "無休館資訊"
        } else {
            rootView.tvMemo.text = data.memo
        }
        if (!data.url.isNullOrBlank()) {
            val webLinkText =
                "<font color='#1e84fb'><a href='${data.url}'>在網頁開啟</a>"
            rootView.tvLink.text = Html.fromHtml(webLinkText)
            rootView.tvLink.movementMethod = LinkMovementMethod.getInstance();
        }

        rootView.rvPlants.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        rootView.rvPlants.isNestedScrollingEnabled = true
        adapter = object : CommonAdapter<PlantsDataModel>(
            context,
            R.layout.item_plants_list,
            zooPlantsList
        ) {
            override fun convert(
                holder: ViewHolder,
                t: PlantsDataModel,
                position: Int
            ) {
                Glide.with(requireContext()).load(t.imageUrl)
                    .into(holder.getView(R.id.ivPlant))
                holder.setText(R.id.tvPlantName,t.alsoKnown)
                holder.setText(R.id.tvPlantBrief,t.brief)
                holder.itemView.setOnClickListener {
                    var bundle = bundleOf("plant" to t)
                    NavHostFragment.findNavController(this@ZooDetailFragment)
                        .navigate(R.id.action_zooDetailFragment_to_plantDetailFragment,bundle)
                }
            }

        }
        rootView.rvPlants.adapter = adapter

    }

    private fun observeViewModel(){
        zooDetailViewModel.zooPlants.observe(viewLifecycleOwner, Observer {
            zooPlantsList.clear()
            zooPlantsList.addAll(it)
            adapter.notifyDataSetChanged()
        })
    }

}