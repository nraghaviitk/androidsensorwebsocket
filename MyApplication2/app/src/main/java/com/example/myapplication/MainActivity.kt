package com.example.myapplication

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import com.example.myapplication.ui.theme.MyApplicationTheme
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI
import javax.net.ssl.SSLSocketFactory
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import okhttp3.*
import okio.ByteString
import java.util.concurrent.TimeUnit
import com.example.myapplication.SensordataViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach


class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var gyrose: Sensor? = null
    private var lightse: Sensor? = null
    private var gravse: Sensor? = null
    private var magse: Sensor? = null
    private var accse: Sensor? = null
  //  private lateinit var webSocketClient: WebSocketClient

    val ALPHA = 0.5f
    var gravity = FloatArray(3)
    var gyrovalues = FloatArray(3)
    var disString = ArrayList<String>()
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    //Channel set up for sending events from main to dispachers
    private val scope = CoroutineScope(Dispatchers.Default)
    private val events = Channel<SensorEvent>(100)

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    val WEB_SOCKET_URL = "ws://192.168.1.8:3000/"
    private val sensorvm=SensordataViewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        /*   gyrose = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        lightse = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        gravse = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
*/        magse = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        accse = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val deviceSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)
        //initiateSocketConnection()

        val client = OkHttpClient()/*.Builder()
            .readTimeout(3, TimeUnit.SECONDS)
            //.sslSocketFactory() - ? нужно ли его указывать дополнительно
            .build()*/
        val request = Request.Builder().url(WEB_SOCKET_URL).build()
        val wsListener = EchoWebSocketListener()
        val webSocket = client.newWebSocket(request, wsListener)
        val rmsobserve =Observer<Float> { rmsangle ->
            if (rmsangle > 2.0) {
                val espdata = "{" + "\"0\": \"0\",\"1\":\"${sensorvm.azimuth.value.toString()}\"" + "}"
                webSocket.send(espdata)
            }


            setContent {
                MyApplicationTheme {
                    //  MyApp(arrayListOf("Nallani","Raghav",disString.get(0),disString.get(1),disString.get(2)))
                    MyApp(
                        arrayListOf(
                            sensorvm.azimuth!!.value.toString(),
                            sensorvm.pitch!!.value.toString(),
                            sensorvm.roll!!.value.toString()
                        ),
                        sensorvm.eventid.value.toString()
                    )
                    //  Log.d("oncreate disString","$disString")
                    //MyApp(disString)
                    //}
                    for (i in deviceSensors) {
                        //      Log.d("we have","${i.name}")
                        //     Log.d("type","${i.type}")
                        //          Log.d("id","${i.id}")


                    }
                }
            }
        }
        sensorvm.rmsangle.observe(this,rmsobserve)

    }


    override fun onResume() {
        super.onResume()
       // initWebSocket()
  /*      gyrose?.also{gyros ->
            sensorManager.registerListener(this, gyros,SensorManager.SENSOR_DELAY_NORMAL)
        }
        gravse?.also{grav ->
            sensorManager.registerListener(this, grav,SensorManager.SENSOR_DELAY_NORMAL)
        }
        lightse?.also{light ->
            sensorManager.registerListener(this, light,SensorManager.SENSOR_DELAY_NORMAL)
        }
    */    magse?.also{mag ->
            sensorManager.registerListener(this, mag,SensorManager.SENSOR_DELAY_NORMAL)
        }
        accse?.also{acc ->
            sensorManager.registerListener(this, acc,SensorManager.SENSOR_DELAY_NORMAL)
        }

    }
    override fun onPause() {
        super.onPause()
      /*  sensorManager.unregisterListener(this,gravse)
        sensorManager.unregisterListener(this,lightse)
        sensorManager.unregisterListener(this,gyrose)
        */sensorManager.unregisterListener(this,magse)
        sensorManager.unregisterListener(this,accse)

    }

    fun lowpass(input:FloatArray, output:FloatArray):FloatArray{
     /*   Log.d("inlowpass input size","${input.size}")
        Log.d("inlowpass output size","${output.size}")
        Log.d("input[0]","${input[0]}")
        Log.d("output[0]","${output[0]}")
        */
        if (output == null){ return input}else{

        for (i in 0..2) {
//            output.get(i) = output.get(i) + ALPHA * (input.get(i) - output.get(i))
            output[i] = output[i] + ALPHA * (input[i] - output[i])
        }
        return output}
    }
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { offer(it) }
    //    process()
    }
