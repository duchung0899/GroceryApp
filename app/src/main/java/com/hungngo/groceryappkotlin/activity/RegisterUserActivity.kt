package com.hungngo.groceryappkotlin.activity

import android.Manifest
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.hungngo.groceryappkotlin.MyCommon
import com.hungngo.groceryappkotlin.R
import com.hungngo.groceryappkotlin.model.User
import kotlinx.android.synthetic.main.activity_register_seller.*
import kotlinx.android.synthetic.main.activity_register_user.*
import kotlinx.android.synthetic.main.activity_register_user.edt_register_user_fullname
import java.util.*

class RegisterUserActivity : AppCompatActivity(), View.OnClickListener, LocationListener {

    // permission arrays
    private var locationPermission: Array<String>? = null
    private var cameraPermission: Array<String>? = null
    private var storagePermission: Array<String>? = null

    // image picked uri
    private var imageUri: Uri? = null

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private lateinit var locationManager: LocationManager

    private lateinit var progressdialog: ProgressDialog
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_user)

        // init permission array
        locationPermission = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        cameraPermission = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        mAuth = FirebaseAuth.getInstance()

        progressdialog = ProgressDialog(this)
        progressdialog.setTitle("Please wait...")
        progressdialog.setCanceledOnTouchOutside(false)

        img_rgt_user_back.setOnClickListener(this)
        img_register_location.setOnClickListener(this)
        img_user.setOnClickListener(this)
        btn_user_register.setOnClickListener(this)
        tv_register_seller.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.img_rgt_user_back -> {
                onBackPressed()
            }
            R.id.img_register_location -> {
                // detect current location
                if (checkLocationPermission()){
                    // already allowed
                    detectLocation()
                }else{
                    requestLocationPermission()
                }
            }
            R.id.img_user -> {
                // pick user image
                showImagePickDialog()
            }
            R.id.btn_user_register -> {
                // register user
                registerBuyer()
            }
            R.id.tv_register_seller -> {
                Intent(this, RegisterSellerActivity::class.java).also {
                    startActivity(it)
                }
            }
        }
    }

    private lateinit var password: String
    private fun registerBuyer() {
        val fullName = edt_register_user_fullname.text.toString().trim()
        val phoneNum = edt_user_phone.text.toString().trim()
        val address = edt_user_complete_address.text.toString().trim()
        val country = edt_user_country.text.toString().trim()
        val state = edt_user_state.text.toString().trim()
        val city = edt_user_city.text.toString().trim()
        val email = edt_user_email.text.toString().trim()
        password = edt_user_password.text.toString().trim()
        val rePassword = edt_user_repassword.text.toString().trim()

        val timeStamp = System.currentTimeMillis()

        val buyer = User(fullName as String?, null, phoneNum as String?, email as String?,
            null , country as String?, state as String?, city as String?,
            address as String?, latitude  as Double?,longitude  as Double?, timeStamp as Long?,
            true, true, null, MyCommon.BUYER, null)

        if(TextUtils.isEmpty(fullName)){
            Toast.makeText(this, "Enter full name", Toast.LENGTH_SHORT).show()
            return
        }
        if(TextUtils.isEmpty(phoneNum)){
            Toast.makeText(this, "Enter phone number", Toast.LENGTH_SHORT).show()
            return
        }
        if(longitude == 0.0 && latitude == 0.0){
            Toast.makeText(
                this,
                "Click GPS icon to get your current location or enter your location by yourself",
                Toast.LENGTH_SHORT
            ).show()

            return
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid email pattern...", Toast.LENGTH_SHORT).show()
            return
        }
        if(password.length < 6){
            Toast.makeText(this, "Password must be at least 6 character", Toast.LENGTH_SHORT).show()
            return
        }
        if(password != rePassword){
            Toast.makeText(this, "Password doesn't match", Toast.LENGTH_SHORT).show()
            return
        }

        createBuyerAccount(buyer)
    }

    private fun createBuyerAccount(buyer: User) {
        progressdialog.setTitle("Creating account...")
        progressdialog.show()

        // create account in firebase authentication
        mAuth.createUserWithEmailAndPassword(buyer.email!!, password)
            .addOnSuccessListener {
                // account created successfully
                saveAccountToFirebase(buyer)
            }
            .addOnFailureListener{
                // account created failed
                progressdialog.dismiss()
                Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveAccountToFirebase(buyer: User) {
        // after account created, update uid to buyer
        buyer.uid = mAuth.uid

        progressdialog.setTitle("Saving info...")

        // name and path of image
        val filePathAndName = "profile_images/${mAuth.uid}"
        // upload image to storage and seller to db
        if (imageUri == null){
            // upload buyer without image
            saveSellerToFirebase(buyer)
        }else{
            // upload seller with image
            val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
            storageReference.putFile(imageUri!!)
                .addOnSuccessListener {
                    val uriTask = it.storage.downloadUrl
                    while (!uriTask.isSuccessful){}
                    val downloadImageUri = uriTask.result
                    if (uriTask.isSuccessful) {
                        // update profileImage to buyer
                        buyer.profileImage = downloadImageUri.toString()
                        //save seller to firebase
                        saveSellerToFirebase(buyer)
                    }
                }
                .addOnFailureListener {
                    progressdialog.dismiss()
                    Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveSellerToFirebase(seller: User){
        val sellerRef = FirebaseDatabase.getInstance().getReference("Users")
        sellerRef.child(mAuth.uid!!).setValue(seller)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    progressdialog.dismiss()
                    startActivity(Intent(this@RegisterUserActivity, MainBuyerActivity::class.java))
                    finish()
                }else {
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // show choice for taking image
    private fun showImagePickDialog() {
        // options to display
        val options = arrayOf("Camera", "Galley")

        // dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Image")
            .setItems(options) { _, which ->
                // handle clicks
                handlerPickedFromDialog(which)
            }
            .show()
    }

    private fun handlerPickedFromDialog(choice: Int){
        if (choice == 0) {
            // camera clicked
            if(checkCameraPermission()){
                // camera permissions allowed
                pickFromCamera()
            }else{
                // not allowed, request
                requestCameraPermission()
            }
        } else {
            // gallery clicked
            if(checkStoragePermission()){
                // storage permissions allowed
                pickFromGallery()
            }else{
                // not allowed, request
                requestStoragePermission()
            }
        }
    }

    // pick image from gallery
    // check permission to access gallery
    private  fun checkStoragePermission(): Boolean{
        return ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
    // if permission is not accepted or has not accepted, request permission
    private fun requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermission!!, MyCommon.STORAGE_REQUEST_CODE)
    }
    // permission allowed, pick image from gallery
    private fun pickFromGallery(){
        Intent(Intent.ACTION_PICK).also {
            it.type = "image/*"
            startActivityForResult(it, MyCommon.IMAGE_PICK_GALLERY_CODE)
        }
    }

    private  fun checkCameraPermission(): Boolean{
        val result1 =  ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        val result2 =  ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        return result1 && result2
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission!!, MyCommon.CAMERA_REQUEST_CODE)
    }

    private fun pickFromCamera(){
        val contentValues: ContentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image Title")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image Description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
            it.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(it, MyCommon.IMAGE_PICK_CAMERA_CODE)
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this, locationPermission!!,
            MyCommon.LOCATION_REQUEST_CODE
        )
    }

    private fun detectLocation() {
        Toast.makeText(this, "Please wait...", Toast.LENGTH_LONG).show()

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if(!checkLocationPermission())
            return
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        when(requestCode){
            MyCommon.LOCATION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    val locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if (locationAccepted) {
                        // permission allowed
                        detectLocation()
                    } else {
                        // permission denied
                        Toast.makeText(this, "Location permission is necessary...", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            MyCommon.CAMERA_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                    if (cameraAccepted && storageAccepted) {
                        // permission allowed
                        pickFromCamera()
                    } else {
                        // permission denied
                        Toast.makeText(this, "Camera permission is necessary...", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            MyCommon.STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                    if (storageAccepted) {
                        // permission allowed
                        pickFromGallery()
                    } else {
                        // permission denied
                        Toast.makeText(this, "Storage permission is necessary...", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK){

            if(requestCode == MyCommon.IMAGE_PICK_GALLERY_CODE){
                // get picked image
                imageUri = data?.data!!
                // set to imageView
                img_user.setImageURI(imageUri)
            } else if (requestCode == MyCommon.IMAGE_PICK_CAMERA_CODE){
                img_user.setImageURI(imageUri)
            }

        }

        super.onActivityResult(requestCode, resultCode, data)
    }

//    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
//        super.onStatusChanged(provider, status, extras)
//    }
//
//    override fun onProviderEnabled(provider: String) {
//        super.onProviderEnabled(provider)
//    }

    override fun onProviderDisabled(provider: String) {
        // gps/location disabled
        Toast.makeText(this, "Please turn on location...", Toast.LENGTH_SHORT).show()
    }

    override fun onLocationChanged(location: Location) {
        // location detected
        latitude = location.latitude
        longitude = location.longitude

        findAddress()
    }

    private fun findAddress() {
        // find address, country, state, city
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses: List<Address>
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1)

            val address: String = addresses[0].getAddressLine(0)  // complete address
            val city: String = addresses[0].locality
            val state: String = addresses[0].adminArea
            val country: String = addresses[0].countryName

            // set addresses
            edt_user_complete_address.setText(address)
            edt_user_city.setText(city)
            edt_user_country.setText(country)
            edt_user_state.setText(state)

        }catch (e: Exception){
            Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

}