package com.example.permissiondemo;

import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.example.permissiondemo.libs.AfterPermissionGranted;
import com.example.permissiondemo.libs.EasyPermissions;

/**
 * @see 必须要实现，这个接口 EasyPermissions.PermissionCallbacks
 * 重写<br>  1 onPermissionsGranted <br>2 onPermissionsDenied 方法
 * @author lixiang
 *
 */
public class MainActivity extends Activity implements OnClickListener ,EasyPermissions.PermissionCallbacks{

	private Button camera, phone, sms, location_and_contacts;
	private static final int RC_CAMERA_PERM = 123;
	private static final int RC_LOCATION_CONTACTS_PERM = 124;
	private static final int RC_PHONE_PERM = 125;
	private static final int RC_SMS_PERM = 126;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initViews();
	}

	private void initViews() {
		camera = (Button) findViewById(R.id.bt_camera);
		camera.setOnClickListener(this);
		phone = (Button) findViewById(R.id.bt_phone);
		phone.setOnClickListener(this);
		sms = (Button) findViewById(R.id.bt_sms);
		sms.setOnClickListener(this);
		location_and_contacts = (Button) findViewById(R.id.bt_location_and_contacts);
		location_and_contacts.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_camera:
			cameraTask();
//			FragmentTransaction ft = getFragmentManager().beginTransaction();
//			ft.replace(R.id.mFrameLayout, new MyFragment());
//			ft.commit();
			break;
		case R.id.bt_phone:
			phoneTask();
			break;
		case R.id.bt_sms:
			smsTask();
			break;
		case R.id.bt_location_and_contacts:
			locationAndContactsTask();
			break;

		default:
			break;
		}
	}

	/**
	 * 示例1 请求多个权限
	 */
	@AfterPermissionGranted(RC_LOCATION_CONTACTS_PERM)
	private void locationAndContactsTask() {
		String[] perms = { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_CONTACTS };
		//检查是否有权限
		if (EasyPermissions.hasPermissions(this, perms)) {
			// 有权限,做权限相关的事情!
			Toast.makeText(this, "TODO: Location and Contacts things", Toast.LENGTH_LONG).show();
		} else {
			// 请求2或多个权限
			EasyPermissions.requestPermissions(this, getString(R.string.rationale_location_contacts), RC_LOCATION_CONTACTS_PERM, perms);
		}
	}

	/**
	 * 示例2 请求 1 个权限
	 */
	
	@AfterPermissionGranted(RC_SMS_PERM)
	private void smsTask() {
		//检查是否有权限
        if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_SMS)) {
			// 有权限,做权限相关的事情!
            Toast.makeText(this, "TODO: SMS things", Toast.LENGTH_LONG).show();
        } else {
			// 请求1个权限
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_sms), RC_SMS_PERM, Manifest.permission.READ_SMS);
        }
	}

	@AfterPermissionGranted(RC_PHONE_PERM)
	private void phoneTask() {
		if (EasyPermissions.hasPermissions(this, Manifest.permission.CALL_PHONE)) {
            // Have permission, do the thing!
            Toast.makeText(this, "TODO: Phone things", Toast.LENGTH_LONG).show();
	        Intent intent = new Intent();
	        intent.setAction("android.intent.action.CALL");
	        intent.setData(Uri.parse("tel:" + 10086));
	        startActivity(intent);
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_camera), RC_PHONE_PERM, Manifest.permission.CALL_PHONE);
        }
	}

	@AfterPermissionGranted(RC_CAMERA_PERM)
	private void cameraTask() {
		if (EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {
            // Have permission, do the thing!
            Toast.makeText(this, "TODO: Camera things", Toast.LENGTH_LONG).show();
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_camera), RC_CAMERA_PERM, Manifest.permission.CAMERA);
        }
	}
	
	//不论用户同意还是拒绝， onRequestPermissionsResult 函数会被回调回来通知结果（通过第三个参数）
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // 请求权限结果，并回调注解方法。其次会检查是否需要弹出，系统请求权限的提示对话框
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
    
    
    
    /**
     * 1 权限通过
     * <br>
     * 通过switch 来判断，requestCode或 perms，做相应的请求
     * <br>
     * @param 请求权限的code
     * @param 请求权限参数
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
    	System.out.println("onPermissionsGranted:" + requestCode + ":" + perms.size());
    }
    /**
     * 2 权限被拒绝
     * <br>
     * 通过switch 来判断，requestCode或 perms，做相应的请求
     * <br>
     * @param 请求权限的code
     * @param 请求权限参数
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
    	System.out.println("onPermissionsDenied:" + requestCode + ":" + perms.size());
		// （可选）检查用户是否拒绝权限，并检查永不再询问。
		// 这将显示一个对话框，指导他们启用应用程序设置的权限。
        EasyPermissions.checkDeniedPermissionsNeverAskAgain(this, getString(R.string.rationale_ask_again), R.string.setting, R.string.cancel, null, perms);
    }
    

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EasyPermissions.SETTINGS_REQ_CODE) {
            // 用户从应用程序设置屏幕返回,好像没有太大作用
            Toast.makeText(this, R.string.returned_from_app_settings_to_activity, Toast.LENGTH_SHORT).show();
        }
    }
}
