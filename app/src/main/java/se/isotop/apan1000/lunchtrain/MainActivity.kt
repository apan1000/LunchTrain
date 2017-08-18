package se.isotop.apan1000.lunchtrain

import android.content.Intent
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.database.Cursor
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*
import android.widget.ProgressBar
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Toast


class MainActivity : AppCompatActivity(),
        LoaderManager.LoaderCallbacks<Cursor>,
        TrainAdapter.TrainAdapterOnClickHandler {

    private val ID_TRAIN_LOADER = 25;

    lateinit private var mTrainAdapter: TrainAdapter
    lateinit private var mRecyclerView: RecyclerView
    private var mPosition = RecyclerView.NO_POSITION

    lateinit private var mLoadingIndicator: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // Do something with the intent
        val userName = intent.getStringExtra("name")
        Toast.makeText(this, "Started by $userName", Toast.LENGTH_LONG)
        //

        mRecyclerView = findViewById(R.id.recyclerview_trains)

        mLoadingIndicator = findViewById(R.id.pb_loading_indicator)

        val layoutManager =  LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mRecyclerView.layoutManager = layoutManager

        mRecyclerView.setHasFixedSize(true)

        /*
         * The TrainAdapter is responsible for linking our train data with the Views that
         * will end up displaying our data.
         */
        mTrainAdapter = TrainAdapter(this, this);

        showLoading()

        //supportLoaderManager.initLoader(ID_TRAIN_LOADER, null, this)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

    private fun createMockData() {
        // TODO: Create some trains
    }

    override fun onClick(id: Long) {
        // TODO: Show train with id
    }

    private fun showTrainsView() {
        mLoadingIndicator.visibility = View.INVISIBLE

        mRecyclerView.visibility = View.VISIBLE
    }

    private fun showLoading() {
        mRecyclerView.visibility = View.INVISIBLE

        mLoadingIndicator.visibility = View.VISIBLE
    }

    override fun onCreateLoader(loaderId: Int, bundle: Bundle?): Loader<Cursor> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLoadFinished(loader: Loader<Cursor>?, data: Cursor?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLoaderReset(loader: Loader<Cursor>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId) {
        R.id.action_settings -> {
            startActivity(Intent(this, SettingsActivity::class.java))
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
