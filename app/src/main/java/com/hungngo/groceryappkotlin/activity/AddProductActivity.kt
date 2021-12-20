package com.hungngo.groceryappkotlin.activity

import android.Manifest
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.hungngo.groceryappkotlin.MyCommon
import com.hungngo.groceryappkotlin.R
import com.hungngo.groceryappkotlin.firebase.FirebaseDAO
import com.hungngo.groceryappkotlin.model.Product
import kotlinx.android.synthetic.main.activity_add_product.*


class AddProductActivity : AppCompatActivity(), View.OnClickListener {

    // permission arrays
    private var cameraPermission: Array<String>? = null
    private var storagePermission: Array<String>? = null

    // image pick uri
    private var imageUri: Uri? = null

    // check add or update product
    private var isUpdate = false

    // product instant to save
    private var product: Product? = null

    private lateinit var mAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        // get default info
        // if add, no info, if update we have first info about product
        getDefaultInfo()

        mAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this).apply {
            title = "Please Wait..."
            setCanceledOnTouchOutside(false)
        }

        // init permission arrays
        cameraPermission = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        // set onclick
        img_add_product_back.setOnClickListener(this)
        img_product.setOnClickListener(this)
        tv_product_cate.setOnClickListener(this)
        btn_add_product.setOnClickListener(this)

        // check discount switch: if true: show discountPrice and discountNote, else hide it
        sw_discount.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                edt_discount_price.visibility = View.VISIBLE
                edt_discount_note.visibility = View.VISIBLE
            }else{
                edt_discount_price.visibility = View.GONE
                edt_discount_note.visibility = View.GONE
            }
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.img_add_product_back -> {
                startActivity(Intent(this, MainSellerActivity::class.java))
//                finish()
            }
            R.id.img_product -> {
                showImagePickDialog()
            }
            R.id.tv_product_cate -> {
                showCategoryDialog()
            }
            R.id.btn_add_product -> {
//                flow: input data -> valid data -> save data to db
                inputData()
            }
        }
    }

    private fun getDefaultInfo() {
        val intent = intent
        if (intent != null){
            val bundle = intent.extras
            if (bundle != null){
                // it's update, get product and show it
                isUpdate = true
                tv_add_edit.text = "Update Product"
                btn_add_product.text = "Update Product"

                // get product from adapter
                product = bundle.getSerializable("PRODUCT") as Product

                // show product
                edt_product_title.setText(product!!.productTitle)
                edt_product_des.setText(product!!.productDescription)
                tv_product_cate.text = product!!.productCategory
                edt_product_quantity.setText(product!!.productQuantity.toString())
                edt_product_price.setText(product!!.productPrice.toString())
                if (product!!.discountAvailable!!){
                    sw_discount.isChecked = true
                    edt_discount_price.visibility = View.VISIBLE
                    edt_discount_note.visibility = View.VISIBLE
                    edt_discount_price.setText(product!!.discountPrice.toString())
                    edt_discount_note.setText(product!!.discountNote.toString())
                }
                Glide.with(this).load(product!!.productIcon).into(img_product)
            }
        }
    }



    private fun inputData() {
        // input data
        val productTitle = edt_product_title.text.toString().trim()
        val productDescription = edt_product_des.text.toString().trim()
        val productCategory = tv_product_cate.text.toString().trim()

        // if edt_product_quantity.text.toString() == "", we can get error (for input string "") if cast it to Int
        // so before cast edt_product_quantity.text.toString() to Int, make sure it's not equal ""
        var productQuantity: Int = 0
        if (edt_product_quantity.text.toString() != "")
            productQuantity = edt_product_quantity.text.toString().trim().toInt()

        val productPrice = edt_product_price.text.toString().trim()
        val discountAvailable = sw_discount.isChecked  // true / false

        // if update, timestamp, productIcon, uid will get data from product, not system
        var timestamp: Long = System.currentTimeMillis()
        var productIcon: String? = null
        var uid = mAuth.uid
        if (isUpdate){
            timestamp = product!!.timestamp!!
            productIcon = product!!.productIcon
            uid = product!!.uid
        }

        // discountPrice's value and discountNote's value will change if switch is true
        var discountPrice = "0"
        var discountNote = ""

        // valid data
        if (TextUtils.isEmpty(productTitle)){
            Toast.makeText(this, "Title is required...", Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(productCategory)){
            Toast.makeText(this, "Category is required...", Toast.LENGTH_SHORT).show()
            return
        }
        if (productQuantity == 0){
            Toast.makeText(this, "Quantity is required...", Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(productPrice)){
            Toast.makeText(this, "Price is required...", Toast.LENGTH_SHORT).show()
            return
        }
        if (discountAvailable) {
            // product has discount
            discountPrice = edt_discount_price.text.toString().trim()
            discountNote = edt_discount_note.text.toString().trim()

            if (TextUtils.isEmpty(discountPrice)) {
                Toast.makeText(this, "Discount Price is required", Toast.LENGTH_SHORT).show()
                return // don't proceed further
            }
        }else{
            // product has not discount
                discountPrice = "0"
                discountNote = ""
        }
        product = Product(productId = timestamp ,productTitle, productDescription,
            productCategory, productQuantity, productIcon, productPrice, discountPrice,
            discountNote, discountAvailable, timestamp, uid)

        // add product to db
        saveProduct(product!!)
    }

    private fun saveProduct(product: Product) {
        // add data to db
        progressDialog.setMessage("Adding Product...")
        if (isUpdate)
            progressDialog.setMessage("Updating Product...")
        progressDialog.show()

        if (imageUri == null){
            // upload product without image
            saveProductToDB(product)
            if (isUpdate){
                Intent(this, MainSellerActivity::class.java).also {
                    startActivity(it)
                }
            }
            clearData()
        }else{
            // upload product with image
            // first, upload image...
            // name and path of image to be uploaded
            val filePathAndName = "product_image/${product.productId}"
            FirebaseStorage.getInstance().getReference(filePathAndName)
                .putFile(imageUri!!)
                .addOnSuccessListener { 
                    // image uploaded
                    // get url of image
                    val uriTask = it.storage.downloadUrl
                    while (!uriTask.isSuccessful){}
                    val downloadImageUri = uriTask.result
                    if (uriTask.isSuccessful){
                        product.productIcon = downloadImageUri.toString()
//                        saveProductToDB(product)
                        FirebaseDAO(this).addProduct(product)
                        progressDialog.dismiss()
                        if (isUpdate){
                            Intent(this, MainSellerActivity::class.java).also {
                                startActivity(it)
                            }
                        }
                        clearData()
                    }
                }
                .addOnFailureListener{
                    progressDialog.dismiss()
                    Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveProductToDB(product: Product){
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(product.uid!!).child("Products").child(product.productId.toString()).setValue(product)
            .addOnSuccessListener {
                progressDialog.dismiss()
                if (!isUpdate)
                    Toast.makeText(this, "Product Added", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(this, "Product updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearData() {
        img_product.setImageResource(R.drawable.ic_cart_gray)
        edt_product_title.setText("")
        edt_product_des.setText("")
        edt_product_price.setText("")
        edt_product_quantity.setText("")
        tv_product_cate.text = ""
        edt_discount_price.setText("")
        edt_discount_note.setText("")
        sw_discount.isChecked = false
        imageUri = null
    }

    private fun showCategoryDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Product Category")
        builder.setItems(MyCommon.productCategory) {_, which ->
            val category = MyCommon.productCategory[which]
            tv_product_cate.text = category
        }
        builder.show()
    }

    private fun showImagePickDialog() {
        val options = arrayOf("Camera", "Gallery")
        val buider = AlertDialog.Builder(this)
        buider.setTitle("Choose options")
            .setItems(options) { _, which ->
                //handle clicks
                handlerPickFromDialog(which)
            }
            .show()
    }

    private fun handlerPickFromDialog(which: Int) {
        if (which == 0) {
            // camera clicked
            if (checkCameraPermission())
                pickFromCamera()
            else
                requestCameraPermission()
        } else {
            // gallery clicked
            if (checkStoragePermission())
                pickFromGallery()
            else
                requestStoragePermission()
        }
    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission!!, MyCommon.STORAGE_REQUEST_CODE)
    }

    private fun pickFromGallery() {
        Intent(Intent.ACTION_PICK).also {
            it.type = "image/*"
            startActivityForResult(it, MyCommon.IMAGE_PICK_GALLERY_CODE)
        }
    }

    private fun checkCameraPermission(): Boolean {
        val result1 = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        val result2 = ContextCompat.checkSelfPermission(
            this, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        return result1 && result2
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission!!, MyCommon.CAMERA_REQUEST_CODE)
    }

    private fun pickFromCamera() {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image Title")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image Description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
            it.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(it, MyCommon.IMAGE_PICK_CAMERA_CODE)
        }
    }

    // handle permission result
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        when(requestCode){
            MyCommon.STORAGE_REQUEST_CODE -> {
                val storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (storageAccepted)
                    pickFromGallery()
                 else
                    Toast.makeText(this, "Camera Permission is necessary...", Toast.LENGTH_SHORT).show()
            }
            MyCommon.CAMERA_REQUEST_CODE -> {
                val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (storageAccepted)
                    pickFromCamera()
                else
                    Toast.makeText(this, "Camera Permission is necessary...", Toast.LENGTH_SHORT).show()
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK){
            if (requestCode == MyCommon.IMAGE_PICK_GALLERY_CODE){
                imageUri = data?.data
                Glide.with(this).load(imageUri).into(img_product)
            } else if (requestCode == MyCommon.IMAGE_PICK_CAMERA_CODE){
                Glide.with(this).load(imageUri).into(img_product)

            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }
}