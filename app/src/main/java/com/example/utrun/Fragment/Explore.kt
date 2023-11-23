package com.example.utrun.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import com.example.utrun.R
import android.location.LocationManager
import android.widget.Toast
import com.example.utrun.util.cuurentLoaction

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
class Explore : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)

        if (savedInstanceState == null) {
            replaceFragment(InboxFragment())
        }

        val radioGroup: RadioGroup = view.findViewById(R.id.toggle)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.search -> replaceFragment(OutGoingFragment())

                R.id.offer -> replaceFragment(InboxFragment())
            }

        }

        return view
    }


    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = childFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_task_layout, fragment)
        fragmentTransaction.commit()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserTask().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    override fun onResume() {
        super.onResume()

       // val obj: cuurentLoaction = cuurentLoaction()
      //  obj.setUserCurrentLocation(requireContext())
    }

}
