package com.fcemtopall.cryptocurrencytracker.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.fcemtopall.cryptocurrencytracker.R
import com.fcemtopall.cryptocurrencytracker.`interface`.ILoadMore
import com.fcemtopall.cryptocurrencytracker.adapter.CoinAdapter
import com.fcemtopall.cryptocurrencytracker.common.Common
import com.fcemtopall.cryptocurrencytracker.model.CoinModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import okhttp3.*
import okhttp3.Request.Builder
import java.io.IOException

class MainActivity : AppCompatActivity(), ILoadMore {

    internal var items: MutableList<CoinModel> = ArrayList()
    internal lateinit var adapter: CoinAdapter
    internal lateinit var client: OkHttpClient
    internal lateinit var request: Request


    override fun onLoadMore() {
        if (items.size <= Common.MAX_COIN_LOAD)
            loadNext10Coin(items.size)
        else
            Toast.makeText(
                this@MainActivity,
                "Data max is " + Common.MAX_COIN_LOAD,
                Toast.LENGTH_SHORT
            )
                .show()
    }

    private fun loadNext10Coin(index: Int) {
        client = OkHttpClient()
        request = Builder()
            .url(String.format("https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest?start=1&limit=100&CMC_PRO_API_KEY=b4b166aa-62e0-44a9-bf7f-162983ef1aa6", index))
            .build()

        swipe_to_refresh.isRefreshing = true // SHow refresh
        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    TODO("Not yet implemented")
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body!!.string()
                    val gson = Gson()
                    val newItems = gson.fromJson<List<CoinModel>>(body, object :
                        TypeToken<List<CoinModel>>() {}.type)
                    runOnUiThread {
                        items.addAll(newItems)
                        adapter.setLoaded()
                        adapter.updateData(items)

                        swipe_to_refresh.isRefreshing = false
                    }                }

            })

    }

    private fun loadFirst10Coin() {
        client = OkHttpClient()
        request = Builder()
            .url(String.format("https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest?start=1&limit=100&CMC_PRO_API_KEY=b4b166aa-62e0-44a9-bf7f-162983ef1aa6"))
            .build()



        client.newCall(request)
            .enqueue(object : Callback{
                override fun onFailure(call: Call, e: IOException) {
                    Log.d("ERROR",e.toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body!!.string()
                    val gson=Gson()
                    items = gson.fromJson(body,object:TypeToken<List<CoinModel>>(){}.type)
                    runOnUiThread {
                        adapter.updateData(items)


                    }                }

            })

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        swipe_to_refresh.post { loadFirst10Coin() }

        swipe_to_refresh.setOnRefreshListener {
            items.clear() // Remove all item
            loadFirst10Coin()
            setUpAdapter()
            initView()
        }

        coin_recycler_view.layoutManager = LinearLayoutManager(this)
        setUpAdapter()
    }

    private fun setUpAdapter() {
        adapter = CoinAdapter(coin_recycler_view,this@MainActivity,items)
        coin_recycler_view.adapter = adapter
        adapter.setLoadMore(this)
    }

    private fun initView(){
        val userButton = findViewById<ImageButton>(R.id.user)
        userButton?.setOnClickListener {
            val intent = Intent(this, User::class.java)
            startActivity(intent)
        }
    }


}

