package de.niklasenglmeier.androidcommon.user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import de.niklasenglmeier.androidcommon.R
import de.niklasenglmeier.androidcommon.adapters.SimpleTextSplitLayoutAdapter
import de.niklasenglmeier.androidcommon.adapters.interfaces.AdapterItemClickListener
import de.niklasenglmeier.androidcommon.databinding.ActivityUserInfoBinding
import de.niklasenglmeier.androidcommon.extensions.applyUserModeSettings
import de.niklasenglmeier.androidcommon.firebase.firestore.FirestoreStandardFetches
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class UserInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(supportActionBar != null) {
            supportActionBar!!.title = "User Info"
        }
        applyUserModeSettings(applicationContext)

        FirestoreStandardFetches.Users.getUserInfo(
            false,
            {
                binding.recyclerViewUserinfo.layoutManager = LinearLayoutManager(applicationContext)
                binding.recyclerViewUserinfo.adapter = SimpleTextSplitLayoutAdapter(
                    this,
                    arrayOf(
                        SimpleTextSplitLayoutAdapter.DataItem("First Name", it.firstName ?: "-"),
                        SimpleTextSplitLayoutAdapter.DataItem("Last Name", it.lastName ?: "-"),
                        SimpleTextSplitLayoutAdapter.DataItem("Display Name", it.displayName ?: "-"),
                        SimpleTextSplitLayoutAdapter.DataItem("Email", it.email ?: "-"),
                        SimpleTextSplitLayoutAdapter.DataItem("Phone", it.phoneNumber ?: "-"),
                        SimpleTextSplitLayoutAdapter.DataItem("Login Method", it.loginMethod.toString()),
                        SimpleTextSplitLayoutAdapter.DataItem(
                            "Account created",
                            SimpleDateFormat("dd/MM/yyyy").format(Date(Firebase.auth.currentUser?.metadata?.creationTimestamp!!))
                        )
                    )
                )
            },
            {
                Toast.makeText(applicationContext, "User data not present", Toast.LENGTH_SHORT).show()
            }
        ) {
            Toast.makeText(applicationContext, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
        }
    }
}