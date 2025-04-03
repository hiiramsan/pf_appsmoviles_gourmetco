package sanchez.carlos.gourmetco

import android.app.Application
import com.cloudinary.android.MediaManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initCloudinary()
    }

    private fun initCloudinary() {
        val config = mutableMapOf<String, String>()
        config["cloud_name"] = "dvznvnzam"
        MediaManager.init(this, config)
    }
}