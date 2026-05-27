package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 0;
    private Button button_0097, button_0153, button_0154, button_0213, button_0266, button_0266H;
    private Button button_0270, button_0290, button_0290H, button_0370, button_0420;
    private RadioGroup radioGroup;
    public int ColorValue = 0;
    //E-paper width and height
    int width0;
    int height0;
    int EpdMode; //E-paper color mode
    int EpdIC; //E-paper IC model
    int[] epdArray =new int[100];//Define the size of the image array (the array size needs to be defined, otherwise assigning values to array elements individually will cause the APP to crash); //Save e-paper resolution
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        button_0097 = findViewById(R.id.button_0097);
        button_0153 = findViewById(R.id.button_0153);
        button_0154 = findViewById(R.id.button_0154);
        button_0213 = findViewById(R.id.button_0213);
        button_0266 = findViewById(R.id.button_0266);
        button_0266H = findViewById(R.id.button_0266H);
        button_0270 = findViewById(R.id.button_0270);
        button_0290 = findViewById(R.id.button_0290);
        button_0290H = findViewById(R.id.button_0290H);
        button_0370 = findViewById(R.id.button_0370);
        button_0420 = findViewById(R.id.button_0420);


        // Find the target RadioButton
        RadioButton radioBtn2 = findViewById(R.id.radioButton_2color);
        RadioButton radioBtn3 = findViewById(R.id.radioButton_3color);
        RadioButton radioBtn4 = findViewById(R.id.radioButton_4color);
        //ColorValue = 2; //Default 2-color

        epdArray = data.getEpdArray(); //Globally get e-paper resolution
        if(epdArray[4]==2){
            ColorValue = 2; //2-color
            radioBtn2.setChecked(true);  //Monochrome
        }
        else if(epdArray[4]==3){
            ColorValue = 3; //3-color
            radioBtn3.setChecked(true);  //Three-color
        }
        else if(epdArray[4]==4){
            ColorValue = 4; //4-color
            radioBtn4.setChecked(true);  //Four-color
        }
        else
        {
            ColorValue = 2; //Default 2-color
            radioBtn2.setChecked(true); //Default monochrome
        }


        radioGroup = findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton selectedRadioButton = findViewById(checkedId);
                // Get the text content of the RadioButton
                String radioButtonText = selectedRadioButton.getText().toString();
                //Toast.makeText(MainActivity.this,  radioButtonText, Toast.LENGTH_SHORT).show();

                if (radioButtonText.equals("2-color")) //JAVA uses equals() to compare strings
                    ColorValue = 2; //2-color
                else if (radioButtonText.equals("3-color"))
                    ColorValue = 3; //3-color
                else if (radioButtonText.equals("4-color"))
                    ColorValue = 4; //4-color
            }
        });
        button_0097.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                epdArray[0] = 88 / 256; //E-paper width W
                epdArray[1] = 88 % 256;
                epdArray[2] = 184 / 256; //E-paper height H
                epdArray[3] = 184 % 256;
                epdArray[4] = ColorValue; //Color mode is black and white monochrome
                epdArray[5] = 2; //IC model is SSD series
                epdArray[6] = 97; //E-paper size is 0.97 inches
                data.setEpdArray(epdArray); //Globally store e-paper resolution
                Intent intent = new Intent(MainActivity.this, activity_imageview.class); //Jump to image processing interface
                startActivity(intent);
            }});
        button_0153.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                epdArray[0] = 152 / 256; //E-paper width W
                epdArray[1] = 152 % 256;
                epdArray[2] = 152 / 256; //E-paper height H
                epdArray[3] = 152 % 256;
                epdArray[4] = ColorValue; //Color mode is black and white monochrome
                epdArray[5] = 2; //IC model is SSD series
                epdArray[6] = 153; //E-paper size is 1.54 inches low resolution
                data.setEpdArray(epdArray); //Globally store e-paper resolution
                Intent intent = new Intent(MainActivity.this, activity_imageview.class); //Jump to image processing interface
                startActivity(intent);
            }});
        button_0154.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                epdArray[0] = 200 / 256; //E-paper width W
                epdArray[1] = 200 % 256;
                epdArray[2] = 200 / 256; //E-paper height H
                epdArray[3] = 200 % 256;
                epdArray[4] = ColorValue; //Color mode is black and white monochrome
                epdArray[5] = 2; //IC model is SSD series
                epdArray[6] = 154; //E-paper size is 1.54 inches high resolution
                data.setEpdArray(epdArray); //Globally store e-paper resolution
                Intent intent = new Intent(MainActivity.this, activity_imageview.class); //Jump to image processing interface
                startActivity(intent);
            }});
        button_0213.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                epdArray[0] = 128 / 256; //E-paper width W
                epdArray[1] = 128 % 256;
                epdArray[2] = 250 / 256; //E-paper height H
                epdArray[3] = 250 % 256;
                epdArray[4] = ColorValue; //Color mode is black and white monochrome
                epdArray[5] = 2; //IC model is SSD series
                epdArray[6] = 213; //E-paper size is 2.13 inches
                data.setEpdArray(epdArray); //Globally store e-paper resolution
                Intent intent = new Intent(MainActivity.this, activity_imageview.class); //Jump to image processing interface
                startActivity(intent);
            }});
        button_0266.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                epdArray[0] = 152 / 256; //E-paper width W
                epdArray[1] = 152 % 256;
                epdArray[2] = 296 / 256; //E-paper height H
                epdArray[3] = 296 % 256;
                epdArray[4] = ColorValue; //Color mode is black and white monochrome
                epdArray[5] = 2; //IC model is SSD series
                epdArray[6] = 266; //E-paper size is 2.66 inches
                data.setEpdArray(epdArray); //Globally store e-paper resolution
                Intent intent = new Intent(MainActivity.this, activity_imageview.class); //Jump to image processing interface
                startActivity(intent);
            }});
        button_0266H.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                epdArray[0] = 184 / 256; //E-paper width W
                epdArray[1] = 184 % 256;
                epdArray[2] = 360 / 256; //E-paper height H
                epdArray[3] = 360 % 256;
                epdArray[4] = ColorValue; //Color mode is black and white monochrome
                epdArray[5] = 2; //IC model is SSD series
                epdArray[6] = 267; //E-paper size is 2.66 inches high resolution
                data.setEpdArray(epdArray); //Globally store e-paper resolution
                if(ColorValue==4)  //High resolution only supports four-color
                {
                    Intent intent = new Intent(MainActivity.this, activity_imageview.class); //Jump to image processing interface
                    startActivity(intent);
                }
            }});
        button_0270.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                epdArray[0] = 176 / 256; //E-paper width W
                epdArray[1] = 176 % 256;
                epdArray[2] = 264 / 256; //E-paper height H
                epdArray[3] = 264 % 256;
                epdArray[4] = ColorValue; //Color mode is black and white monochrome
                epdArray[5] = 2; //IC model is SSD series
                epdArray[6] = 270; //E-paper size is 2.7 inches
                data.setEpdArray(epdArray); //Globally store e-paper resolution
                Intent intent = new Intent(MainActivity.this, activity_imageview.class); //Jump to image processing interface
                startActivity(intent);
            }});
        button_0290.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                epdArray[0] = 128 / 256; //E-paper width W
                epdArray[1] = 128 % 256;
                epdArray[2] = 296 / 256; //E-paper height H
                epdArray[3] = 296 % 256;
                epdArray[4] = ColorValue; //Color mode is black and white monochrome
                epdArray[5] = 2; //IC model is SSD series
                epdArray[6] = 290; //E-paper size is 2.9 inches
                data.setEpdArray(epdArray); //Globally store e-paper resolution
                Intent intent = new Intent(MainActivity.this, activity_imageview.class); //Jump to image processing interface
                startActivity(intent);
            }});
        button_0290H.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                epdArray[0] = 168 / 256; //E-paper width W
                epdArray[1] = 168 % 256;
                epdArray[2] = 384 / 256; //E-paper height H
                epdArray[3] = 384 % 256;
                epdArray[4] = ColorValue; //Color mode is black and white monochrome
                epdArray[5] = 2; //IC model is SSD series
                epdArray[6] = 291; //E-paper size is 2.9 inches high resolution
                data.setEpdArray(epdArray); //Globally store e-paper resolution
                if(ColorValue==4)  //High resolution only supports four-color
                {
                    Intent intent = new Intent(MainActivity.this, activity_imageview.class); //Jump to image processing interface
                    startActivity(intent);
                }
            }});
        button_0370.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                epdArray[0] = 240 / 256; //E-paper width W
                epdArray[1] = 240 % 256;
                epdArray[2] = 416 / 256; //E-paper height H
                epdArray[3] = 416 % 256;
                epdArray[4] = ColorValue; //Color mode is black and white monochrome
                epdArray[5] = 2; //IC model is SSD series
                epdArray[6] = 370; //E-paper size is 3.7 inches
                data.setEpdArray(epdArray); //Globally store e-paper resolution
                Intent intent = new Intent(MainActivity.this, activity_imageview.class); //Jump to image processing interface
                startActivity(intent);
            }});
        button_0420.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                epdArray[0] = 400 / 256; //E-paper width W
                epdArray[1] = 400 % 256;
                epdArray[2] = 300 / 256; //E-paper height H
                epdArray[3] = 300 % 256;
                epdArray[4] = ColorValue; //Color mode is black and white monochrome
                epdArray[5] = 2; //IC model is SSD series
                epdArray[6] = 420; //E-paper size is 4.2 inches
                data.setEpdArray(epdArray); //Globally store e-paper resolution
                if(ColorValue!=4)  //Does not support four-color refresh
                {
                    Intent intent = new Intent(MainActivity.this, activity_imageview.class); //Jump to image processing interface
                    startActivity(intent);
                }
            }});

    }
}