package com.virmana.news_app.ui.Activities;


import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.virmana.news_app.R;
import com.virmana.news_app.Adapters.PostAdapter;
import com.virmana.news_app.api.apiClient;
import com.virmana.news_app.api.apiRest;
import com.virmana.news_app.model.Category;
import com.virmana.news_app.model.Post;
import com.virmana.news_app.Provider.PrefManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SearchActivity extends AppCompatActivity {


    private String query;

    private Integer type_ads = 0;
    private Integer page = 0;
    private String language = "0";
    private Boolean loaded=false;

    private SwipeRefreshLayout swipe_refreshl_image_search;
    private RecyclerView recycler_view_image_search;
    private List<Post> postList =new ArrayList<>();
    private List<Category> categoryList =new ArrayList<>();
    private PostAdapter postAdapter;
    private LinearLayoutManager linearLayoutManager;
    private PrefManager prefManager;
    private boolean loading = true;
    private int pastVisiblesItems, visibleItemCount, totalItemCount;
    private RelativeLayout relative_layout_load_more;
    private LinearLayout linear_layout_page_error;
    private Button button_try_again;
    private ImageView imageView_empty_favorite;

    private Integer item = 0 ;
    private Integer lines_beetween_ads = 8 ;
    private Boolean native_ads_enabled = false ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras() ;
        this.query =  bundle.getString("query");
        this.prefManager= new PrefManager(getApplicationContext());
        this.language=prefManager.getString("LANGUAGE_DEFAULT");

        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle(query);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        initView();
        loadVideo();
        showAdsBanner();
        initAction();

    }
    public boolean checkSUBSCRIBED(){
        PrefManager prefManager= new PrefManager(getApplicationContext());
        if (!prefManager.getString("SUBSCRIBED").equals("TRUE")) {
            return false;
        }
        return true;
    }
    public void showAdsBanner() {
        if (!checkSUBSCRIBED()) {
            PrefManager prefManager= new PrefManager(getApplicationContext());
            if (prefManager.getString("ADMIN_BANNER_TYPE").equals("FACEBOOK")){
                showFbBanner();
            }
            if (prefManager.getString("ADMIN_BANNER_TYPE").equals("ADMOB")){
                showAdmobBanner();
            }
            if (prefManager.getString("ADMIN_BANNER_TYPE").equals("BOTH")) {
                if (prefManager.getString("Banner_Ads_display").equals("FACEBOOK")) {
                    prefManager.setString("Banner_Ads_display", "ADMOB");
                    showAdmobBanner();
                } else {
                    prefManager.setString("Banner_Ads_display", "FACEBOOK");
                    showFbBanner();
                }
            }
        }

    }
    public void showAdmobBanner(){
        PrefManager prefManager= new PrefManager(getApplicationContext());
        LinearLayout linear_layout_ads =  (LinearLayout) findViewById(R.id.linear_layout_ads);
        final AdView mAdView = new AdView(this);
        mAdView.setAdSize(AdSize.SMART_BANNER);
        mAdView.setAdUnitId(prefManager.getString("ADMIN_BANNER_ADMOB_ID"));
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);
        linear_layout_ads.addView(mAdView);

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mAdView.setVisibility(View.VISIBLE);
            }
        });
    }
    public void showFbBanner(){
        PrefManager prefManager= new PrefManager(getApplicationContext());
        LinearLayout linear_layout_ads =  (LinearLayout) findViewById(R.id.linear_layout_ads);
        com.facebook.ads.AdView adView = new com.facebook.ads.AdView(this, prefManager.getString("ADMIN_BANNER_FACEBOOK_ID"), com.facebook.ads.AdSize.BANNER_HEIGHT_90);
        linear_layout_ads.addView(adView);
        adView.loadAd();

    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.back_enter, R.anim.back_exit);
        return;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                super.onBackPressed();
                overridePendingTransition(R.anim.back_enter, R.anim.back_exit);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void initView(){
        if (!prefManager.getString("ADMIN_NATIVE_TYPE").equals("FALSE")){
            native_ads_enabled=true;
            lines_beetween_ads=Integer.parseInt(prefManager.getString("ADMIN_NATIVE_LINES"));
        }
        PrefManager prefManager= new PrefManager(getApplicationContext());
        if (prefManager.getString("SUBSCRIBED").equals("TRUE")) {
            native_ads_enabled=false;
        }
        this.imageView_empty_favorite=(ImageView) findViewById(R.id.imageView_empty_favorite);
        this.relative_layout_load_more=(RelativeLayout) findViewById(R.id.relative_layout_load_more);
        this.button_try_again =(Button) findViewById(R.id.button_try_again);
        this.swipe_refreshl_image_search=(SwipeRefreshLayout) findViewById(R.id.swipe_refreshl_image_search);
        this.linear_layout_page_error=(LinearLayout) findViewById(R.id.linear_layout_page_error);
        this.recycler_view_image_search=(RecyclerView) findViewById(R.id.recycler_view_image_search);
        this.linearLayoutManager=  new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        postAdapter =new PostAdapter(postList,categoryList,this);
        recycler_view_image_search.setHasFixedSize(true);
        recycler_view_image_search.setAdapter(postAdapter);
        recycler_view_image_search.setLayoutManager(linearLayoutManager);
        recycler_view_image_search.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if(dy > 0) //check for scroll down
                {

                    visibleItemCount    = linearLayoutManager.getChildCount();
                    totalItemCount      = linearLayoutManager.getItemCount();
                    pastVisiblesItems   = linearLayoutManager.findFirstVisibleItemPosition();

                    if (loading)
                    {
                        if ( (visibleItemCount + pastVisiblesItems) >= totalItemCount)
                        {
                            loading = false;
                            loadNextVideo();
                        }
                    }
                }else{

                }
            }
        });

    }
    public void initAction(){
        swipe_refreshl_image_search.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                postList.clear();
                page = 0;
                item = 0;
                loading=true;
                loadVideo();
            }
        });
    }
    public void loadNextVideo(){
        relative_layout_load_more.setVisibility(View.VISIBLE);
        Retrofit retrofit = apiClient.getClient();
        apiRest service = retrofit.create(apiRest.class);
        Call<List<Post>> call = service.searchImage(prefManager.getString("ORDER_DEFAULT_STATUS"),language,page,query);
        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                apiClient.FormatData(SearchActivity.this,response);

                if(response.isSuccessful()){
                    if (response.body().size()!=0){
                        for (int i=0;i<response.body().size();i++){
                            postList.add(response.body().get(i));
                            if (native_ads_enabled){
                                item++;
                                if (item == lines_beetween_ads ){
                                    item= 0;
                                    if (prefManager.getString("ADMIN_NATIVE_TYPE").equals("ADMON")) {
                                        postList.add(new Post().setViewType(8));
                                    }else if (prefManager.getString("ADMIN_NATIVE_TYPE").equals("FACEBOOK")){
                                        postList.add(new Post().setViewType(4));
                                    } else if (prefManager.getString("ADMIN_NATIVE_TYPE").equals("BOTH")){
                                        if (type_ads == 0) {
                                            postList.add(new Post().setViewType(8));
                                            type_ads = 1;
                                        }else if (type_ads == 1){
                                            postList.add(new Post().setViewType(4));
                                            type_ads = 0;
                                        }
                                    }
                                }
                            }
                        }
                        postAdapter.notifyDataSetChanged();
                        page++;
                        loading=true;

                    }else {

                    }
                    relative_layout_load_more.setVisibility(View.GONE);
                }
            }
            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                relative_layout_load_more.setVisibility(View.GONE);
            }
        });
    }
    public void loadVideo(){
        imageView_empty_favorite.setVisibility(View.GONE);
        swipe_refreshl_image_search.setRefreshing(true);
        linear_layout_page_error.setVisibility(View.GONE);
        recycler_view_image_search.setVisibility(View.GONE);

        Retrofit retrofit = apiClient.getClient();
        apiRest service = retrofit.create(apiRest.class);
        Call<List<Post>> call = service.searchImage(prefManager.getString("ORDER_DEFAULT_STATUS"),language,page,query);
        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                swipe_refreshl_image_search.setRefreshing(false);
                apiClient.FormatData(SearchActivity.this,response);
                if(response.isSuccessful()){
                    if (response.body().size()!=0){
                        postList.clear();
                        for (int i=0;i<response.body().size();i++){
                            postList.add(response.body().get(i));
                            if (native_ads_enabled){
                                item++;
                                if (item == lines_beetween_ads ){
                                    item= 0;
                                    if (prefManager.getString("ADMIN_NATIVE_TYPE").equals("ADMON")) {
                                        postList.add(new Post().setViewType(8));
                                    }else if (prefManager.getString("ADMIN_NATIVE_TYPE").equals("FACEBOOK")){
                                        postList.add(new Post().setViewType(4));
                                    } else if (prefManager.getString("ADMIN_NATIVE_TYPE").equals("BOTH")){
                                        if (type_ads == 0) {
                                            postList.add(new Post().setViewType(8));
                                            type_ads = 1;
                                        }else if (type_ads == 1){
                                            postList.add(new Post().setViewType(4));
                                            type_ads = 0;
                                        }
                                    }
                                }
                            }
                        }
                        postAdapter.notifyDataSetChanged();
                        page++;
                        loaded=true;
                        imageView_empty_favorite.setVisibility(View.GONE);
                        recycler_view_image_search.setVisibility(View.VISIBLE);
                        linear_layout_page_error.setVisibility(View.GONE);
                    }else {
                        imageView_empty_favorite.setVisibility(View.VISIBLE);
                        recycler_view_image_search.setVisibility(View.GONE);
                        linear_layout_page_error.setVisibility(View.GONE);
                    }


                }else{
                    imageView_empty_favorite.setVisibility(View.GONE);
                    recycler_view_image_search.setVisibility(View.GONE);
                    linear_layout_page_error.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                swipe_refreshl_image_search.setRefreshing(false);
                recycler_view_image_search.setVisibility(View.GONE);
                linear_layout_page_error.setVisibility(View.VISIBLE);
                imageView_empty_favorite.setVisibility(View.GONE);

            }
        });
    }
}
