package eu.tutorials.locationapp

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import eu.tutorials.locationapp.ui.theme.LocationAppTheme
import androidx.lifecycle.viewmodel.compose.viewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: LocationViewModel = viewModel()
            LocationAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MyApp(viewModel)
                }
            }
        }
    }
}
@Composable
fun MyApp(viewModel: LocationViewModel){
    val context = LocalContext.current
    val locationUtils = LocationUtils(context)
    LocationDisplay(viewModel = viewModel, locationUtils = locationUtils, context = context)
}


@Composable
fun LocationDisplay(
    locationUtils: LocationUtils,
    viewModel: LocationViewModel,
    context: Context
){
    val location = viewModel.location.value

    val address = location?.let{
        locationUtils.reversedGeocodeLocation(location)
    }

    //making permission launcher
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {permissions ->
            if(permissions[Manifest.permission.ACCESS_COARSE_LOCATION]==true
                &&
                permissions[Manifest.permission.ACCESS_FINE_LOCATION]==true){
                //user has location access
                locationUtils.requestLocationUpdates(viewModel = viewModel)
            }else{
                //ask for  location access
                val rationaleRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                    context as MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                if(rationaleRequired){
                    Toast.makeText(context,
                        "Location access is required for this feature to work", Toast.LENGTH_LONG)
                        .show()
                }
                else{
                    Toast.makeText(context,
                        "Location permission is required,Go to Android Settings", Toast.LENGTH_LONG)
                        .show()
                }

            }
        }
    )



    Column(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        if(location!=null){
            Text(text = "location:Latitude: ${location.latitude} Longitude: ${location.longitude}\n $address")
        }
        else{
            Text(text = "Location not available")
        }

        Button(onClick = {
            if(locationUtils.hasLocationAccess(context)){
                //permission is granted ,update the location
                locationUtils.requestLocationUpdates(viewModel)
            }
            else{
                //request permission location
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }) {
            Text(text = "Get location")
        }

    }
}
