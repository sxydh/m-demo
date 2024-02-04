package cn.net.bhe.resolverdemo

import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import java.util.LinkedList

class MainActivity : ComponentActivity() {

    private val tag = MainActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val uriAuthority = "cn.net.bhe.providerdemo"
        val baseUri = "content://$uriAuthority"

        val insertUri = Uri.parse("$baseUri/t_log")
        val insertValues = ContentValues()
        val insertIds = LinkedList<String>()
        findViewById<Button>(R.id.insertButton).setOnClickListener {
            insertIds.push("${System.currentTimeMillis()}")
            insertValues.put("id", "${insertIds.first}")
            insertValues.put("ip", "192.168.233.129")
            insertValues.put("op", "INSERT")
            insertValues.put("val", "${insertIds.first}")
            val insertRet = contentResolver.insert(insertUri, insertValues)
            Log.d(tag, "onCreate, insertRet = $insertRet")
        }

        val queryListUri = Uri.parse("$baseUri/t_log")
        findViewById<Button>(R.id.queryListButton).setOnClickListener {
            val queryListRet = contentResolver.query(queryListUri, null, null, null, null)
            Log.d(tag, "onCreate, queryListRet = ${cursor2String(queryListRet!!)}")
        }

        findViewById<Button>(R.id.queryByIdButton).setOnClickListener {
            if (insertIds.size != 0) {
                val queryByIdUri = Uri.parse("$baseUri/t_log/${insertIds.first}")
                val queryByIdRet = contentResolver.query(queryByIdUri, null, null, null, null)
                Log.d(tag, "onCreate, queryByIdRet = ${cursor2String(queryByIdRet!!)}")
            }
        }
    }

    private fun cursor2String(cursor: Cursor): String {
        val stringBuilder = StringBuilder()
        while (cursor.moveToNext()) {
            stringBuilder.append("\n")
            for (i in 0 until cursor.columnCount) {
                stringBuilder.append(cursor.getString(i)).append(", ")
            }
        }
        return stringBuilder.toString()
    }

}