package com.martin.exam.ui.plantDetail

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
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
import kotlinx.android.synthetic.main.fragment_plant_detail.view.*


class PlantDetailFragment : BaseFragment() {

    companion object {
        fun newInstance() = PlantDetailFragment()
    }
    lateinit var data: PlantsDataModel



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            data = (it.getSerializable("plant") as PlantsDataModel?)!!
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        rootView =  inflater.inflate(R.layout.fragment_plant_detail, container, false)
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
    }

    private fun initView(){
        Glide.with(requireContext()).load(data.imageUrl)
                    .into(rootView.ivPlantDetail)
        rootView.tvBrief.text = "介紹：\n${data.longDescription}"
        rootView.tvEngName.text = data.nameEn
        rootView.tvSmallName.text = "其他名稱：\n${data.alsoKnown}"
        rootView.tvFeature.text = "辨認方式：\n${data.feature}"
        rootView.tvFunction.text = "所在位置：\n${data.location}"
        rootView.tvFinalDate.text = "最後更新：${data.updatedAt}"
    }

    private fun observeViewModel(){

    }

}