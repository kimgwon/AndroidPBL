package com.example.pbl_sns.ui

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.pbl_sns.MyApplication
import com.example.pbl_sns.R
import com.example.pbl_sns.base.BaseDialogFragment
import com.example.pbl_sns.databinding.DialogProfileEditBinding
import com.example.pbl_sns.model.Privacy
import com.example.pbl_sns.model.User
import com.example.pbl_sns.viewmodel.UserViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileEditDialog:BaseDialogFragment<DialogProfileEditBinding>(R.layout.dialog_profile_edit) {
    private val db = Firebase.firestore
    private lateinit var dataOrigin: Privacy
    private lateinit var editItId:String
    private lateinit var editItImage:String
    private lateinit var editItInfo:String
    private lateinit var editItName:String
    private var isEdit:Boolean = false
    private var emptyIs:String = ""

    private val viewModel by lazy {
        ViewModelProvider(this)[UserViewModel::class.java]
    }

    override fun initDataBinding() {
        super.initDataBinding()

        viewModel.getUserData()

        viewModel.userLiveData.observe(viewLifecycleOwner) {
            dataOrigin = it
            binding.editTvIdProfile.setText(it.id)
            editItImage = it.image
            binding.editTvInfoProfile.setText(it.info)
            binding.editTvNameProfile.setText(it.name)
        }
    }

    override fun initAfterBinding() {
        super.initAfterBinding()

        binding.btnCloseEdit.setOnClickListener {
            dismiss()
        }

        binding.btnCompleteEdit.setOnClickListener {
            editItId = binding.editTvIdProfile.text.toString()
            editItInfo = binding.editTvInfoProfile.text.toString()
            editItName = binding.editTvNameProfile.text.toString()

            if(checkData()){
                val data =  hashMapOf(
                    "id" to editItId,
                    "image" to editItImage,
                    "info" to editItInfo,
                    "name" to editItName,
                )

                Log.d("dataa", data.toString())

                db.collection("users").document(MyApplication.prefs.getString("email", "null"))
                    .update("privacy", data)

                Log.d("lifee", "Dialog")
                dismiss()

            } else{
                Toast.makeText(context, "${emptyIs}가 입력되지 않았습니다.", Toast.LENGTH_LONG)
            }
        }
    }

    private fun checkData():Boolean{
        if(editItId.isEmpty()){
            emptyIs = "아이디"
            isEdit = false
        } else if(editItName.isEmpty()){
            emptyIs = "이름"
            isEdit = false
        } else
            isEdit = true

        return isEdit
    }

}