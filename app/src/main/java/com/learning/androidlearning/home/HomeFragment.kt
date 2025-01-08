package com.learning.androidlearning.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.learning.androidlearning.R

class HomeFragment : Fragment() {

    private lateinit var tvUsername: TextView
    private lateinit var rvHotTopics: RecyclerView
    private lateinit var tvLeaderboardUpdate: TextView
    private val hotTopicsAdapter = HotTopicsAdapter()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupData()
    }

    private fun initViews(view: View) {
        tvUsername = view.findViewById(R.id.tvWelcome)
        rvHotTopics = view.findViewById(R.id.rvHotTopics)
        tvLeaderboardUpdate = view.findViewById(R.id.tvLeaderboardUpdate)

        // Setup RecyclerView
        rvHotTopics.adapter = hotTopicsAdapter
    }

    private fun setupData() {
        // Set username
        tvUsername.text = "Yilin"

        // Set leaderboard update time
        tvLeaderboardUpdate.text = "Updated 20/10/2024"

        // Setup hot topics data
        val hotTopics =
                listOf(HotTopic("1", "NEWS 1"), HotTopic("2", "NEWS 2"), HotTopic("3", "NEWS 3"))
        hotTopicsAdapter.setItems(hotTopics)
    }

    companion object {
        fun newInstance() = HomeFragment()
    }
}
