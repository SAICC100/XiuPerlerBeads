package com.example.xiuperlerbeads

import android.app.Application
import com.example.xiuperlerbeads.data.repository.InventoryRepository
import com.example.xiuperlerbeads.data.repository.JournalRepository
import com.example.xiuperlerbeads.domain.model.BeadColorManager

class XiuPerlerBeadsApp : Application() {

    lateinit var repository: InventoryRepository
        private set

    lateinit var journalRepository: JournalRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        // 必须在 Repository 初始化之前加载颜色数据，否则新建品牌时颜色列表为空
        BeadColorManager.loadFromAssets(this)
        repository = InventoryRepository(this)
        journalRepository = JournalRepository(this)
    }
    
    companion object {
        lateinit var instance: XiuPerlerBeadsApp
            private set
    }
}
