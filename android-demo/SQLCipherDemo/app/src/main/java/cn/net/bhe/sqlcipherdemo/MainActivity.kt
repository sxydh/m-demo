package cn.net.bhe.sqlcipherdemo

import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import cn.net.bhe.sqlcipherdemo.ui.theme.SQLCipherDemoTheme
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteOpenHelper
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SQLCipherDemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android", modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        sqlCipherDemo(this)
    }
}

fun sqlCipherDemo(context: Context) {
    SQLiteDatabase.loadLibs(context)

    val file = "sqlcipher_demo.db"
    val openHelper = SQLCipherSQLiteOpenHelper(context, file, 1)
    val pwd = SQLiteDatabase.getBytes("123".toCharArray())
    val db = openHelper.getWritableDatabase(pwd)

    /* 新增 */
    val tb = "t_demo"
    db.insert(tb, null, ContentValues().apply {
        put("value", UUID.randomUUID().toString())
    })

    /* 查询 */
    val cur = db.query("select value from t_demo")
    while (cur.moveToNext()) {
        println(cur.getString(0))
    }

    /* 删库 */
    val path = context.getDatabasePath(file)
    if (path.exists()) {
        path.delete()
    }
}

class SQLCipherSQLiteOpenHelper(context: Context, name: String, version: Int) : SQLiteOpenHelper(context, name, null, version) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("create table if not exists t_demo(id integer primary key autoincrement, value text)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("drop table if exists t_demo")
        onCreate(db)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!", modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SQLCipherDemoTheme {
        Greeting("Android")
    }
}