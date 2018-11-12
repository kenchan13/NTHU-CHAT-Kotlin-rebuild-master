
package com.google.firebase.codelab.nthuchat

//import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
//import jdk.nashorn.internal.runtime.ECMAException.getException
import com.google.firebase.auth.FirebaseUser
//import org.junit.experimental.results.ResultMatchers.isSuccessful
import com.google.firebase.auth.AuthResult
import com.google.android.gms.tasks.Task
import android.support.annotation.NonNull
import android.support.v4.app.Fragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.layers_demo.*


/**
 * The main activity of the API library demo gallery.
 * The main layout lists the demonstrated features, with buttons to launch them.
 */

class DiscoverActivity : Fragment() {


    var mAuth: FirebaseAuth? = null
    var currentUser:FirebaseUser ?= null


//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
////        direct to layer demo
//        mAuth = FirebaseAuth.getInstance()
////        signInAnonymous()
////        val intent = Intent(this, LayersDemoActivity::class.java)
//
//        startActivity(intent)
//
//
//    }
//
//    public override fun onStart() {
//        super.onStart()
//        // Check if user is signed in (non-null) and update UI accordingly.
//        var currentUser = mAuth!!.getCurrentUser()
//    }
//
//    private fun signInAnonymous(){
//        mAuth!!.signInAnonymously().addOnCompleteListener(this) { task ->
//            if (task.isSuccessful) {
//                // Sign in success, update UI with the signed-in user's information
////                Log.d(FragmentActivity.TAG, "signInAnonymously:success")
//                val user = mAuth!!.currentUser
//                Toast.makeText(applicationContext, "Authentication success.",
//                        Toast.LENGTH_SHORT).show()
//            } else {
//                // If sign in fails, display a message to the user.
////                Log.w(FragmentActivity.TAG, "signInAnonymously:failure", task.exception)
//                Toast.makeText(applicationContext, "Authentication failed.",
//                        Toast.LENGTH_SHORT).show()
//            }
//
//            // ...
//        }
//
//
//
//    }

}