//    private fun offer(event: SensorEvent) = runBlocking { events.send(event)
    //process()
    private fun offer(event: SensorEvent) {
   // scope.launch(Dispatchers.Default) {
    runBlocking {

        events.send(event)
    }

    scope.launch {

    //fun process() = scope.launch {
        events.consumeEach { event ->
            // Do something

            var gyrov: String = ""
            var lightv: String = ""
            var gravv: String = ""
            if (event!!.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(
                    event.values,
                    0,
                    accelerometerReading,
                    0,
                    accelerometerReading.size
                )
            } else if (event!!.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(
                    event!!.values,
                    0,
                    magnetometerReading,
                    0,
                    magnetometerReading.size
                )
            }

            fun updateOrientationAngles() {
                // Update rotation matrix, which is needed to update orientation angles.
                SensorManager.getRotationMatrix(
                    rotationMatrix, null,
                    accelerometerReading,
                    magnetometerReading
                )

                SensorManager.getOrientation(rotationMatrix, orientationAngles)


            }

            lateinit var theta: String
            if (accelerometerReading != null && magnetometerReading != null) {
                updateOrientationAngles()
                //var theta : String()
                var theta0 = orientationAngles[0].toString()
                var theta1 = orientationAngles[1].toString()
                var theta2 = orientationAngles[2].toString()
                theta = theta0 + "i " + theta1 + "j " + theta2 + "k"
                 // Log.d("orientation angles:", "${theta}")

            } else {
                theta = "not done yet"
            }
/*
            sensorvm.updateAngle(
                orientationAngles,
                event.timestamp.toFloat(),
                event.sensor.name.toString()
            )
  */
            sensorvm.rms(orientationAngles, event.timestamp.toFloat(),event.sensor.name.toString())


//make the string here only and
// addit to the list to pass it to composable where it is iterated.

        }
    }
}
    override fun onAccuracyChanged(sensor: Sensor?, p1: Int) {

    }

}
private class EchoWebSocketListener : WebSocketListener() {
    override fun onOpen(webSocket: WebSocket, response: Response) {
       // webSocket.send("Hello, it's SSaurel !")
        val idforserv = "{"+"\"0\": \"M20\",\"1\":\"0\""+"}"
        webSocket.send("${idforserv}")

//        webSocket.send("What's up ?")
        // webSocket.send(ByteString.decodeHex("deadbeef"))
        //webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        output("Receiving : " + text!!)
    }

    //override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
      //  output("Receiving bytes : " + bytes!!.hex())
    //}

    fun onMessage(webSocket: WebSocket, sermess:servermessage) {
        output("message:"+sermess!!.message!!.toString()+"sender:"+sermess!!.name!!.toString())
//        output("sender : " + sermess!!.name!!.toString())
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket!!.close(NORMAL_CLOSURE_STATUS, null)
        output("Closing : $code / $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        output("Error : " + t.message)
    }

    companion object {
        private val NORMAL_CLOSURE_STATUS = 1000
    }

    private fun output(txt: String) {
        Log.v("WSS", txt)
    }
}


@Composable
fun MyApp(names: ArrayList<String>,eventname:String) {

    var shouldShowOnboarding = remember { mutableStateOf(true) }

    if (shouldShowOnboarding.value) {
        OnboardingScreen(onContinueClicked = { shouldShowOnboarding.value = false })
    } else {
        Greetings(names,eventname)
    }
}

@Composable
fun OnboardingScreen(onContinueClicked: () -> Unit) {

    Surface {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome to the Basics Codelab!")
            Button(
                modifier = Modifier.padding(vertical = 24.dp),
                onClick = onContinueClicked
            ) {
                Text("Continue")
            }
        }
    }
}

@Composable
//private fun Greetings(names: List<String> = listOf("World", "Compose")) {
private fun Greetings(names: ArrayList<String>,eventname: String) {

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        for (name in names) {
            Greeting(name = name,eventname)
        }
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 320)
@Composable
fun OnboardingPreview() {
    MyApplicationTheme {
        OnboardingScreen(onContinueClicked = {})
    }
}

@Composable
private fun Greeting(name: String,eventname: String) {
    var textname = remember(key1 = eventname) {name}


    val expanded = remember { mutableStateOf(false) }
    //var namestring = rememberSaveable{mutableStateOf(name)}
    val extraPadding = if (expanded.value) 48.dp else 0.dp

    Surface(
        color = MaterialTheme.colors.primary,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Row(modifier = Modifier.padding(24.dp)) {
            Column(modifier = Modifier
                .weight(1f)
                .padding(bottom = extraPadding)
            ) {
             //   Text(text = "Hello, ")
                //   Text(text = namestring.value)
               Text(text = textname)
            }
        /*
            OutlinedButton(
                onClick = { expanded.value = !expanded.value }
            ) {
                Text(if (expanded.value) "Show less" else "Show more")
            }*/
        }
    }
}
@Composable
private fun card(name: String) {

   // val expanded = remember { mutableStateOf(false) }

 //   val extraPadding = if (expanded.value) 48.dp else 0.dp
    val extraPadding=0.dp
    Surface(
        color = MaterialTheme.colors.primary,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Row(modifier = Modifier.padding(24.dp)) {
            Column(modifier = Modifier
                .weight(1f)
                .padding(bottom = extraPadding)
            ) {
   //             Text(text = "Hello, ")
                Text(text = name)
            }
     /*       OutlinedButton(
                onClick = { expanded.value = !expanded.value }
            ) {
                Text(if (expanded.value) "Show less" else "Show more")
            }*/
        }
    }
}
@Preview(showBackground = true, widthDp = 320)
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        Greetings(arrayListOf("Nallani","Raghav"),"Gravity")
        card("yeah this one too")
    }
}

