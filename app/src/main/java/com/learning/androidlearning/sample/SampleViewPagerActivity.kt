package com.learning.androidlearning.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.learning.androidlearning.R

class SampleViewPagerActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample_viewpager)

        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = ViewPagerAdapter(this)
    }

    private inner class ViewPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> SampleFragment.newInstance("Fragment 1")
                1 -> SampleFragment.newInstance("Fragment 2")
                2 -> SampleFragment.newInstance("Fragment 3")
                else -> throw IllegalArgumentException("Invalid position $position")
            }
        }
    }
}

class SampleFragment : Fragment() {
    companion object {
        private const val ARG_TEXT = "text"

        fun newInstance(text: String): SampleFragment {
            val fragment = SampleFragment()
            val args = Bundle()
            args.putString(ARG_TEXT, text)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sample_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val textView = view.findViewById<TextView>(R.id.sampleTextView)
        textView.text = arguments?.getString(ARG_TEXT) ?: "Default Text"
    }
}