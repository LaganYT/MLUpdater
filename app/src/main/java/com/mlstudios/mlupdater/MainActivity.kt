import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var apps: List<AppInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.listView)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        listView.adapter = adapter

        fetchAppInfo()
    }

    private fun fetchAppInfo() {
        GlobalScope.launch(Dispatchers.IO) {
            val jsonString = readConfigFile()
            apps = parseConfigFile(jsonString)
            updateUI()
        }
    }

    private fun readConfigFile(): String {
        val url = URL("https://yourwebsite.com/config.json")
        val connection = url.openConnection()
        val reader = BufferedReader(InputStreamReader(connection.getInputStream()))
        val stringBuilder = StringBuilder()

        var line: String?
        while (reader.readLine().also { line = it } != null) {
            stringBuilder.append(line)
        }

        return stringBuilder.toString()
    }

    private fun parseConfigFile(jsonString: String): List<AppInfo> {
        val apps = mutableListOf<AppInfo>()
        val jsonArray = JSONArray(jsonString)
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val packageName = jsonObject.getString("packageName")
            val appName = jsonObject.getString("appName")
            val updateUrl = jsonObject.getString("updateUrl")
            apps.add(AppInfo(packageName, appName, updateUrl))
        }
        return apps
    }

    private fun updateUI() {
        runOnUiThread {
            adapter.clear()
            for (app in apps) {
                val status = if (isAppInstalled(app.packageName)) "Installed" else "Not Installed"
                adapter.add("${app.appName} - $status")
            }
        }
    }

    private fun isAppInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}

data class AppInfo(val packageName: String, val appName: String, val updateUrl: String)
