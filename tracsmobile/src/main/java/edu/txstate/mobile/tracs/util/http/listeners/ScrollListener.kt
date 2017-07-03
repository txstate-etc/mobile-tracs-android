package edu.txstate.mobile.tracs.util.http.listeners

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager

abstract class ScrollListener : RecyclerView.OnScrollListener {
    private val currentPage = 0
    private val previousTotalItemCount = 0
    private val loading = true
    private val startingPageIndex = 0

    private var visibleThreshold = 5

    private lateinit var layoutManager: RecyclerView.LayoutManager

    constructor(layoutManager: LinearLayoutManager) {
        this.layoutManager = layoutManager
    }

    constructor(layoutManager: GridLayoutManager) {
        this.layoutManager = layoutManager
        visibleThreshold *= layoutManager.spanCount
    }

    constructor (layoutManager: StaggeredGridLayoutManager) {
        this.layoutManager = layoutManager
        visibleThreshold *= layoutManager.spanCount
    }

    fun getLastVisibleItem(lastVisibleItemPositions: Array<Int>) {

    }
}