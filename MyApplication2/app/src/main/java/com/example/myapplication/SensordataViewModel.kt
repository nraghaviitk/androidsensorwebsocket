package com.example.myapplication

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.lang.Math.*
import kotlin.math.pow
import kotlin.math.sqrt

class SensordataViewModel: ViewModel() {

    private var _azimuth=MutableLiveData<Float>()
    val azimuth : LiveData<Float> =_azimuth

    private var _pitch=MutableLiveData<Float>()
    val pitch : LiveData<Float> =_pitch

    private var _roll=MutableLiveData<Float>()
    val roll : LiveData<Float> =_roll

    private var _timestamp=MutableLiveData<Float>()
    val timestamp : LiveData<Float> =_timestamp

    private var _eventid=MutableLiveData<String>()
    val eventid : LiveData<String> =_eventid

    private var _rmsangle = MutableLiveData<Float>()
    val rmsangle : LiveData<Float> = _rmsangle

//    fun updateAngle(az:Float,pi:Float,ro:Float){
    fun updateAngle(orientAngles: FloatArray,timest: Float,eid:String){
    viewModelScope.launch {

        //Log.d("eid in vm","${eid}")
        //Log.d("oangle 0 in vm","${orientAngles[0].toString()}")

    }

      }
    fun rms(orientAngles:FloatArray,timest:Float,eid:String) {
        viewModelScope.launch {
            val PIconst = 180.toFloat()/ PI
            if(timestamp.value==null){
                _azimuth.value = orientAngles[0]*PIconst.toFloat()
                _pitch.value = orientAngles[1]*PIconst.toFloat()
                _roll.value = orientAngles[2]*PIconst.toFloat()
                _timestamp.value = timest
                _eventid.value = eid
            }else if (timest != timestamp.value) {
                _rmsangle.value =
                    (orientAngles[0]*PIconst.toFloat() - azimuth.value!!).pow(2) + (orientAngles[1]*PIconst.toFloat() - pitch.value!!).pow(
                        2
                    ) + (orientAngles[2]*PIconst.toFloat() - roll.value!!).pow(
                        2
                    )
                _rmsangle.value = sqrt(_rmsangle.value!!/3)
/*

  */
                if(rmsangle.value?.toFloat()!! > 0.5){
                    Log.d("inside vm rms", "${rmsangle.value}")
                    Log.d("inside vm oa0", "${orientAngles[0]}")
                    Log.d("post rmscheck azimuth ", "${azimuth.value}")
                    Log.d("inside vm vmts", "${timestamp.value}")
                    Log.d("inside vm ts", "${timest}")

                    _azimuth.value = orientAngles[0]*PIconst.toFloat()
                    _pitch.value = orientAngles[1]*PIconst.toFloat()
                    _roll.value = orientAngles[2]*PIconst.toFloat()
                    _timestamp.value = timest
                    _eventid.value = eid
                }

            }

            //get sensor timestamp, and values, for stable values wrt to time store it
            //and have them observed from websokcket
            // and send them.
        }
    }
}