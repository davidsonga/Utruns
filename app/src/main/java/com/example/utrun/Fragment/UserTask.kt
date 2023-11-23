package com.example.utrun.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioGroup
import com.example.utrun.R
import com.example.utrun.util.cuurentLoaction


class UserTask : Fragment() {

    private lateinit var addNewTaskButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_task, container, false)

        if (savedInstanceState == null) {
            replaceFragment(InboxFragment())
        }

        val radioGroup: RadioGroup = view.findViewById(R.id.toggle)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.search -> replaceFragment(InboxFragment())
                R.id.offer -> replaceFragment(InboxFragment())
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addNewTaskButton = view.findViewById(R.id.btn_addTask)
        addNewTaskButton.setOnClickListener {
            // Navigate to UserTask fragment
            fragmentManager?.beginTransaction()?.apply {
                replace(R.id.fragment_container, AddNewTask())
                addToBackStack(null)
                commit()
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = childFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_task_layout, fragment)
        fragmentTransaction.commit()
    }

    override fun onResume() {
        super.onResume()

        val obj: cuurentLoaction = cuurentLoaction()
        obj.setUserCurrentLocation(requireContext())
    }
}
