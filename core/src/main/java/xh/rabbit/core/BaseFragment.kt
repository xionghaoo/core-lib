package xh.rabbit.core

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<out VIEW: ViewBinding> : Fragment() {

    private var _binding: VIEW? = null
    protected val binding: VIEW get() {
        return _binding ?: throw IllegalStateException("view is destroyed")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = onCreateBindView(inflater, container, savedInstanceState)
        return _binding!!.root
    }

    abstract fun onCreateBindView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): VIEW

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}