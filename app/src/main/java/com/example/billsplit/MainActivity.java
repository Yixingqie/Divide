package com.example.billsplit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    ImageView imageview;
    //Button btnUpload;
    TextView txtView;
    Bitmap bitmap;
    ArrayList<String> priceList =new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        TabItem tabMembers = findViewById(R.id.members_tab);
        TabItem tabReceipt = findViewById(R.id.receipt_tab);
        TabItem tabTotals = findViewById(R.id.totals_tab);
        ViewPager2 viewPager = findViewById(R.id.viewPager);

        FragmentManager fm = getSupportFragmentManager();
        FragmentAdapter pagerAdapter = new FragmentAdapter(fm, getLifecycle());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(0);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });
      //  btnUpload = findViewById(R.id.btnUpload);
        txtView = findViewById(R.id.receiptHeader);
        bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                R.drawable.ocr_sample);

//        btnUpload.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                CropImage.activity()
//                        .setGuidelines(CropImageView.Guidelines.ON)
//                        .start(MainActivity.this);
//            }
//        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                Uri resultUri = result.getUri();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    getTextFromImage(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void getTextFromImage(Bitmap bitmap) {
        TextRecognizer txtRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (!txtRecognizer.isOperational()) {
            Toast.makeText(MainActivity.this, "Error Occurred with text recognizer", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Worked", Toast.LENGTH_SHORT).show();
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray items = txtRecognizer.detect(frame);
            StringBuilder strBuilder = new StringBuilder();
            Log.e("Int ","size: "+items.size());
            for (int i = 0; i < items.size(); i++) {
                TextBlock item = (TextBlock) items.valueAt(i);
                strBuilder.append(item.getValue());
                strBuilder.append("\n / \n");
                Log.e( "Value", item.getValue());
                for (Text line : item.getComponents()) {
                    //extract scanned text lines here
                    Log.e("lines", line.getValue());
                    String strchck = line.getValue().toString();
                    if(strchck.matches("(?:\\$)?\\d+(?:(\\.|\\s)\\d+)?"))
                    {
                        System.out.println("Price");
                        priceList.add(strchck);
                    }
                    else
                    {
                        System.out.println("Label: " + strchck);
                        if(Character.isDigit(strchck.charAt(0))){
                            System.out.println("Quantity Available");
                        }else{
                            System.out.println("Quantity unavailable");
                        }
                    }
                    for(int j = 0; j < line.getComponents().size(); j++){
                        System.out.println(line.getComponents().get(j).getValue());
                    }
                    for (Text element : line.getComponents()) {
                        //extract scanned text words here
                        Log.e("element", element.getValue());

                    }
                }
            }
            txtView.setText(strBuilder.toString().substring(0, strBuilder.toString().length() - 1));
            for(int i = 0; i < priceList.size(); i++){
                System.out.println(priceList.get(i));
            }
        }
    }

}
