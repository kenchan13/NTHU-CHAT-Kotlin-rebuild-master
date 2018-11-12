package com.google.firebase.codelab.nthuchat

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.support.v4.app.Fragment
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.annotation.NonNull
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AlertDialog
import android.text.InputFilter
import android.util.Log
import android.view.MotionEvent
import android.view.SubMenu
import android.view.View
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import com.facebook.stetho.Stetho
import com.google.android.gms.ads.AdView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.squareup.picasso.Picasso

import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.layers_demo.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener, DiscoverFragment.OnFragmentInteractionListener {

    // Firebase instance variables
    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseUser: FirebaseUser? = null
    //private val mFirebaseRemoteConfig: FirebaseRemoteConfig? = null  //WTF
    //private val mFirebaseAnalytics: FirebaseAnalytics? = null  //WTF
    //private val mAdView: AdView? = null  //WTF
    var mUsername: String? = null
    var mPhotoUrl: String? = null
    var mUid: String? = null
    lateinit private var mNameView: TextView
    //private var mEmailView: TextView? = null
    //private var mIconView: ImageView? = null
    var navigationView: NavigationView? = null
    var drawer: DrawerLayout? = null
    //private var headerView: View? = null
    //var fab: FloatingActionButton? = null  //WTF
    lateinit var currentFragment: Fragment
    //private val mFirebaseDB: FirebaseDatabase? = null  //WTF
    //private val mFBdiv: DatabaseReference? = null  //WTF
    lateinit var dbinstance: AppDatabase
    var user: User? = null
    var sub1: Menu? = null

    lateinit var discoverFragment: DiscoverFragment

    override protected fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Stetho.initializeWithDefaults(this);
        setContentView(R.layout.activity_main)
        navigationView = findViewById(R.id.nav_view)
        var headerView: View = navigationView?.getHeaderView(0) as View
        mNameView = headerView.findViewById(R.id.nameView)
        var mEmailView: TextView = headerView.findViewById(R.id.emailView)
        var mIconView: ImageView = headerView.findViewById(R.id.iconView)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        dbinstance = AppDatabase.getAppDatabase(applicationContext)
        user = dbinstance.userDao().getUser()

        discoverFragment = DiscoverFragment.newInstance()

        drawer = findViewById(R.id.drawer_layout)
        val toggle = object : ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                if (slideOffset != 0f) {
                    hideKeyboard(this@MainActivity)
                }
            }
        }

        drawer?.addDrawerListener(toggle)
        toggle.syncState()

        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseUser = mFirebaseAuth!!.getCurrentUser()
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        } else {
            if (mFirebaseUser!!.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser!!.getPhotoUrl().toString()
                mUsername = mFirebaseUser!!.getDisplayName()
                mUid = mFirebaseUser!!.getUid()
                if (mPhotoUrl != null && mPhotoUrl!!.contains("..")) {
                    mPhotoUrl = "https://nthuchat.com" + mPhotoUrl!!.replace("..", "")
                }
                //Toast.makeText(this, "name:  "+mPhotoUrl, Toast.LENGTH_SHORT).show();
                mNameView.setText(mUsername)
                mEmailView.setText(mFirebaseUser!!.getEmail())
                Picasso.with(this@MainActivity).load(mPhotoUrl).transform(CropCircleTransformation()).into(mIconView)
                //mIconView.setImageURI(Uri.parse(mPhotoUrl));
            } else {
                val picnum = Math.round(Math.random() * 12 + 1).toInt()
                val namelist = arrayOf("葉葉", "畫眉", "JIMMY", "阿醜", "茶茶", "麥芽", "皮蛋", "小豬", "布丁", "黑嚕嚕", "憨吉", "LALLY", "花捲")
                val namenum = Math.round(Math.random() * namelist.size).toInt()
                val profileUpdate = UserProfileChangeRequest.Builder()
                        .setDisplayName(namelist[namenum])
                        .setPhotoUri(Uri.parse("../images/user$picnum.jpg")).build()

//                Original Error Kotlin Code
//                mFirebaseUser!!.updateProfile(profileUpdate)
//                        .addOnCompleteListener( object : OnCompleteListener<Void>() {
//                             fun onComplete(@NonNull task: Task<Void>) {
//                                if (task.isSuccessful()) {
//                                    mUsername = mFirebaseUser!!.getDisplayName()
//                                    mUid = mFirebaseUser!!.getUid()
//                                    mPhotoUrl = mFirebaseUser!!.getPhotoUrl().toString()
//                                    mNameView!!.setText(mUsername)
//                                    mEmailView!!.setText(mFirebaseUser!!.getEmail())
//                                    if (mPhotoUrl != null && mPhotoUrl!!.contains("..")) {
//                                        mPhotoUrl = "https://nthuchat.com" + mPhotoUrl!!.replace("..", "")
//                                    }
//                                    Picasso.with(this@MainActivity).load(mPhotoUrl).transform(CropCircleTransformation()).into(mIconView)
//                                }
//                            }
//                        })

                mFirebaseUser!!.updateProfile(profileUpdate)
                        .addOnCompleteListener{ task ->
                            fun onComplete(@NonNull task: Task<Void>) {
                                if (task.isSuccessful()) {
                                    mUsername = mFirebaseUser!!.getDisplayName()
                                    mUid = mFirebaseUser!!.getUid()
                                    mPhotoUrl = mFirebaseUser!!.getPhotoUrl().toString()
                                    mNameView.setText(mUsername)
                                    mEmailView.setText(mFirebaseUser!!.getEmail())
                                    if (mPhotoUrl != null && mPhotoUrl!!.contains("..")) {
                                        mPhotoUrl = "https://nthuchat.com" + mPhotoUrl!!.replace("..", "")
                                    }
                                    Picasso.with(this@MainActivity).load(mPhotoUrl).transform(CropCircleTransformation()).into(mIconView)
                                }
                            }
                        }

            }




        }

        if (user != null) {
            navigationView?.getMenu()?.findItem(R.id.div)?.setTitle(user!!.Div)
            val coursename = user!!.Classes as String
            val course_title = coursename.split("#".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            //Toast.makeText(this, "course.length: "+course_title.length, Toast.LENGTH_SHORT).show();
            if (course_title.size > 1) {
                sub1 = navigationView?.getMenu()?.addSubMenu(R.id.course_menu, 49, 49, R.string.courses)
                for (id in 0..course_title.size - 1) {
                    //Toast.makeText(MainActivity.this, course_title[id], Toast.LENGTH_SHORT).show();
                    sub1?.add(0, 50 + id, 50 + id, course_title[id])?.setIcon(R.drawable.ic_assignment_black_18dp)
                }
            }
        } else {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }
        navigationView?.setNavigationItemSelectedListener(this)
        displaySelectedScreen(R.id.school)
    }


    override fun onBackPressed() {
        drawer = findViewById(R.id.drawer_layout)
        if (drawer?.isDrawerOpen(GravityCompat.START) == true) {
            drawer?.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        //getSupportFragmentManager().beginTransaction().add(R.id.content_frame, currentFragment).commit();
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.getItemId()) {
            R.id.action_settings -> {
                val uri = Uri.parse("https://www.facebook.com/nthuchat/")
                val it = Intent(Intent.ACTION_VIEW, uri)
                startAnimatedActivity(it)
                return true
            }
            else -> {
                hideKeyboard(this)
                return super.onOptionsItemSelected(item)
            }
        }
    }


    override fun onNavigationItemSelected(@NonNull item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.getItemId()
        when (id) {
            R.id.school -> displaySelectedScreen(R.id.school)
            R.id.div -> displaySelectedScreen(R.id.div)
            R.id.change_name -> {
                val title = getString(R.string.change_name)
                val intro = getString(R.string.change_name_intro)
                val confirm = getString(R.string.confirm)
                val cancel = getString(R.string.cancel)
                val lastname = mFirebaseUser!!.getDisplayName()

                val alertdialog = AlertDialog.Builder(this@MainActivity)
                val editText = EditText(this@MainActivity)
                val container = FrameLayout(this@MainActivity)
                val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                params.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin)
                params.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin)
                editText.setLayoutParams(params)
                editText.setHint(lastname)
                editText.setMaxLines(1)
                editText.setSingleLine()
                editText.setFilters(arrayOf<InputFilter>(InputFilter.LengthFilter(20)))
                container.addView(editText)

                alertdialog.setTitle(title)//設定視窗標題
                        .setIcon(R.mipmap.ic_launcher)//設定對話視窗圖示
                        .setMessage(intro)//設定顯示的文字
                        .setView(container)
//                        Original Kotlin Error Codre
//                        .setNegativeButton(cancel, object : DialogInterface.OnClickListener() {
//                            override fun onClick(dialog: DialogInterface, which: Int) {
//                                Toast.makeText(this@MainActivity, "Canceled Change Name", Toast.LENGTH_SHORT).show()
//                            }


                        .setNegativeButton(cancel, object : DialogInterface.OnClickListener {
                             override fun onClick(dialog: DialogInterface, which: Int) {
                                Toast.makeText(this@MainActivity, "Canceled Change Name", Toast.LENGTH_SHORT).show()
                            }

                        })//設定結束的子視窗
                        .setPositiveButton(confirm, object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface, which: Int) {
                                val changename = editText.getText().toString()
                                if (changename.contains(" ")) {
                                    changename.replace(" ".toRegex(), "")
                                }
                                if (changename.trim({ it <= ' ' }).length > 0) {
                                    val profileUpdate = UserProfileChangeRequest.Builder()
                                            .setDisplayName(changename).build()
//                                  Original Error Kotlin
//                                    mFirebaseUser!!.updateProfile(profileUpdate)
//                                            .addOnCompleteListener(object : OnCompleteListener<Void>() {
//                                                override fun onComplete(@NonNull task: Task<Void>) {
//                                                    if (task.isSuccessful()) {
//                                                        mUsername = mFirebaseUser!!.getDisplayName()
//                                                        mUid = mFirebaseUser!!.getUid()
//                                                        mPhotoUrl = mFirebaseUser!!.getPhotoUrl().toString()
//                                                        Toast.makeText(this@MainActivity, "Now your name: $mUsername", Toast.LENGTH_SHORT).show()
//                                                        mNameView!!.setText(mUsername)
//                                                    }
//                                                }
//                                            })

                                    mFirebaseUser!!.updateProfile(profileUpdate)
                                            .addOnCompleteListener { task ->
                                                 fun onComplete(@NonNull task: Task<Void>) {
                                                    if (task.isSuccessful()) {
                                                        mUsername = mFirebaseUser!!.getDisplayName()
                                                        mUid = mFirebaseUser!!.getUid()
                                                        mPhotoUrl = mFirebaseUser!!.getPhotoUrl().toString()
                                                        Toast.makeText(this@MainActivity, "Now your name: $mUsername", Toast.LENGTH_SHORT).show()
                                                        mNameView.setText(mUsername)
                                                    }
                                                }
                                            }


                                }
                            }
                        })//設定結束的子視窗
                        .show()
            }
            R.id.sign_out_menu -> {
                mFirebaseAuth!!.signOut()
                val targetUser: User = dbinstance.userDao().getUser()
                dbinstance.userDao().delete(targetUser)
                AppDatabase.destroyInstance()
                startAnimatedActivity(Intent(this, SignInActivity::class.java))
            }

            else -> displaySelectedScreen(id)
        }
        hideKeyboard(this)
        drawer = findViewById(R.id.drawer_layout)
        drawer?.closeDrawer(GravityCompat.START)
        return true
    }

    protected fun startAnimatedActivity(intent: Intent) {
        startActivity(intent)
        hideKeyboard(this)
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
    }

    private fun displaySelectedScreen(itemId: Int) {
        //creating fragment object
        var fragment: Fragment? = null
        //initializing the fragment object which is selected
        when (itemId) {
            R.id.school -> {
                fragment = Schoolchat()
                navigationView?.setCheckedItem(itemId)
            }
            R.id.div -> {
                fragment = Department()
                navigationView?.setCheckedItem(itemId)
            }


            R.id.discover -> {
                fragment = DiscoverFragment()
                navigationView?.setCheckedItem(itemId)
//                fragment = DiscoverFragment()
//                navigationView?.setCheckedItem(itemId)
                //fragment = Department()
//                navigationView?.setCheckedItem(itemId)
//                val intent = Intent(this, NewActivity::class.java)
//                startActivity(intent)
//                Log.d(TAG, "discover onclicked")
            }


            else -> {
                fragment = Course(sub1?.findItem(itemId).toString())
                navigationView?.setCheckedItem(itemId)
            }
        }

        //replacing the fragment
        if (fragment != null) {
            hideKeyboard(this)
            val ft = getSupportFragmentManager().beginTransaction()
            ft.setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
            ft.replace(R.id.content_frame, fragment)
            ft.commit()
        }

        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
    }

    override protected fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: requestCode=$requestCode, resultCode=$resultCode")
    }

    override fun onConnectionFailed(@NonNull connectionResult: ConnectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:$connectionResult")
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private val TAG = "MainActivity"

        fun hideKeyboard(activity: Activity) {
            val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            //Find the currently focused view, so we can grab the correct window token from it.
            var view = activity.getCurrentFocus()
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = View(activity)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onFragmentInteraction(uri: Uri) {
        // For linking activities and fragment to prevent crash
        Log.d("onfragmentinteraction", "")
    }
}
