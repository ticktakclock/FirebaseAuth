package com.github.ticktakclock.firebaseauth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import butterknife.bindView


class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private val classes = arrayOf(
            FirebaseSignInActivity::class.java,
            EmailSignInActivity::class.java
    )
    private val descriptions = arrayOf(
            "using google sign in",
            "using email sign in"
    )

    private val contentList: ListView by bindView(R.id.activity_main_content_lv)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setUpUI()
    }

    /**
     * setup layout behavior
     * */
    private fun setUpUI() {
        val adapter = MyArrayAdapter(this, android.R.layout.simple_list_item_2, classes)
        adapter.setDescriptionIds(descriptions)
        contentList.adapter = adapter
        contentList.setOnItemClickListener { parent, view, position, id ->
            val clickedClass = classes[position]
            startActivity(Intent(this, clickedClass))
        }
    }

    class MyArrayAdapter(private val mContext: Context, resource: Int, private val mClasses: Array<Class<out AppCompatActivity>>) : ArrayAdapter<Class<*>>(mContext, resource, mClasses) {
        private var mDescriptionIds = emptyArray<String>()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view: View = convertView ?: inflater.inflate(android.R.layout.simple_list_item_2, null)

            (view.findViewById(android.R.id.text1) as TextView).text = mClasses[position].simpleName
            (view.findViewById(android.R.id.text2) as TextView).setText(mDescriptionIds[position])

            return view
        }

        fun setDescriptionIds(descriptionIds: Array<String>) {
            mDescriptionIds = descriptionIds
        }
    }
}
