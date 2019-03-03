package cn.devld.Realtime_Load_Monitor_Controller;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final int R_DISCOVERY_DEVICE = 0xf;

    public static final UUID DEVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothDevice mDevice;
    private String mDeviceName = "Unknown Device";

    private BTHelper mBTHelper;

    private ScrollView mScrollView;
    private TextView tv_log, txtCounter, txtFirstCounter, txtSecondCounter, txtAboutTheApp, txtTestAmp, txtTestAmp2, txtTestAmp3,
    txtTestAmp4, txtAmp, txtWatt, txtAmp2, txtWatt2;
    private EditText ev_cmd, edtFunds, edtFunds2, edtTimer1, edtTimer2;
    private Spinner sp_br;
    private Button btnTimer1, btnTimer2;
    private Button btn_send;
    private Button btn_Off;
    private Button btn_On2, btn_Off2, btnFundsThreshold, btnFundsThreshold2;
    private ImageView lightBulb1, lightBulb2;

    public static final int WHAT_CONNECT = 0;
    public static final int WHAT_ERROR = 1;
    public static final int WHAT_RECV = 2;

    private Double fundsThreshold, fundsThreshold2;

    public ArrayList<Double> KWHs = new ArrayList<Double>();
    public ArrayList<Double> KWHs2 = new ArrayList<Double>();


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_CONNECT:
                    boolean suc = (boolean) msg.obj;
                    if (suc) {
                        btn_send.setEnabled(true);
                        msg("Connected to " + mDeviceName + ".\n---------------");
                    } else {
                        msg("Can't connect to " + mDeviceName + ".");
                    }
                    break;
                case WHAT_ERROR:
                    msg("Lost connection.");
                    break;
                case WHAT_RECV:
                    String myMessage = (String)msg.obj;
                    if (myMessage.equals("1"))
                        myMessage = "Switching light on!";
                    writeSomeStuff(myMessage);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("Monitoring & Controlling System");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        startActivityForResult(new Intent(this, DevicesDiscoveryActivity.class), R_DISCOVERY_DEVICE);

    }

    private void initView() {
       //Countdown timer variables.
        txtAmp = (TextView) findViewById(R.id.txtAmp);
        txtAmp2 = (TextView) findViewById(R.id.txtAmp2);
        txtWatt = (TextView) findViewById(R.id.txtWatt);
        txtWatt2 = (TextView) findViewById(R.id.txtWatt2);
        txtFirstCounter = (TextView) findViewById(R.id.txtFirstCounter);
        txtSecondCounter = (TextView) findViewById(R.id.txtSecondCounter);
        txtAboutTheApp = (TextView) findViewById(R.id.txtAboutTheApp);
        btnTimer1 = (Button) findViewById(R.id.btnTimer1);
        btnTimer2= (Button) findViewById(R.id.btnTimer2);
        edtTimer1 = (EditText) findViewById(R.id.edtTimer1);
        edtTimer2 = (EditText) findViewById(R.id.edtTimer2);
        lightBulb1 = (ImageView) findViewById(R.id.lightBulb1);
        lightBulb2 = (ImageView) findViewById(R.id.lightBulb2);

        mScrollView = (ScrollView) findViewById(R.id.main_scrollview);
        tv_log = (TextView) findViewById(R.id.main_logview);
        //txtCounter = (TextView) findViewById(R.id.txtCounter);
        ev_cmd = (EditText) findViewById(R.id.main_cmdview);
        sp_br = (Spinner) findViewById(R.id.sp_br);
        btn_send = (Button) findViewById(R.id.main_send_btn);
        btn_Off = (Button) findViewById(R.id.btn_off);
        btn_On2 = (Button) findViewById(R.id.btn_On2);
        btn_Off2 = (Button) findViewById(R.id.btn_Off2);
        btnFundsThreshold = (Button) findViewById(R.id.btn_fundsThreshold);
        btnFundsThreshold2 = (Button) findViewById(R.id.btn_fundsThreshold2);

        edtFunds = (EditText) findViewById(R.id.edtFunds);
        edtFunds2 = (EditText) findViewById(R.id.edtFunds2);

        btnTimer1.setEnabled(false);
        btnTimer2.setEnabled(false);
        lightBulb1.setColorFilter(getApplicationContext().getResources().getColor(R.color.shutDownColor));
        lightBulb2.setColorFilter(getApplicationContext().getResources().getColor(R.color.shutDownColor));
        btn_send.setEnabled(true);
        btn_Off.setEnabled(false);
        btn_On2.setEnabled(true);
        btn_Off2.setEnabled(false);
        btnFundsThreshold.setEnabled(false);
        btnFundsThreshold2.setEnabled(false);
        fundsThreshold = 1e9; fundsThreshold2 = 1e9;

       txtAboutTheApp.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Intent intent = new Intent(MainActivity.this, AbuAwadActivity.class);
               startActivity(intent);
           }
       });
        btnFundsThreshold.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                try{fundsThreshold = Double.parseDouble(edtFunds.getText().toString());}
                catch(Exception e) {
                    fundsThreshold = 1e9;
                }
               // Toast.makeText(getApplicationContext(), "Load will be shut down once the entered amount is reached " + fundsThreshold, Toast.LENGTH_LONG);

            }
        });
        btnFundsThreshold2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                try{fundsThreshold2 = Double.parseDouble(edtFunds2.getText().toString());}
                catch (Exception e) {
                    fundsThreshold2 = 1e9;
                }

               // Toast.makeText(getApplicationContext(), "Load will be shut down once the entered amount is reached " + fundsThreshold, Toast.LENGTH_LONG);

            }
        });
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String data = ev_cmd.getText().toString();
                String data = "1";
                ev_cmd.setText("");
                String br = "\n";
               /* switch (sp_br.getSelectedItemPosition()) {
                    case 0:
                        br = "\n";
                        break;
                    case 1:
                        br = "";
                        break;
                    case 2:
                        br = "\r\n";
                        break;
                }*/
                mBTHelper.send((data).getBytes());
                btn_Off.setEnabled(true);
                btn_send.setEnabled(false);
                lightBulb1.setColorFilter(getApplicationContext().getResources().getColor(R.color.turnOnColor));
                btnTimer1.setEnabled(true);
                btnFundsThreshold.setEnabled(true);
            }
        });
        btn_Off.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                    String data = "0";
                    mBTHelper.send((data).getBytes());
                    btn_send.setEnabled(true);
                    btn_Off.setEnabled(false);
                    lightBulb1.setColorFilter(getApplicationContext().getResources().getColor(R.color.shutDownColor));
                    btnTimer1.setEnabled(false);
                    btnFundsThreshold.setEnabled(false);


            }
        });

        btn_On2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String data = "3";
                mBTHelper.send((data).getBytes());
                btn_On2.setEnabled(false);
                btn_Off2.setEnabled(true);
                lightBulb2.setColorFilter(getApplicationContext().getResources().getColor(R.color.turnOnColor));
                btnTimer2.setEnabled(true);
                btnFundsThreshold2.setEnabled(true);


            }
        });

        btn_Off2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String data = "4";
                mBTHelper.send((data).getBytes());
                btn_On2.setEnabled(true);
                btn_Off2.setEnabled(false);
                lightBulb2.setColorFilter(getApplicationContext().getResources().getColor(R.color.shutDownColor));
                btnTimer2.setEnabled(false);
                btnFundsThreshold2.setEnabled(false);


            }
        });

        btnTimer1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // do some stuff here for the first button
                int firstNumber = -1;

                try {
                    firstNumber = Integer.parseInt(edtTimer1.getText().toString()) * 1000;
                }
                catch (Exception e){
                    Toast.makeText(getApplicationContext(), "Please insert a value", Toast.LENGTH_LONG);
                }
                edtTimer1.setText("");
                if(firstNumber != -1) {
                    new CountDownTimer(firstNumber, 1000) {

                        public void onTick(long millisUntilFinished) {
                            txtFirstCounter.setText("seconds remaining: " + millisUntilFinished / 1000);
                            //here you can have your logic to set text to edittext
                        }

                        public void onFinish() {
                            //mTextField.setText("done!");
                            mBTHelper.send(("0").getBytes());
                            btn_send.setEnabled(true);
                            btn_Off.setEnabled(false);
                            txtFirstCounter.setText("Done!");
                            lightBulb1.setColorFilter(getApplicationContext().getResources().getColor(R.color.shutDownColor));
                            btnTimer1.setEnabled(false);
                            btnFundsThreshold.setEnabled(false);
                        }

                    }.start();
                }
            }
        });
        btnTimer2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int secondNumber = -1;
                try{
                    secondNumber = Integer.parseInt(edtTimer2.getText().toString()) * 1000;
                }
                catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Please insert a value", Toast.LENGTH_LONG);

                }
                edtTimer2.setText("");
                // do some stuff here for the second button
                if(secondNumber != -1) {
                    new CountDownTimer(secondNumber, 1000) {

                        public void onTick(long millisUntilFinished) {
                            txtSecondCounter.setText("seconds remaining: " + millisUntilFinished / 1000);
                            //here you can have your logic to set text to edittext
                        }

                        public void onFinish() {
                            //mTextField.setText("done!");
                            mBTHelper.send(("4").getBytes());
                            btn_On2.setEnabled(true);
                            btn_Off2.setEnabled(false);
                            txtSecondCounter.setText("Done!");
                            lightBulb2.setColorFilter(getApplicationContext().getResources().getColor(R.color.shutDownColor));
                            btnTimer2.setEnabled(false);
                            btnFundsThreshold2.setEnabled(false);
                        }

                    }.start();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case R_DISCOVERY_DEVICE:
                if (resultCode == RESULT_OK) {
                    BluetoothDevice device = data.getParcelableExtra(DevicesDiscoveryActivity.EXTRA_DEVICE);
                    mDevice = device;
                    if (mDevice.getName() != null) {
                        mDeviceName = mDevice.getName();
                    }
                    msg("Device Name: " + device.getName() + " Address: " + device.getAddress());
                    mBTHelper = new BTHelper(mDevice, new BTHelper.BTListener() {
                        @Override
                        public void onConnect(boolean success) {
                            Message msg = mHandler.obtainMessage();
                            msg.what = WHAT_CONNECT;
                            msg.obj = success;
                            mHandler.sendMessage(msg);
                        }

                        @Override
                        public void onDataReceived(String data) {
                            Message msg = mHandler.obtainMessage();
                            msg.what = WHAT_RECV;
                            msg.obj = data;
                            mHandler.sendMessage(msg);
                        }

                        @Override
                        public void onError() {
                            mHandler.sendEmptyMessage(WHAT_ERROR);
                        }
                    });
                    msg("Connecting to " + mDeviceName + ".");
                    mBTHelper.connect(DEVICE_UUID);
                } else {
                    msg("No device selected.");
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBTHelper != null) {
            mBTHelper.disconnect();
        }
    }

    public void msg(String msg) {
        tv_log.append(msg + "\n");
        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }

    private int count = 0;
   public void writeSomeStuff(String data) {
       TextView txtKWH = (TextView) findViewById(R.id.txtKWH1);
       TextView txtKWH2 = (TextView) findViewById(R.id.txtKWH2);
       TextView txtCost = (TextView) findViewById(R.id.txtCost);
       TextView txtCost2 = (TextView) findViewById(R.id.txtCost2);
       //this chunk of code sets AMP and Watts
       int myAindex = data.indexOf("A");
       int myWindex = data.indexOf("W");
       int myWhIndex = data.indexOf("W.h");
       int myJDindex1 = data.indexOf("JD");
       DecimalFormat df = new DecimalFormat("#.####");
       try {
           String A, W, wh, firstCost, secondString, A2, W2, wh2, secondCost;
           // first guy's data.
           A = data.substring(0, myAindex);
           W = data.substring(myAindex+2, myWindex);
           wh = data.substring(myWindex + 2, myWhIndex);
           firstCost = data.substring(myWhIndex+4, myJDindex1);
           txtAmp.setText(A+"A");
           txtWatt.setText(W+"W");
           txtKWH.setText(wh+"W⋅h");
           txtCost.setText("(فلس)"+firstCost);
           //done with first guy
           secondString = data.substring(myJDindex1+3, data.length()); //defining second guy
           //redefining indices
           myAindex = secondString.indexOf("A");
           myWindex = secondString.indexOf("W");
           myWhIndex = secondString.indexOf("W.h");
           myJDindex1 = secondString.indexOf("JD");
           //done redefining indices
           //second guy's data
           A2 = secondString.substring(0, myAindex);
           W2= secondString.substring(myAindex+2, myWindex);
           wh2 = secondString.substring(myWindex + 2, myWhIndex);
           secondCost = secondString.substring(myWhIndex+4, myJDindex1);
           txtAmp2.setText(A2+"A");
           txtWatt2.setText(W2+"W");
           txtKWH2.setText(wh2+"W⋅h");
           txtCost2.setText("(فلس)"+secondCost);
            //done with second guy


           Double myCost = Double.parseDouble(firstCost);
           Double myCost2 = Double.parseDouble(secondCost);
          if(myCost >= fundsThreshold){
               mBTHelper.send(("0").getBytes());
               btn_send.setEnabled(true);
               btn_Off.setEnabled(false);
               fundsThreshold = 1e9;
               edtFunds.setText("");
               btnTimer1.setEnabled(false);
              lightBulb1.setColorFilter(getApplicationContext().getResources().getColor(R.color.shutDownColor));
              btnFundsThreshold.setEnabled(false);
          }
           if(myCost2 >= fundsThreshold2){
               mBTHelper.send(("4").getBytes());
               btn_On2.setEnabled(true);
               btn_Off2.setEnabled(false);
               fundsThreshold2 = 1e9;
               edtFunds2.setText("");
               btnTimer2.setEnabled(false);
               lightBulb2.setColorFilter(getApplicationContext().getResources().getColor(R.color.shutDownColor));
               btnFundsThreshold2.setEnabled(false);
           }
       }
       catch (Exception e) {
           Toast.makeText(getApplicationContext(),"it happened here", Toast.LENGTH_LONG);
           txtAmp.setText("Waiting for Signal");
           txtAmp2.setText("Waiting for Signal");
       }
               // this chunk of code sets data for cost and KWH.


    }


}
