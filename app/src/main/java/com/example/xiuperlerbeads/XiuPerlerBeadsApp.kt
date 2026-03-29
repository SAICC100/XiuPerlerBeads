package com.example.xiuperlerbeads

import android.app.Application
import com.example.xiuperlerbeads.data.repository.InventoryRepository

class XiuPerlerBeadsApp : Application() {
    
    lateinit var repository: InventoryRepository
        private set
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        repository = InventoryRepository(this)
    }
    
    companion object {
        lateinit var instance: XiuPerlerBeadsApp
            private set
    }
}
