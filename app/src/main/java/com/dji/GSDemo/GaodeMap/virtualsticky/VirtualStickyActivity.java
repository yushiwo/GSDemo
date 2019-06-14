package com.dji.GSDemo.GaodeMap.virtualsticky;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dji.GSDemo.GaodeMap.DJIDemoApplication;
import com.dji.GSDemo.GaodeMap.R;
import com.dji.GSDemo.GaodeMap.utils.ToastUtils;

import java.util.Timer;
import java.util.TimerTask;

import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.mission.hotpoint.HotpointMission;
import dji.common.model.LocationCoordinate2D;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.hotpoint.HotpointMissionOperator;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;

import static dji.common.mission.hotpoint.HotpointHeading.ALONG_CIRCLE_LOOKING_FORWARDS;
import static dji.common.mission.hotpoint.HotpointStartPoint.EAST;

/**
 * 基于virtual sticky控制实现
 * 1、通过hotpoint mission指定环绕点和起始半径
 * 2、切换成 virtualStickControl 模式
 * 3、发送 control data，控制螺旋飞行（控制pitch和roll）
 */
public class VirtualStickyActivity extends AppCompatActivity {

    FlightController mFlightController;
    HotpointMissionOperator operator;

    boolean isConnect;

    private Timer mSendVirtualStickDataTimer;
    private SendVirtualStickDataTask mSendVirtualStickDataTask;

    private float mPitch;
    private float mRoll;
    private float mYaw;
    private float mThrottle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_virtual_sticky);

        BaseProduct baseProduct = DJIDemoApplication.getProductInstance();
        operator = DJISDKManager.getInstance().getMissionControl().getHotpointMissionOperator();

        if (baseProduct != null && baseProduct.isConnected()) {
            if (baseProduct instanceof Aircraft) {
                isConnect = true;
                mFlightController = ((Aircraft) baseProduct).getFlightController();
                //设置飞行控制的回调，可以拿到当前无人机的经纬度
                mFlightController.setStateCallback(
                        new FlightControllerState.Callback() {
                            @Override
                            public void onUpdate(FlightControllerState djiFlightControllerCurrentState) {
                                double droneLocationLat = djiFlightControllerCurrentState.getAircraftLocation().getLatitude();
                                double droneLocationLng = djiFlightControllerCurrentState.getAircraftLocation().getLongitude();

                                Log.i("tag", "latitude = " + droneLocationLat + "  longitude" + droneLocationLng);

                            }
                        });

                initFlightController();
            }
        } else {
            ToastUtils.setResultToToast("disConnect");
        }
    }

    private void initFlightController() {
        mFlightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
        mFlightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
        mFlightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
        mFlightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);
    }

    public void onClick(View view) {
        if (!isConnect) {
            ToastUtils.setResultToToast("disConnect");
            return;
        }
        switch (view.getId()) {
            case R.id.hot_take_off:
                takeOff();
                break;
            case R.id.start_hot_point:
                startPoint();
                break;
            case R.id.stop_hot_point:
                stopPoint();
                break;

            case R.id.enable_virtual_stick:
                setVirtualStickyEnable();
                break;
            case R.id.start_fly:
                startFly();
                break;
        }
    }

    public void takeOff() {
        mFlightController.startTakeoff(
                new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError != null) {
                            Toast.makeText(VirtualStickyActivity.this, djiError.getDescription(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(VirtualStickyActivity.this, "Take off Success", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    /**
     * 设置HotpointMission任务时，以下参数必须要设置，不然会报failure错误
     * 设置范围有限定，以下参数都会引起飞行器限制飞行，可以参考文档合理设置范围
     */
    public void startPoint() {
        HotpointMission hotpointMission = new HotpointMission();
        //设置海拔
        hotpointMission.setAltitude(30);
        //设置顺时针方向
        hotpointMission.setClockwise(true);
        //设置角速率
        hotpointMission.setAngularVelocity(5);
        //设置范围
        hotpointMission.setRadius(10);
        hotpointMission.setHeading(ALONG_CIRCLE_LOOKING_FORWARDS);
        //当前热点的经纬度
        hotpointMission.setHotpoint(new LocationCoordinate2D(30.27858, 120.12497));
        //起飞的方向
        hotpointMission.setStartPoint(EAST);

        operator.startMission(hotpointMission, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                    ToastUtils.setResultToToast("Mission start successfully!");
                } else {
                    ToastUtils.setResultToToast("Mission start failed, error: " + djiError.getDescription() + " retrying...");
                }
            }
        });
    }

    public void stopPoint() {
        operator.stop(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                    ToastUtils.setResultToToast("Mission stop successfully!");
                } else {
                    ToastUtils.setResultToToast("Mission stop failed, error: " + djiError.getDescription() + " retrying...");
                }
            }
        });
    }

    /**
     * 启动virtual sticky
     */
    private void setVirtualStickyEnable() {
        mFlightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError != null) {
                    ToastUtils.setResultToToast(djiError.getDescription());
                } else {
                    ToastUtils.setResultToToast("enable Virtual Stick Success");
                }
            }
        });
    }

    private void startFly() {
        if (null == mSendVirtualStickDataTimer) {
            mSendVirtualStickDataTask = new SendVirtualStickDataTask();
            mSendVirtualStickDataTimer = new Timer();
            mSendVirtualStickDataTimer.schedule(mSendVirtualStickDataTask, 0, 200);
        }
    }

    class SendVirtualStickDataTask extends TimerTask {
        @Override
        public void run() {
            if (mFlightController != null) {
                mPitch = 1.0f;
                mRoll = 1.0f;
                mFlightController.sendVirtualStickFlightControlData(
                        new FlightControlData(
                                mPitch, mRoll, mYaw, mThrottle
                        ), new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {

                            }
                        }
                );
            }
        }
    }

}
