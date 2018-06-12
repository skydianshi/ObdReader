package com.example.testfunctions.cardview;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.testfunctions.R;
public class CardFragment3 extends Fragment {

    private CardView mCardView;
    Button addButton;
    TextView title;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card_fragment3, container, false);
        mCardView = (CardView) view.findViewById(R.id.cardView3);
        mCardView.setMaxCardElevation(mCardView.getCardElevation()
                * CardAdapter.MAX_ELEVATION_FACTOR);
        title = (TextView)view.findViewById(R.id.title3);
        addButton = (Button)view.findViewById(R.id.addButton3);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addButton.setText("已添加");
                addButton.setEnabled(false);
                System.out.println(title.getText());
            }
        });
        return view;
    }

}
