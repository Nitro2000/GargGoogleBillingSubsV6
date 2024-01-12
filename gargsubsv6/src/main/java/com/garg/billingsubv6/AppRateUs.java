package com.garg.billingsubv6;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatRatingBar;

public class AppRateUs {


    public static void showDifferentStoreAlert(@NonNull Context context, AppRateUsEventListener callRateEvent) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialogue_rating);
        dialog.setCancelable(false);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        ImageView close = dialog.findViewById(R.id.close);
        if (close != null) {
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    dialog.dismiss();
                    callRateEvent.onCloseDialog();
                }
            });
        }

        AppCompatRatingBar ratingBar = dialog.findViewById(R.id.customRatingBar);
        AppCompatButton submitBtn = dialog.findViewById(R.id.submitBtn);

        submitBtn.setOnClickListener(view -> {
            if (ratingBar.getRating() > 3f) {
                dialog.dismiss();
                callRateEvent.onRateMoreThree();
            } else if (ratingBar.getRating() == 0f) {
                Toast.makeText(context, "Please fill star", Toast.LENGTH_SHORT).show();
            } else {
                dialog.dismiss();
                callRateEvent.onRateEqualLessThree();
            }
        });
        dialog.show();
    }


}
