package com.tg.jitpacksample

import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.tg.jitpack_lib.TestTgBobfSdk
//import tg.sdk.sca.presentation.ui.enrollment.TgDashboardActivity
//import tg.sdk.sca.tgbobf.TgBobfSdk

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        TestTgBobfSdk.init(application)

//        TgBobfSdk.init(application)
//
//        if (!TgBobfSdk.isUserEnrolled()) {
//            // fetch userToken and pass it as parameter
//            startActivity(
//                TgDashboardActivity.getEnrollmentLaunchIntent(
//                    context = this,
//                    userToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkiLCJpc3MiOiJodHRwczovL2F1dGhzZXJ2ZXIuYmFuay5jb20iLCJleHAiOjE2MTc3NDgxOTEsImlhdCI6MTYxNzc0NDU5MX0.C3wCSJ4b6ZxrCeSbNh3J1ZjK2E6nwJNUIXGx1yPwYCDg7sPPb9AVx5UR0tSjMoXIHxw0boLrht5oCNvIf-ZzEwQ_8rc7nGz7kZnWKY-ShtGjshGE01GHCMQugBgwcbtvoLg09Vrvfe64nWURFYNFgys3sDehKdJbkjSl661DzKOMDxiWXob1e-CU-IT4p-uqgcmI1S-Ir_FcW6qs8Qce1M--v4j7slifRh6Tl7ztDKPydzKt5viTtZzJNf6foZeGYmoNAOB4OTNmD9NztCbg7-t9lfypBt1sao3ZxKlAzMLuZdLQ1KzqCDJq7pbcBNm_-h5N7UqE1Jy6hI3k9FiRfQ"
//                )
//            )
//        } else {
//            startActivity(
//                TgDashboardActivity.getDashboardLaunchIntent(
//                    context = this
//                )
//            )
//        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}