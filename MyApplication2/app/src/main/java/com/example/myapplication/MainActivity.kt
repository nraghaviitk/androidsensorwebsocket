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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var sensor: Sensor? = null
    val gravity = FloatArray(3)
    val linear_acceleration= FloatArray(3)
//    var disString: MutableList<String
//    val disString= ArrayList<String>()
    var disString= ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        setContent {
            MyApplicationTheme {
            //    Log.d("oncreate","$disString")
                // A surface container using the 'background' color from the theme
             //if(disString!=null) {
          //       if(disString!=null) {
            //     MyApp(disString)
             //}else{
//                MyApp(arrayListOf("Nallani","Raghav",disString.get(0),disString.get(1),disString.get(2)))
                MyApp(arrayListOf("Nallani","Raghav"))
              //  Log.d("oncreate disString","$disString")
                //MyApp(disString)
             //}
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sensor?.also{gyros ->
            sensorManager.registerListener(this, gyros,SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
 //       TODO("Not yet implemented")
        if(event!=null) {
            var axisX: Float = event.values[0]
            gravity[0]=axisX
            var axisY: Float = event.values[1]
            var axisZ: Float = event.values[2]
            var xas=axisX.toString()
            var xbs=axisY.toString()
            var xcs=axisZ.toString()
            var gyrov = xas+"i "+xbs+"j "+xcs+"k"
            /*
            disString.add(0,"$xas")
            disString.add(1,"$xbs")
            disString.add(2,"$xcs")
            */

            Log.d("sensorchaged gyrov","$gyrov")

       //     Log.d("sensorchaged disString","$disString")
            //Greeting(name = gyrov)
            setContent {
                MyApplicationTheme {
//                    MyApp(arrayListOf("inside","override"))
                    MyApp(arrayListOf("$gyrov"))
                    //card("$gyrov")
                    //Log.d("inside override","$disString[0]")
                    Log.d("sensor type","${event.sensor.name}")
                }
            }
            //call a function which has setcontent
            // and adds the string or updates a list and shows the value
        }
    //make the string here only and
    // addit to the list to pass it to composable where it is iterated.
    }

    override fun onAccuracyChanged(sensor: Sensor?, p1: Int) {

    }

}



@Composable
fun MyApp(names: ArrayList<String>) {

    var shouldShowOnboarding = remember { mutableStateOf(true) }

    if (shouldShowOnboarding.value) {
        OnboardingScreen(onContinueClicked = { shouldShowOnboarding.value = false })
    } else {
        Greetings(names)
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
private fun Greetings(names: ArrayList<String>) {

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        for (name in names) {
            Greeting(name = name)
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
private fun Greeting(name: String) {

    val expanded = remember { mutableStateOf(false) }

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
                Text(text = "Hello, ")
                Text(text = name)
            }
            OutlinedButton(
                onClick = { expanded.value = !expanded.value }
            ) {
                Text(if (expanded.value) "Show less" else "Show more")
            }
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
        Greetings(arrayListOf("Nallani","Raghav"))
        card("yeah this one too")
    }
}

