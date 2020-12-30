package com.virmana.news_app.ui.fragement;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.orhanobut.hawk.Hawk;
import com.virmana.news_app.R;
import com.virmana.news_app.Adapters.PostAdapter;
import com.virmana.news_app.model.Post;
import com.virmana.news_app.Provider.PrefManager;

import java.util.ArrayList;
import java.util.List;


public class FavoritesFragment extends Fragment {


    private RelativeLayout activity_favorites;
    private RecyclerView recycle_view_home_favorite;
    private ImageView imageView_empty_favorite;
    private SwipeRefreshLayout swipe_refreshl_home_favorite;
    private List<Post> postList =new ArrayList<Post>();
    private PostAdapter postAdapter;

    private View view;
    private GridLayoutManager gridLayoutManager;
    private PrefManager prf;
    private Integer type_ads = 0 ;
    private Integer item = 0 ;
    private Integer lines_beetween_ads = 8 ;
    private Boolean native_ads_enabled = false ;
    public FavoritesFragment() {
        // Required empty public constructor
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser){
            item = 0;
            getStatus();
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_favorites, container, false);
        this.prf= new PrefManager(getActivity().getApplicationContext());

        iniView(view);
        initAction();
       // getPost();
        return view;
    }





    public void iniView(View  view){


        PrefManager prefManager= new PrefManager(getActivity().getApplicationContext());
        if (!prefManager.getString("ADMIN_NATIVE_TYPE").equals("FALSE")){
            native_ads_enabled=true;
            lines_beetween_ads=Integer.parseInt(prefManager.getString("ADMIN_NATIVE_LINES"));
        }
        if (prefManager.getString("SUBSCRIBED").equals("TRUE")) {
            native_ads_enabled=false;
        }
        this.activity_favorites=(RelativeLayout) view.findViewById(R.id.activity_favorites);

        this.recycle_view_home_favorite=(RecyclerView) view.findViewById(R.id.recycle_view_home_favorite);
        this.swipe_refreshl_home_favorite=(SwipeRefreshLayout) view.findViewById(R.id.swipe_refreshl_home_favorite);
        this.imageView_empty_favorite=(ImageView) view.findViewById(R.id.imageView_empty_favorite);




        this.gridLayoutManager=  new GridLayoutManager(getActivity().getApplicationContext(),1,GridLayoutManager.VERTICAL,false);


        this.recycle_view_home_favorite=(RecyclerView) this.view.findViewById(R.id.recycle_view_home_favorite);
        this.swipe_refreshl_home_favorite=(SwipeRefreshLayout)  this.view.findViewById(R.id.swipe_refreshl_home_favorite);


        postAdapter =new PostAdapter(postList,null,null,null,getActivity(),true,false);
        recycle_view_home_favorite.setHasFixedSize(true);
        recycle_view_home_favorite.setAdapter(postAdapter);
        recycle_view_home_favorite.setLayoutManager(gridLayoutManager);
    }

    public void initAction(){
        this.swipe_refreshl_home_favorite.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                item = 0;
                getStatus();
            }
        });
    }

    private void getStatus() {

        PrefManager prefManager= new PrefManager(getActivity().getApplicationContext());


        swipe_refreshl_home_favorite.setRefreshing(true);
        List<Post> posts = new ArrayList<>();
        try {
            posts = Hawk.get("favorite");
            if (posts.size()!=0) {
            }
        }catch (NullPointerException e){
            posts = new ArrayList<>();
        }
        if (posts ==null){
            posts = new ArrayList<>();
        }
        if (posts.size()!=0){
            postList.clear();
            for (int i = 0; i< posts.size(); i++){
                Post a= new Post();
                a = posts.get(i) ;
                postList.add(a);
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
            imageView_empty_favorite.setVisibility(View.GONE);
            recycle_view_home_favorite.setVisibility(View.VISIBLE);
        }else{
            imageView_empty_favorite.setVisibility(View.VISIBLE);
            recycle_view_home_favorite.setVisibility(View.GONE);
        }

        swipe_refreshl_home_favorite.setRefreshing(false);

    }
}