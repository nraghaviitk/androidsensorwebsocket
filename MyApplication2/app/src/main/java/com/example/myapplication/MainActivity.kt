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

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    val WEB_SOCKET_URL = "ws://192.168.1.8:3000/"

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

        setContent {
            MyApplicationTheme {
                //    Log.d("oncreate","$disString")
                // A surface container using the 'background' color from the theme
                //if(disString!=null) {
                //       if(disString!=null) {
                //     MyApp(disString)
                //}else{
//                MyApp(arrayListOf("Nallani","Raghav",disString.get(0),disString.get(1),disString.get(2)))
                MyApp(arrayListOf("Nallani", "Raghav"), "Gravity")
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
    /*
    private fun initWebSocket() {
        val coinbaseUri: URI? = URI(WEB_SOCKET_URL)

        createWebSocketClient(coinbaseUri)

        val socketFactory: SSLSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
        webSocketClient.setSocketFactory(socketFactory)
        webSocketClient.connect()
    }
    private fun createWebSocketClient(coinbaseUri: URI?) {
        Log.d("createws called","websoc create")

        webSocketClient = object : WebSocketClient(coinbaseUri) {
        //webSocketClient = WebSocketClient(coinbaseUri) {

            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d(TAG, "onOpen")
                subscribe()
            }

            override fun onMessage(message: String?) {
                Log.d(TAG, "onMessage: $message")
                setUpBtcPriceText(message)
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d(TAG, "onClose")
                //unsubscribe()
            }

            override fun onError(ex: Exception?) {
                Log.e(TAG, "onError: ${ex?.message}")
            }

        }
    }
    private fun setUpBtcPriceText(message: String?) {
        message?.let {
            val moshi = Moshi.Builder().build()
//            val adapter: JsonAdapter<BitcoinTicker> = moshi.adapter(BitcoinTicker::class.java)
            val adapter: JsonAdapter<servermessage> = moshi.adapter(servermessage::class.java)

            val serverdata = adapter.fromJson(message)
            setContent {
                MyApplicationTheme {
                    MyApp(arrayListOf("${serverdata!!.message}"),serverdata!!.name)
             //       Log.d("sensor name","${serverdata!!.name}")
              //      Log.d("sensor type","${serverdata!!.message}")
                }
            }
          //  runOnUiThread { btc_price_tv.text = "1 BTC: ${bitcoin?.price} €" }
          //  runOnUiThread { gyrotext.text = "${gravity}" }

        }
    }
    private fun subscribe() {
        /*webSocketClient.send(
            "{\n" +
                    "    \"type\": \"subscribe\",\n" +
                    "    \"channels\": [{ \"name\": \"ticker\", \"product_ids\": [\"BTC-EUR\"] }]\n" +
                    "}"
        ) */
        webSocketClient.send(
            "{"+"\"Android\": \"0\""+"}"
                )
    }
    companion object {
        //const val WEB_SOCKET_URL = "wss://ws-feed.pro.coinbase.com"
        const val WEB_SOCKET_URL = "ws://192.168.1.8:3000/"

        const val TAG = "Coinbase"
    }*/
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
        var gyrov:String=""
        var lightv : String=""
        var gravv:String=""
        if (event!!.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event!!.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event!!.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }
/*
    for (i in 0..2){
        Log.d("magreading","${magnetometerReading[i]}")
        Log.d("accreading","${accelerometerReading[i]}")
        }*/
    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    fun updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(
            rotationMatrix, null,
            accelerometerReading,
            magnetometerReading
        )
/*
        for (i in 0..8){
            Log.d("rotmatrix${i}","${rotationMatrix[i]}")
        }
        // "rotationMatrix" now has up-to-date information.
        */
        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        // "orientationAngles" now has up-to-date information.
    }
        lateinit var theta:String
        if(accelerometerReading!=null && magnetometerReading!=null) {
            updateOrientationAngles()
            //var theta : String()
            var theta0 = orientationAngles[0].toString()
            var theta1 = orientationAngles[1].toString()
            var theta2 = orientationAngles[2].toString()
            theta = theta0 + "i " + theta1 + "j " + theta2 + "k"
          //  Log.d("orientation angles:", "${theta}")
        }else{
             theta = "not done yet"
        }

 /*   if(event!=null && event.sensor.type==Sensor.TYPE_GYROSCOPE) {
            var axisX: Float = event.values[0]
//            gyrovalues[0]=axisX

            var axisY: Float = event.values[1]
  //          gyrovalues[1]=axisY
            var axisZ: Float = event.values[2]
    //        gyrovalues[2]=axisZ
            var xas=axisX.toString()
            var xbs=axisY.toString()
            var xcs=axisZ.toString()
            gyrov = "gyro:"+xas+"i "+xbs+"j "+xcs+"k"
            Log.d("sensorchaged gyrov","$gyrov")
            gyrovalues=lowpass(event.values.clone(),gyrovalues)
            val lpstring=gyrovalues[0].toString()+" "+gyrovalues[1].toString()
            Log.d("Lowpassed","${lpstring}")
            //call a function which has setcontent
            // and adds the string or updates a list and shows the value
//        } else if(event!=null && event.sensor.type==65604){
        } else if(event!=null && event.sensor.type==Sensor.TYPE_LIGHT){

            var lux = event.values[0].toString()
            lightv="lightsensor:"+lux
            Log.d("sensorchaged lightv","$lightv")
        }else if(event!=null && event.sensor.type==Sensor.TYPE_GRAVITY){

            var axisX: Float = event.values[0]
            var axisY: Float = event.values[1]
            var axisZ: Float = event.values[2]
            var xas=axisX.toString()
            var xbs=axisY.toString()
            var xcs=axisZ.toString()
            gravv = "grav:"+xas+"i "+xbs +"j "+xcs+"k"
            Log.d("sensorchaged gravv","$gravv")
        }
   */     setContent {
            MyApplicationTheme {
//                    MyApp(arrayListOf("inside","override"))
//                MyApp(arrayListOf("$gyrov","$lightv","$gravv"))
                //if(gyrovalues!=event?.values?.clone()){
               // MyApp(arrayListOf("$lightv","$gravv","$gyrov"),event!!.sensor.name)
                MyApp(arrayListOf("$theta"),event!!.sensor.name)
                val sockdata="{\"0\":\"${theta}\""
                //webSocket.send(sockdata)

                //}
                //card("$gyrov")
                //Log.d("inside override","$disString[0]")
            //    Log.d("sensor name","${event?.sensor?.name}")
            //    Log.d("sensor type","${event?.sensor?.type}")
            }
        }
    //make the string here only and
    // addit to the list to pass it to composable where it is iterated.
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
        webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        output("Receiving : " + text!!)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        output("Receiving bytes : " + bytes!!.hex())
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

