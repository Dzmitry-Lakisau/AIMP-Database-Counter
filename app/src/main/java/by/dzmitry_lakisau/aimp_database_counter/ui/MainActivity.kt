package by.dzmitry_lakisau.aimp_database_counter.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.method.ScrollingMovementMethod
import android.widget.Toast
import by.dzmitry_lakisau.aimp_database_counter.database.DatabaseWorker
import by.dzmitry_lakisau.aimp_database_counter.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    private val dateRanges: LinkedHashMap<String, Float> = linkedMapOf(
        "12.07.2016 00:00:00" to 42563F,
        "31.07.2016 18:21:00" to 42582.7645833333F,
        "27.08.2016 00:00:00" to 42609F,
        "07.01.2017 00:00:00" to 42742F,
        "16.09.2017 00:00:00" to 42994F,
        "22.02.2018 00:00:00" to 43153F,
        "26.02.2018 00:00:00" to 43157F,
        "27.02.2018 00:00:00" to 43158F,
        "22.06.2018 00:00:00" to 43273F,
        "23.06.2018 00:00:00" to 43274F,
        "17.08.2018 00:00:00" to 43329F,
        "18.08.2018 00:00:00" to 43330F,
        "15.10.2018 00:00:00" to 43388F,
        "18.10.2018 00:00:00" to 43391F
    )

    var result = String()

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val databaseWorker = DatabaseWorker(this)

        textView_result.movementMethod = ScrollingMovementMethod()

        databaseWorker.count(dateRanges)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    result += "$it\n"
                },
                {
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                },
                {
                    textView_result.text = result
                }
            )
    }
}
