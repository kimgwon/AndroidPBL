package com.example.pbl_sns.ui.home

import android.content.ContentValues
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.example.pbl_sns.MyApplication.Companion.prefs
import com.example.pbl_sns.R
import com.example.pbl_sns.base.BaseFragment
import com.example.pbl_sns.databinding.FragmentHomeBinding
import com.example.pbl_sns.model.Post
import com.example.pbl_sns.model.Reply
import com.example.pbl_sns.model.User
import com.example.pbl_sns.repository.AlarmDTO
import com.example.pbl_sns.repository.UserRepository
import com.example.pbl_sns.ui.MainActivity
import com.example.pbl_sns.ui.profile.LogoutDialog
import com.example.pbl_sns.ui.profile.ProfileAdapter
import com.example.pbl_sns.ui.profile.ReplyDialog
import com.example.pbl_sns.ui.search.SearchAdapter
import com.example.pbl_sns.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment: BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {
    lateinit var homeAdapter: HomeAdapter
    private val userEmail = prefs.getString("email", "-1")
    private val userId = prefs.getString("id", "-1")
    private val userRepo = UserRepository()
    private val db = Firebase.firestore
    private val viewModel by lazy {
        ViewModelProvider(this)[UserViewModel::class.java]
    }

    override fun initDataBinding() {
        super.initDataBinding()

        (activity as MainActivity).setBottomNavSetting("all")

        homeAdapter = HomeAdapter(ArrayList())
        binding.homeRecyclerviewHome.adapter = homeAdapter

        viewModel.getUserFollowing(userEmail)
        viewModel.getAllPost()
        viewModel.userLiveFollowingData.observe(viewLifecycleOwner) {
            viewModel.getAllPost()
        }
        viewModel.allLivePostData.observe(viewLifecycleOwner) {
            homeAdapter.itemList = it
        }
        viewModel.getUserLikePost(userEmail)
        viewModel.userLikePostData.observe(viewLifecycleOwner){
            homeAdapter.likePostData = it
        }
    }

    override fun initAfterBinding() {
        super.initAfterBinding()

        // adapter에서 눌렸을때 친구 프로필 뜸
        homeAdapter.setItemClickListener(object : HomeAdapter.OnItemClickListener {
            override fun onClick(position: Int, status: String, post:Post, editReply: String) {
                val id = homeAdapter.itemList[position].email
                if (status == "btnReply") {
                    addReply(id, post, editReply)
                } else if (status == "btnAllReply") {
                    ReplyDialog(id, post).show(parentFragmentManager,"ReplyDialog")
                }
            }
        })
    }

    fun addReply(email: String, post:Post, editReply: String) {

        if (userEmail != "-1") {
            val reply = hashMapOf(
                "profile" to "",
                "id" to userId,
                "reply" to editReply,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("users").document(email).collection("postArray").document(post.time.toString())
                .update("reply",FieldValue.arrayUnion(reply))
                .addOnSuccessListener {
                    Toast.makeText(context, "댓글 업로드 성공", Toast.LENGTH_LONG).show()
                    replyAlarm(email, editReply);
                }

        }
    }

    private fun replyAlarm(destinationUid: String, reply: String){
        var alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        alarmDTO.userId = userEmail
        alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        alarmDTO.kind = 1
        alarmDTO.timestamp = System.currentTimeMillis()
        alarmDTO.message = "님이 회원님의 게시물에 댓글을 달았습니다: $reply"
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
    }
}