package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.Objects;
import android.view.KeyEvent;

//NFC
import android.provider.Settings;

import java.io.IOException;

public class activity_imageview extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private TextView textView1;
    ImageView imageView;
    private Button button_camera,button_bitmap,button_text;
    Bitmap bitmap0; //Original image
    Bitmap bitmap90; //Image rotated 90 degrees
    int[] epdArray =new int[100];//Define the size of the image array (the array size must be defined, otherwise assigning values to array elements individually will cause the APP to crash); //Save e-paper resolution
    //E-paper width and height
    public int width0;//E-paper width
    public int height0;//E-paper height
    byte[] image_buffer = new byte[100000];//Define the size of the image array
    int epdInch;//E-paper size
    int epdColor;//E-paper color
    int epdIC;//E-paper IC
    int epdWidth;//Global e-paper width
    int epdHeight;//Global e-paper height
    int dataNum; //Data transmission progress
    //NFC
    // NFC i/o operation class IsoDep
    IntentFilter[] mWriteTagFilters;
    PendingIntent mNfcPendingIntent;
    NfcAdapter mNfcAdapter;
    IsoDep isodep;
    Tag detectedTag;
    byte[] cmd;
    byte[] response = new byte[2]; //TAG feedback data
    byte ScreenIndex_BW = 0;  //Current screen to write
    byte ScreenIndex_R = 1;  //Current screen to write
    private TextView resultTextView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_imageview);

        imageView = findViewById(R.id.imageView);
        resultTextView = findViewById(R.id.result_text_view);
        //Display the default image of the current e-paper
        epdArray = data.getEpdArray(); //Globally obtain e-paper resolution
        epdInch = epdArray[6];//E-paper size
        epdColor = epdArray[4];//E-paper color
        epdIC = epdArray[5];//E-paper IC model
        epdWidth=epdArray[0]*256+epdArray[1];//Global e-paper width
        epdHeight=epdArray[2]*256+epdArray[3];//Global e-paper height

        if(epdInch>=420) //For e-paper larger than 4.2 inches, swap width and height to facilitate capturing larger images. Vertical scanning is used for pattern extraction without rotating 90 degrees
        {
            epdWidth=epdArray[2] * 256 + epdArray[3];//E-paper height
            epdHeight =epdArray[0] * 256 + epdArray[1];//E-paper width
        }



        //Do not display the app name in the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(""); // Set to empty string, do not display the default app name


        if (epdColor == 2) //Monochrome e-paper
        {
            switch (epdInch) //E-paper size
            {
                case 97:
                    imageView.setImageResource(R.drawable.bw097); // Replace your_image_name with your actual image resource name
                    break;
                case 153:
                    imageView.setImageResource(R.drawable.bw153);
                    break;
                case 154:
                    imageView.setImageResource(R.drawable.bw154);
                    break;
                case 213:
                    imageView.setImageResource(R.drawable.bw213);
                    break;
                case 266:
                    imageView.setImageResource(R.drawable.bw266);
                    break;
                case 270:
                    imageView.setImageResource(R.drawable.bw270);
                    break;
                case 290:
                    imageView.setImageResource(R.drawable.bw290);
                    break;
                case 370:
                    imageView.setImageResource(R.drawable.bw370);
                    break;
                case 420:
                    imageView.setImageResource(R.drawable.bw420);
                    break;
                default:
                    break;
            }
            // Call the method to convert the image in ImageView to Bitmap
            bitmap0 = getBitmapFromImageView(imageView);

        }
        if (epdColor == 3) //Tri-color e-paper
        {
            switch (epdInch) //E-paper size
            {
                case 97:
                    imageView.setImageResource(R.drawable.r097); // Replace your_image_name with your actual image resource name
                    break;
                case 153:
                    imageView.setImageResource(R.drawable.r153);
                    break;
                case 154:
                    imageView.setImageResource(R.drawable.r154);
                    break;
                case 213:
                    imageView.setImageResource(R.drawable.r213);
                    break;
                case 266:
                    imageView.setImageResource(R.drawable.r266);
                    break;
                case 270:
                    imageView.setImageResource(R.drawable.r270);
                    break;
                case 290:
                    imageView.setImageResource(R.drawable.r290);
                    break;
                case 370:
                    imageView.setImageResource(R.drawable.r370);
                    break;
                case 420:
                    imageView.setImageResource(R.drawable.r420);
                    break;
                default:
                    break;
            }
            // Call the method to convert the image in ImageView to Bitmap
            bitmap0 = getBitmapFromImageView(imageView);
        }
        if (epdColor == 4) //Four-color e-paper
        {
            switch (epdInch) //E-paper size
            {
                case 97:
                    imageView.setImageResource(R.drawable.f097); // Replace your_image_name with your actual image resource name
                    break;
                case 153:
                    imageView.setImageResource(R.drawable.f153);
                    break;
                case 154:
                    imageView.setImageResource(R.drawable.f154);
                    break;
                case 213:
                    imageView.setImageResource(R.drawable.f213);
                    break;
                case 266:
                    imageView.setImageResource(R.drawable.f266);
                    break;
                case 267:
                    imageView.setImageResource(R.drawable.f267);
                    break;
                case 270:
                    imageView.setImageResource(R.drawable.f270);
                    break;
                case 290:
                    imageView.setImageResource(R.drawable.f290);
                    break;
                case 291:
                    imageView.setImageResource(R.drawable.f291);
                    break;
                case 370:
                    imageView.setImageResource(R.drawable.f370);
                    break;
                case 420:
                    imageView.setImageResource(R.drawable.f420);
                    break;
                default:
                    break;
            }
            // Call the method to convert the image in ImageView to Bitmap
            bitmap0 = getBitmapFromImageView(imageView);
        }


    }






    //Mobile phone back button operation
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Create an Intent to jump to the Activity corresponding to Interface 1 (assumed to be Interface1Activity here)
            android.content.Intent intent = new android.content.Intent(this, MainActivity.class); //Main page
            // Add flag to destroy all Activities above Interface 1
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish(); // End the current Activity of Interface 3
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


