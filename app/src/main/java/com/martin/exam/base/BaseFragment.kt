package com.martin.exam.base

import android.app.Dialog

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.martin.exam.R


var isLoading:Boolean = false
open class BaseFragment : Fragment(), View.OnClickListener {

    lateinit var loadingDialog: Dialog

    protected lateinit var rootView: View

    protected val fm: FragmentManager by lazy {
        requireActivity().supportFragmentManager
    }


    override fun onClick(v: View?) {
        TODO("Not yet implemented")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()
    }
    private fun initUI() {
        //createLoadingDialog(this)
        //loadingDialog = createLoadingDialog(requireContext())
    }

    fun startLoading() {
        if(!isLoading){
            loadingDialog.show()
            isLoading = true
        }
    }

    fun stopLoading() {
        loadingDialog.dismiss()
        isLoading = false
    }
    fun setActionBar(
        activity: AppCompatActivity,
        toolbar: Toolbar,
        stringRes: Int = 0,
        indicator: Int = R.drawable.ic_arrow_back
    ) {
        activity.setSupportActionBar(toolbar)
        val actionBar: ActionBar? = activity.supportActionBar
        //actionBar?.setTitle(stringRes)
        actionBar?.setHomeAsUpIndicator(indicator)
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }



}