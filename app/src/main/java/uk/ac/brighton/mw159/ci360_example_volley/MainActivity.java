package uk.ac.brighton.mw159.ci360_example_volley;

import java.util.ArrayList;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;


public class MainActivity extends ListActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private String mQuery;
    private ArrayList<Item> mItemList;
    private ItemListAdapter mItemListAdapter;
    private ListView mListView;
    private ProgressDialog mProgress;


    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mItemList = new ArrayList<Item>();
        mItemListAdapter = new ItemListAdapter(this, R.layout.list_item, mItemList, false);
        setListAdapter(mItemListAdapter);

        mListView = (ListView) findViewById(android.R.id.list);

        ((Button) findViewById(R.id.btn_search)).setOnClickListener(new OnClickListener() {

            public void onClick(View view) {
                View focused = getCurrentFocus();
                if (focused != null) {
                    focused.clearFocus();
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
                }
                search();
            }
        });
    }



    private final void search() {

        mQuery = ((EditText) findViewById(R.id.txt_search)).getText().toString().trim();

        if(mQuery.length() == 0) return;  // sanity check

        mProgress = ProgressDialog.show(this, null, getString(R.string.loading));

        String url = "http://www.vam.ac.uk/api/json/museumobject/search?images=1&q=" + mQuery;
        JsonObjectRequest jsonRequest = new JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            new Response.Listener<JSONObject>() {

                public void onResponse(JSONObject json) {

                    if((mProgress != null) && (mProgress.isShowing())) mProgress.dismiss();

                    mItemList.clear();

                    try {

                        JSONArray records = json.getJSONArray("records");

                        for(int i=0; i<records.length(); i++) {
                            JSONObject obj = records.getJSONObject(i);
                            JSONObject fields = obj.getJSONObject("fields");
                            Item item = new Item(fields.getString("artist"),
                                    fields.optString("date_text", "Unknown"),
                                    fields.getString("primary_image_id"));
                            mItemList.add(item);
                        }
                    }
                    catch(Exception e) {
                        Log.e(TAG, "search()", e);
                    }

                    mItemListAdapter.notifyDataSetChanged();
                    mListView.smoothScrollToPosition(0);
                }
            },
            new Response.ErrorListener() {
                public void onErrorResponse(VolleyError e) {
                    if((mProgress != null) && (mProgress.isShowing())) mProgress.dismiss();
                    Log.e(TAG, "search()", e);
                }
            }
        );

        MyRequestQueue.getInstance(this).getRequestQueue().add(jsonRequest);
    }
}