//Convert imageView to bitmap

    private Bitmap getBitmapFromImageView(ImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        if (drawable == null) {
            return null;
        }
        if (drawable instanceof BitmapDrawable) {
            // If it is a BitmapDrawable type, get its Bitmap directly
            return ((BitmapDrawable) drawable).getBitmap();
        }
        return null;
    }
    //Image pattern extraction
//Convert Bitmap to array
//ByteArrayOutputStream bitmapBuffer= new ByteArrayOutputStream(); //Create byte array buffer
//Vertical scanning, universal for all e-papers. Small-size e-papers need to rotate the bitmap by 90 degrees, while large-size e-papers above 4.2 inches do not need rotation
    public byte[] GetPictureData_SSD(int mode)  //mode=0 BW  mode=1 RW
    {
        int index = 0;
        byte temp = 0;
        Bitmap bitmap = bitmap90;//Process the image data passed from the second page and rotate it 90 degrees
        width0 = bitmap.getWidth();
        height0 = bitmap.getHeight();
        for (int i = width0 - 1; i >= 0; i--)                      //Traverse the X-axis direction starting from the upper left corner of the image, mirror pattern extraction for SSD1.54~2.9 series
        {
            temp = 0;
            for (int j = 0; j <= height0 / 8 - 1; j++)             //8 pixels on Y-axis
            {
                for (int k = 0; k < 8; k++)
                {
                    temp = (byte)(temp * 2);
                    int pixel = bitmap.getPixel(i, (j * 8) + k); //Vertical scanning, i is the abscissa, j*8+k is the ordinate
                    int r = (pixel & 0xff0000) >> 16;
                    int g = (pixel & 0xff00) >> 8;
                    int b = (pixel & 0xff);
                    if (mode == 0)  //Black and white data
                    {  //BW mode
                        if (r <= 100 && g <= 100 && b <= 100)   //RGB black
                            temp = (byte)(temp + 0);  //Black
                        else
                            temp = (byte)(temp + 1); //White
                    }
                    if (mode == 1) //Red and white data
                    { //RW mode
                        if (r >= 100 && g <= 100 && b <= 100)   //RGB red (SSD series red and white data need to be inverted, handled in the display function)
                            temp = (byte)(temp + 0);  //Red
                        else
                            temp = (byte)(temp + 1); //White
                    }
                }
                image_buffer[index] = temp;
                index++;
            }
        }
        return image_buffer;
    }
    //Vertical scanning, universal for all e-papers. Small-size e-papers need to rotate the bitmap by 90 degrees, while large-size e-papers above 4.2 inches do not need rotation
    public byte[] GetPictureData_4G()
    {
        int index = 0;
        byte temp = 0;
        Bitmap bitmap = bitmap90;//Process the image data passed from the second page and rotate it 90 degrees
        width0 = bitmap.getWidth();
        height0 = bitmap.getHeight();
        for (int i = width0 - 1; i >= 0; i--)                      //Traverse the X-axis direction starting from the upper left corner of the image, mirror pattern extraction for SSD1.54~2.9 series
        {
            temp = 0;
            for (int j = 0; j <= height0 / 4 - 1; j++)             //4 pixels on Y-axis
            {
                for (int k = 0; k < 4; k++)
                {
                    temp = (byte)(temp * 4);
                    int pixel = bitmap.getPixel(i, (j * 4) + k); //Vertical scanning, i is the abscissa, j*8+k is the ordinate
                    int r = (pixel & 0xff0000) >> 16;
                    int g = (pixel & 0xff00) >> 8;
                    int b = (pixel & 0xff);

                    //BW mode
                    if (r <= 100 && g <= 100 && b <= 100)   //RGB black
                        temp = (byte)(temp + 0x00);  //Black
                    else if (r >= 200 && g >= 200 && b >= 200)   //RGB white
                        temp = (byte)(temp + 0x01); //White
                    else
                    {
                        int num = (r + g + b) / 3;
                        if (num <= 127)
                            temp = (byte)(temp + 0x03); //Gray 1 Red
                        else
                            temp = (byte)(temp + 0x02); //Gray 2 Yellow
                    }
                }
                image_buffer[index] = temp;
                index++;
            }
        }
        return image_buffer;
    }



















    //********************************NFC************************************************************//
    @Override
    protected void onResume() {
        super.onResume();

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            // Device does not support NFC
            resultTextView.setText("NFC is not supported!");
            //finish();
            return;
        }
        if (!mNfcAdapter.isEnabled()) {
            // NFC is not enabled, guide the user to enable NFC in settings
            Intent setNfc = new Intent(Settings.ACTION_NFC_SETTINGS);
            startActivity(setNfc);
        };
        resultTextView.setText("NFC is ready!");
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        mWriteTagFilters = new IntentFilter[]{tagDetected};
        //When an NFC tag is detected, PendingIntent is used to return to this Activity
        mNfcPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE); //PendingIntent.FLAG_IMMUTABLE is required, otherwise the APP will crash
        //Create an NFC foreground dispatch task, otherwise the APP will restart after NFC recognition
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mWriteTagFilters, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Tag writing mode
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Thread t2 = new Thread() {
                @Override
                public void run() {
                    writeTag(detectedTag);
                }
            };
            t2.start();

        }
    }


    protected void writeTag(Tag tag) {
        String[] tech = tag.getTechList();
        if (tech[0].equals("android.nfc.tech.IsoDep")) {
            isodep = IsoDep.get(tag);
            try {
                isodep.setTimeout(50000);//Increase NFC timeout setting to prevent NFC disconnection
                if (!isodep.isConnected()) {
                    isodep.connect();
                }
                if (isodep.isConnected()) //NFC connected (newly added)
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            resultTextView.setText("NFC is connect");
                            count=0; //Clear time counter
                        }
                    });
                    stopRefreshMonitoring();  //Stop refresh timing

                    //1.IC DIY  DB command
                    cmd = HexString2Btyes("F0DB020000");    //IC_DIY = "F0DB020000";
                    response = isodep.transceive(cmd);
                    delay_ms(10);// Pause for 10 milliseconds, parameter unit is milliseconds
                    //2.Write e-paper parameters
                    cmd = HexString2Btyes(data.setEpdInit(epdColor, epdInch)[0]);    //E-paper initialization parameter setting operation
                    response = isodep.transceive(cmd);
                    delay_ms(10);// Pause for 10 milliseconds, parameter unit is milliseconds
                    //3.Write e-paper parameters
                    cmd = HexString2Btyes(data.setEpdInit(epdColor, epdInch)[1]);    //Screen switching
                    response = isodep.transceive(cmd);

                    if (response[0] == (byte) 0x90) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                resultTextView.setText("90");
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                resultTextView.setText("Init NG");
                            }
                        });
                    }
                    delay_ms(10);// Pause for 10 milliseconds, parameter unit is milliseconds
                    //4.Transfer image
                    //The image must be rotated 90 degrees----otherwise the image display will be garbled>>>>>>>>>>>>>>>>
                    //Assign the rotated image to bitmap90, keep bitmap0 unchanged to prevent garbled characters caused by data transmission interruption.
                    bitmap90 = rotateBitmap(bitmap0, 90); //Rotate 90 degrees to facilitate vertical scanning data processing


                    //3.7-inch monochrome transfers images twice
                    if ((epdColor == 2) && (epdInch == 370))  //Black, white and red image transfer
                    {
                        GetPictureData_SSD(1);//Placeholder operation to obtain red and white image data. This step is necessary, otherwise the first NFC refresh cannot transmit data!!!
                        //Transfer black and white data
                        int datas = width0 * height0 / 8; //Total amount of e-paper data
                        int i;
                        for (i = 0; i < datas / 250; i++) {
                            cmd = new byte[250 + 5];
                            cmd[0] = (byte) 0xF0;
                            cmd[1] = (byte) 0xD2;
                            cmd[2] = (byte) ScreenIndex_BW;//Image data writing area
                            cmd[3] = (byte) i;// Index number
                            cmd[4] = (byte) 0xFA;
                            for (int j = 0; j < 250; j++) {
                                cmd[j + 5] = (byte) 0xFF;
                            }
                            response = isodep.transceive(cmd);//Send black and white data
                            //Text2.Text = (i + 1).ToString() + "SendData24:" + HexToString(response);  //Display feedback data

                            //Data tail judgment
                            if (i == datas / 250 - 1 && datas % 250 != 0) //Send data tail, fill in insufficient data
                            {
                                cmd = new byte[250 + 5];
                                cmd[0] = (byte) 0xF0;
                                cmd[1] = (byte) 0xD2;
                                cmd[2] = (byte) ScreenIndex_BW;  //Image data writing area, UC series
                                cmd[3] = (byte) (i + 1);  // Index number
                                cmd[4] = (byte) 0xFA;
                                for (int j = 0; j < 250; j++) {
                                    cmd[j + 5] = (byte) 0xFF;
                                }
                                response = isodep.transceive(cmd);//Send black and white data
                            }
                            dataNum = i;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    float aa = (float)(250 * dataNum) / (float)(datas * 2);//Data transmission progress
                                    int bb = (int) (aa * 100); //Enlarge data by 100 times
                                    String str = String.valueOf(bb);
                                    resultTextView.setText("Data complete:" + str + "%");
                                }
                            });
                        }

                        //Transfer red and white data
                        GetPictureData_SSD(0);//Obtain black and white image data
                        for (i = 0; i < datas / 250; i++) {
                            cmd = new byte[250 + 5];
                            cmd[0] = (byte) 0xF0;
                            cmd[1] = (byte) 0xD2;
                            cmd[2] = (byte) ScreenIndex_R;//Write image data into red area
                            cmd[3] = (byte) i;// Index number
                            cmd[4] = (byte) 0xFA;
                            for (int j = 0; j < 250; j++) {
                                cmd[j + 5] = image_buffer[j + 250 * i]; //Red needs to be inverted
                            }
                            response = isodep.transceive(cmd);//Send black and white data

                            //Data tail judgment
                            if (i == datas / 250 - 1 && datas % 250 != 0) //Send data tail, fill in insufficient data
                            {
                                cmd = new byte[250 + 5];
                                cmd[0] = (byte) 0xF0;
                                cmd[1] = (byte) 0xD2;
                                cmd[2] = (byte) ScreenIndex_R;  //Write image data into red area
                                cmd[3] = (byte) (i + 1);  // Index number
                                cmd[4] = (byte) 0xFA;
                                for (int j = 0; j < 250; j++) {
                                    cmd[j + 5] = image_buffer[j + 250 * (datas / 250)];//Red needs to be inverted
                                }
                                response = isodep.transceive(cmd);//Send black and white data
                            }
                            dataNum = i;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    float aa = (float)(250 * dataNum + datas) / (float)(datas * 2);//Data transmission progress
                                    int bb = (int) (aa * 100); //Enlarge data by 100 times
                                    String str = String.valueOf(bb);
                                    resultTextView.setText("Data complete:" + str + "%");
                                }
                            });
                        }
                    }
                    //4.2-inch monochrome transfers images twice
                    else if ((epdColor == 2) && (epdInch == 420))  //Black, white and red image transfer
                    {
                        //Transfer black and white data
                        GetPictureData_SSD(0);//Obtain black and white image data
                        int datas = width0 * height0 / 8; //Total amount of e-paper data
                        int i;
                        for (i = 0; i < datas / 250; i++) {
                            cmd = new byte[250 + 5];
                            cmd[0] = (byte) 0xF0;
                            cmd[1] = (byte) 0xD2;
                            cmd[2] = (byte) ScreenIndex_BW;//Image data writing area
                            cmd[3] = (byte) i;// Index number
                            cmd[4] = (byte) 0xFA;
                            for (int j = 0; j < 250; j++) {
                                cmd[j + 5] = image_buffer[j + 250 * i];
                            }
                            response = isodep.transceive(cmd);

                            //Data tail judgment
                            if (i == datas / 250 - 1 && datas % 250 != 0) //Send data tail, fill in insufficient data
                            {
                                cmd = new byte[250 + 5];
                                cmd[0] = (byte) 0xF0;
                                cmd[1] = (byte) 0xD2;
                                cmd[2] = (byte) ScreenIndex_BW;  //Image data writing area
                                cmd[3] = (byte) (i + 1);  // Index number
                                cmd[4] = (byte) 0xFA;
                                for (int j = 0; j < 250; j++) {
                                    cmd[j + 5] = image_buffer[j + 250 * (datas / 250)];
                                }
                                response = isodep.transceive(cmd);//Send black and white data
                            }
                            dataNum = i;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    float aa = (float) (250 * dataNum) / (float) (datas * 2);//Data transmission progress
                                    int bb = (int) (aa * 100); //Enlarge data by 100 times
                                    String str = String.valueOf(bb);
                                    resultTextView.setText("Data complete:" + str + "%");
                                }
                            });
                        }
                        //Transmitting red and white data
                        GetPictureData_SSD(1);//Obtain red and white image data
                        for (i = 0; i < datas / 250; i++) {
                            cmd = new byte[250 + 5];
                            cmd[0] = (byte) 0xF0;
                            cmd[1] = (byte) 0xD2;
                            cmd[2] = (byte) ScreenIndex_R;//Write image data into the red area
                            cmd[3] = (byte) i;// Index number
                            cmd[4] = (byte) 0xFA;
                            for (int j = 0; j < 250; j++) {
                                cmd[j + 5] = (byte) (0xFF - image_buffer[j + 250 * i]); //Red needs to be reversed
                            }
                            response = isodep.transceive(cmd);

                            //Data tail judgment
                            if (i == datas / 250 - 1 && datas % 250 != 0) //Send data tail, fill in insufficient data
                            {
                                cmd = new byte[250 + 5];
                                cmd[0] = (byte) 0xF0;
                                cmd[1] = (byte) 0xD2;
                                cmd[2] = (byte) ScreenIndex_R;  //Write image data into the red area
                                cmd[3] = (byte) (i + 1);  //Index number
                                cmd[4] = (byte) 0xFA;
                                for (int j = 0; j < 250; j++) {
                                    cmd[j + 5] = (byte) (0xFF - image_buffer[j + 250 * (datas / 250)]);//Red needs to be reversed
                                }
                                response = isodep.transceive(cmd);
                            }
                            dataNum = i;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    float aa = (float) (250 * dataNum + datas) / (float) (datas * 2);//Data transmission progress
                                    int bb = (int) (aa * 100); //Enlarge data by 100 times
                                    String str = String.valueOf(bb);
                                    resultTextView.setText("Data complete:" + str + "%");
                                }
                            });
                        }
                    }

                    else if ((epdColor == 2) && (epdInch != 370)&& (epdInch != 420))  //Black and white image transfer, special processing for 3.7 and 4.2 inches
                    {
                        GetPictureData_SSD(0);//Obtain black and white image data
                        int datas = width0 * height0 / 8; //Total amount of e-paper data
                        int i;
                        for (i = 0; i < datas / 250; i++) {
                            cmd = new byte[250 + 5];
                            cmd[0] = (byte) 0xF0;
                            cmd[1] = (byte) 0xD2;
                            cmd[2] = (byte) ScreenIndex_BW;//Image data writing area
                            cmd[3] = (byte) i;// Index number
                            cmd[4] = (byte) 0xFA;
                            for (int j = 0; j < 250; j++) {
                                cmd[j + 5] = image_buffer[j + 250 * i];
                            }
                            response = isodep.transceive(cmd);//Send black and white data
                            //Text2.Text = (i + 1).ToString() + "SendData24:" + HexToString(response);  //Display feedback data

                            //Data tail judgment
                            if (i == datas / 250 - 1 && datas % 250 != 0) //Send data tail, fill in insufficient data
                            {
                                cmd = new byte[250 + 5];
                                cmd[0] = (byte) 0xF0;
                                cmd[1] = (byte) 0xD2;
                                cmd[2] = (byte) ScreenIndex_BW;  //Image data writing area, UC series
                                cmd[3] = (byte) (i + 1);  // Index number
                                cmd[4] = (byte) 0xFA;
                                for (int j = 0; j < 250; j++) {
                                    cmd[j + 5] = image_buffer[j + 250 * (datas / 250)];
                                }
                                response = isodep.transceive(cmd);//Send black and white data
                            }
                            dataNum = i;

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    float aa = (float) (250 * dataNum) / (float) datas;//Data transmission progress
                                    int bb = (int) (aa * 100); //Enlarge data by 100 times
                                    String str = String.valueOf(bb);
                                    resultTextView.setText("Data complete:" + str + "%");
                                }
                            });


                        }
                    }
                    if (epdColor == 3)  //Black, white and red image transfer
                    {
                        //Transfer black and white data
                        GetPictureData_SSD(0);//Obtain black and white image data
                        int datas = width0 * height0 / 8; //Total amount of e-paper data
                        int i;
                        for (i = 0; i < datas / 250; i++) {
                            cmd = new byte[250 + 5];
                            cmd[0] = (byte) 0xF0;
                            cmd[1] = (byte) 0xD2;
                            cmd[2] = (byte) ScreenIndex_BW;//Image data writing area
                            cmd[3] = (byte) i;// Index number
                            cmd[4] = (byte) 0xFA;
                            for (int j = 0; j < 250; j++) {
                                cmd[j + 5] = image_buffer[j + 250 * i];
                            }
                            response = isodep.transceive(cmd);//Send black and white data
                            //Text2.Text = (i + 1).ToString() + "SendData24:" + HexToString(response);  //Display feedback data

                            //Data tail judgment
                            if (i == datas / 250 - 1 && datas % 250 != 0) //Send data tail, fill in insufficient data
                            {
                                cmd = new byte[250 + 5];
                                cmd[0] = (byte) 0xF0;
                                cmd[1] = (byte) 0xD2;
                                cmd[2] = (byte) ScreenIndex_BW;  //Image data writing area, UC series
                                cmd[3] = (byte) (i + 1);  // Index number
                                cmd[4] = (byte) 0xFA;
                                for (int j = 0; j < 250; j++) {
                                    cmd[j + 5] = image_buffer[j + 250 * (datas / 250)];
                                }
                                response = isodep.transceive(cmd);//Send black and white data
                            }
                            dataNum = i;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    float aa = (float)(250 * dataNum) / (float)(datas * 2);//Data transmission progress
                                    int bb = (int) (aa * 100); //Enlarge data by 100 times
                                    String str = String.valueOf(bb);
                                    resultTextView.setText("Data complete:" + str + "%");
                                }
                            });
                        }

                        //Transfer red and white data
                        GetPictureData_SSD(1);//Obtain red and white image data
                        for (i = 0; i < datas / 250; i++) {
                            cmd = new byte[250 + 5];
                            cmd[0] = (byte) 0xF0;
                            cmd[1] = (byte) 0xD2;
                            cmd[2] = (byte) ScreenIndex_R;//Write image data into red area
                            cmd[3] = (byte) i;// Index number
                            cmd[4] = (byte) 0xFA;
                            for (int j = 0; j < 250; j++) {
                                cmd[j + 5] = (byte) (0xFF - image_buffer[j + 250 * i]); //Red needs to be inverted
                            }
                            response = isodep.transceive(cmd);//Send black and white data

                            //Data tail judgment
                            if (i == datas / 250 - 1 && datas % 250 != 0) //Send data tail, fill in insufficient data
                            {
                                cmd = new byte[250 + 5];
                                cmd[0] = (byte) 0xF0;
                                cmd[1] = (byte) 0xD2;
                                cmd[2] = (byte) ScreenIndex_R;  //Write image data into red area
                                cmd[3] = (byte) (i + 1);  // Index number
                                cmd[4] = (byte) 0xFA;
                                for (int j = 0; j < 250; j++) {
                                    cmd[j + 5] = (byte) (0xFF - image_buffer[j + 250 * (datas / 250)]);//Red needs to be inverted
                                }
                                response = isodep.transceive(cmd);//Send black and white data
                            }
                            dataNum = i ;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    float aa = (float)(250 * dataNum + datas) / (float)(datas * 2);//Data transmission progress
                                    int bb = (int) (aa * 100); //Enlarge data by 100 times
                                    String str = String.valueOf(bb);
                                    resultTextView.setText("Data complete:" + str + "%");
                                }
                            });
                        }
                    }
                    if (epdColor == 4)  //Black, white, red and yellow data transmission
                    {
                        GetPictureData_4G();//Obtain black, white, red and yellow image data
                        int datas = width0 * height0 / 4; //Total amount of e-paper data
                        int i;
                        for (i = 0; i < datas / 250; i++) {
                            cmd = new byte[250 + 5];
                            cmd[0] = (byte) 0xF0;
                            cmd[1] = (byte) 0xD2;
                            cmd[2] = (byte) ScreenIndex_BW;//Image data writing area
                            cmd[3] = (byte) i;// Index number
                            cmd[4] = (byte) 0xFA;
                            for (int j = 0; j < 250; j++) {
                                cmd[j + 5] = image_buffer[j + 250 * i];
                            }
                            response = isodep.transceive(cmd);//Send black and white data

                            //Data tail judgment
                            if (i == datas / 250 - 1 && datas % 250 != 0) //Send data tail, fill in insufficient data
                            {
                                cmd = new byte[250 + 5];
                                cmd[0] = (byte) 0xF0;
                                cmd[1] = (byte) 0xD2;
                                cmd[2] = (byte) ScreenIndex_BW;  //Image data writing area, UC series
                                cmd[3] = (byte) (i + 1);  // Index number
                                cmd[4] = (byte) 0xFA;
                                for (int j = 0; j < 250; j++) {
                                    cmd[j + 5] = image_buffer[j + 250 * (datas / 250)];
                                }
                                response = isodep.transceive(cmd);//Send black and white data
                            }
                            dataNum = i;

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    float aa = (float) (250 * dataNum) / (float) datas;//Data transmission progress
                                    int bb = (int) (aa * 100); //Enlarge data by 100 times
                                    String str = String.valueOf(bb);
                                    resultTextView.setText("Data complete:" + str + "%");
                                }
                            });


                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            resultTextView.setText("Data complete:" + "100%");
                        }
                    });


                    byte[] refreshcmd = new byte[5];
                    if (epdColor == 4)//Four-color screen refresh command 4
                    {
                        //epd  refresh
                        //Support image refresh command D4
                        refreshcmd[0] = (byte) 0xF0;
                        refreshcmd[1] = (byte) 0xD4;
                        refreshcmd[2] = (byte) 0x85;//Power-on delay time 10*100ms, old models use 0x05 here, new models support four-color screens and need to be set to 0x85
                        refreshcmd[3] = (byte) 0x80; //Direct return + image data writing area
                        refreshcmd[4] = (byte) 0x00;
                        response = isodep.transceive(refreshcmd);//Send black and white data
                    } else  //Monochrome and tri-color screen refresh command
                    {
                        //epd  refresh
                        //Support image refresh command D4
                        refreshcmd[0] = (byte) 0xF0;
                        refreshcmd[1] = (byte) 0xD4;
                        refreshcmd[2] = (byte) 0x05;//Power-on delay time 10*100ms, old models use 0x05 here, new models support four-color screens and need to be set to 0x85
                        refreshcmd[3] = (byte) 0x80; //Direct return + image data writing area
                        refreshcmd[4] = (byte) 0x00;
                        response = isodep.transceive(refreshcmd);//Send black and white data
                    }


                    if (response[0] == (byte) 0x90) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                resultTextView.setText("E-paper is updating, please wait！");
                            }
                        });

                        startRefreshMonitoring(); //Display refresh time.

                    }
                    else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                count=0; //Clear time counter
                                resultTextView.setText("Update error！");
                                stopRefreshMonitoring();  //Stop refresh timing
                            }
                        });


                    }
                    //The image must be rotated 90 degrees----otherwise the image display will be garbled>>>>>>>>>>>>>>>>
                    //bitmap0 = rotateBitmap(bitmap0, 270); //Rotate 270 degrees to facilitate vertical scanning data processing

                }

            }
            catch (IOException e)
            {
                //throw new RuntimeException(e);
            }
            finally
            {  /*
                if (mNfcAdapter != null)
                {
                    try
                    {
                        isodep.close();

                    }
                    catch (IOException e)
                    {

                    }
                }*/

            }

        }
    }

    // Start refresh timing and NFC connection check
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable refreshRunnable;
    private int count = 0;
    private void startRefreshMonitoring() {
        count = 0; // Reset counter

        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                count++;

                // Check NFC connection status, temporarily not detected.
                if (!isodep.isConnected()) {
                    // NFC connection interrupted, update UI and stop loop
                    runOnUiThread(() -> {
                        resultTextView.setText("NFC connection error!");
                        stopRefreshMonitoring();  //Stop refresh timing
                    });
                    return; // Exit loop
                }

                // Update refresh time every 10 counts (about 1 second)


                if (count % 10 == 0) {
                    final int seconds = count / 10;
                    runOnUiThread(() -> {
                        resultTextView.setText("Refresh time: " + seconds + "s");
                    });
                }
                if (epdColor == 2&&count == 20)//Monochrome refresh time 2s
                {
                    runOnUiThread(() -> {
                        count=0;
                        resultTextView.setText("E-paper refresh complete！");
                    });
                    return; // Exit loop
                }
                else if (epdColor == 3&&count == 160)//Tri-color refresh time 16s
                {
                    runOnUiThread(() -> {
                        count=0;
                        resultTextView.setText("E-paper refresh complete！");
                    });
                    return; // Exit loop
                }
                else if (epdColor == 4&&count == 200)//Four-color refresh time 20s
                {
                    runOnUiThread(() -> {
                        count=0;
                        resultTextView.setText("E-paper refresh complete！");
                    });
                    return; // Exit loop
                }

                // Execute again after 100 milliseconds to form a loop
                handler.postDelayed(this, 100);
            }
        };

        // Start loop
        handler.post(refreshRunnable);
    }
    // Method to stop monitoring
    private void stopRefreshMonitoring() {
        if (handler != null && refreshRunnable != null) {
            // Remove all pending refreshRunnable callbacks in Handler
            handler.removeCallbacks(refreshRunnable);
        }
        // Optional: Reset counter and UI status
        count = 0;
        runOnUiThread(() -> {
            //resultTextView.setText("Refresh stopped");
        });
    }



    // Call when Activity is destroyed to prevent memory leaks
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRefreshMonitoring();
    }



    private void delay_ms(int xms) {
        try {
            Thread.sleep(xms); // Pause for xms milliseconds, parameter unit is milliseconds
        } catch (InterruptedException e) {
            // Catch thread interruption exception (must be handled as it is a checked exception)
            Thread.currentThread().interrupt(); // Restore thread interruption status for upper-layer logic to perceive
        }
    }





    public static Bitmap rotateBitmap(Bitmap bitmap, float angle) {
        // If the incoming Bitmap is empty, return null directly
        if (bitmap == null ) {
            return null;
        }
        // Get the width and height of the Bitmap
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // Create a Matrix object for image transformation
        Matrix matrix = new Matrix();
        // Set rotation angle, rotate around the center point of the image
        matrix.postRotate(angle);
        // Create a new rotated Bitmap based on the original Bitmap
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
        // If the rotated Bitmap is the same as the original Bitmap, return the rotated Bitmap directly
        if (newBitmap.sameAs(bitmap)) {
            return newBitmap;
        }
        // Recycle memory occupied by the original Bitmap
        //This must be canceled, otherwise it will cause the APP to crash when loading images for the second time
        //bitmap.recycle();

        return newBitmap;
    }


    private static String HexToString(byte[] data) //Convert byte to string
    {
        String ReData = "";
        for (int i = 0; i < data.length; i++)
        {
            ReData += NumToString(data[i] / 16) + NumToString(data[i] % 16);
        }
        return ReData;
    }
    private static String NumToString(int c)  //Convert number to string
    {

        String data = "";
        if (c >= 10)
        {
            switch (c)
            {
                case 10:
                    data = "A";
                    break;
                case 11:
                    data = "B";
                    break;
                case 12:
                    data = "C";
                    break;
                case 13:
                    data = "D";
                    break;
                case 14:
                    data = "E";
                    break;
                case 15:
                    data = "F";
                    break;
            }
        }
        else
        {
            switch (c)
            {
                case 0:
                    data = "0";
                    break;
                case 1:
                    data = "1";
                    break;
                case 2:
                    data = "2";
                    break;
                case 3:
                    data = "3";
                    break;
                case 4:
                    data = "4";
                    break;
                case 5:
                    data = "5";
                    break;
                case 6:
                    data = "6";
                    break;
                case 7:
                    data = "7";
                    break;
                case 8:
                    data = "8";
                    break;
                case 9:
                    data = "9";
                    break;

            }

        }
        return data;
    }
    private static int parse(char c)
    {
        if (c >= 'a')
            return (c - 'a' + 10) & 0x0f;
        if (c >= 'A')
            return (c - 'A' + 10) & 0x0f;
        return (c - '0') & 0x0f;
    }
    // Convert from hexadecimal string to byte array
    public static byte[] HexString2Btyes(String hexstr)
    {
        // Convert string to character array
        char[] charArray = hexstr.toCharArray();
        byte[] b = new byte[hexstr.length() / 2];
        int j = 0;
        for (int i = 0; i < b.length; i++)
        {
            char c0 = charArray[j++];
            char c1 = charArray[j++];
            b[i] = (byte)((parse(c0) << 4) | parse(c1));

        }
        return b;
    }




}