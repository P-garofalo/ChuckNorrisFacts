/**
 * App inspired by
 * https://medium.com/@ssaurel/develop-a-chuck-norris-facts-android-app-with-kotlin-d0d7b14e98dd
 */
package com.philgarofalo.chucknorrisfacts

import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import android.media.MediaPlayer
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.os.AsyncTask
import org.json.JSONObject
import org.json.JSONException
import android.os.Build
import android.os.Handler
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.ImageView
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    // The URL of the web service. You can test this and see the returned output
    // by entering it into a web browser.
    val jokesUrl = "https://api.icndb.com/jokes/random"
    lateinit var chuckPic: ImageView
    lateinit var chuckFactTextView: TextView
    var random = Random()
    lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        chuckPic = findViewById(R.id.chuck_image)
        chuckFactTextView = findViewById(R.id.chuck_fact)
        // The following line, along with android:scrollbars="vertical"
        // in the layout file, enables scrolling large text.
        chuckFactTextView.setMovementMethod(ScrollingMovementMethod())
        // Add roundhouse kick sound
        mediaPlayer = MediaPlayer.create(this, R.raw.round_house_kick)
    }

    /**
     * chuckTap responds to taps on Chuck's image view.
     *
     * @param v
     */
    fun chuckTap(v: View?) {
        if (random.nextInt(3) == 0) {
            anotherFact(v)
        } else {
            // roundhouse kick to the face
            if (random.nextInt(2) == 0) chuckPic.setImageResource(R.drawable.chuck_norris_roundhouse_kick) else chuckPic.setImageResource(
                R.drawable.chuck_norris_roundhousekick_right
            )
            mediaPlayer.start()
            val handler = Handler()
            handler.postDelayed(
                { chuckPic.setImageResource(R.drawable.chuck_norris_photo) },
                500
            )
        }
    }

    /**
     * anotherFact gets another Chuck Norris fact
     * @param v - the button's view object
     */
    fun anotherFact(v: View?) {
        Log.d(TAG, "anotherFact: ")
        FetchFactsTask().execute(jokesUrl)
    }

    /*
     * FetchFactsTask is an AsyncTask that runs in a separate worker (background) thread,
     * the retrieval of the fact JSON data from the web service.
     */
    private /*abstract*/ inner class FetchFactsTask : AsyncTask<String?, Void?, JSONObject?>() {
        /**
         * doInBackground is the method that does the actual work of the thread, in the background.
         *
         * @param params - the parameters that get passed in the execute method call.
         * @return - the returned object that is passed to onPostExecute.
         */
        protected override fun doInBackground(vararg params: String?): JSONObject? {
            var urlConnection: HttpURLConnection? = null
            try {
                val url = URL(params[0])
                urlConnection = url.openConnection() as HttpURLConnection
                val out = ByteArrayOutputStream()
                val `in` = urlConnection.inputStream
                if (urlConnection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw IOException(urlConnection.responseMessage + ": with " + url.toString())
                }
                var bytesRead: Int
                val buffer = ByteArray(1024)
                while (`in`.read(buffer).also { bytesRead = it } > 0) {
                    out.write(buffer, 0, bytesRead)
                }
                out.close()
                return JSONObject(out.toString())
            } catch (e: IOException) {
                Log.e(TAG, "getJsonFromServer: HTTP call error", e)
            } catch (e: JSONException) {
                Log.e(TAG, "getJsonFromServer: JSON parsing error", e)
            } finally {
                urlConnection?.disconnect()
            }
            return null
        }

        /**
         * onPostExecute gets called after doInBackground ends and is run on the main UI thread.
         * You can safely reference and set UI widgets in this method.
         *
         * @param jsonObject
         */
        override fun onPostExecute(jsonObject: JSONObject?) {
            super.onPostExecute(jsonObject)
            try {
                // parse the JSON object for the "joke" string
                if (jsonObject == null) throw JSONException("No joke data returned")

                // Extract the fact HTML string from the JSON formatted string
                val jsonObjValue = jsonObject.getJSONObject("value")
                val chuckFactStr = jsonObjValue.getString("joke")

                // Convert the HTML into a "spannable", i.e. formatted, string for the TextView.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    chuckFactTextView.text =
                        Html.fromHtml(chuckFactStr, Html.FROM_HTML_MODE_LEGACY)
                } else {
                    chuckFactTextView.text = Html.fromHtml(chuckFactStr)
                }
            } catch (e: JSONException) {
                Log.e(TAG, "anotherFact: Couldn't get the joke", e)
            }
        }
    }
}