package com.example.permissiondemo;

import java.util.List;

import android.Manifest;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.permissiondemo.libs.AfterPermissionGranted;
import com.example.permissiondemo.libs.EasyPermissions;

public class MyFragment extends Fragment implements EasyPermissions.PermissionCallbacks {
	private static final String TAG = "MainFragment";
	private static final int RC_SMS_PERM = 122;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Create view
		View v = inflater.inflate(R.layout.fragment_main, container, false);
		// Button click listener
		v.findViewById(R.id.button_sms).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				smsTask();
			}
		});

		return v;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
//		 在app设置界面返回后可以做一些事情
		if (requestCode == EasyPermissions.SETTINGS_REQ_CODE) {
			//是否获取到了 Sms 权限
			boolean hasReadSmsPermission = EasyPermissions.hasPermissions(getContext(), Manifest.permission.READ_SMS);
			String hasReadSmsPermissionText = getString(R.string.has_read_sms_permission, hasReadSmsPermission);
			Toast.makeText(getContext(), hasReadSmsPermissionText, Toast.LENGTH_SHORT).show();
		}
	}
	
	//不论用户同意还是拒绝， onRequestPermissionsResult 函数会被回调回来通知结果（通过第三个参数）
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // 检查是否需要弹出请求权限的提示对话框
		EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
	}

	@AfterPermissionGranted(RC_SMS_PERM)
	private void smsTask() {
		if (EasyPermissions.hasPermissions(getContext(), Manifest.permission.READ_SMS)) {
			// Have permission, do the thing!
			Toast.makeText(getActivity(), "TODO: SMS things", Toast.LENGTH_LONG).show();
		} else {
			// Request one permission
			EasyPermissions.requestPermissions(this, getString(R.string.rationale_sms), RC_SMS_PERM, Manifest.permission.READ_SMS);
		}
	}

	//获得权限
	@Override
	public void onPermissionsGranted(int requestCode, List<String> perms) {
		Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
	}

	//权限被拒绝
	@Override
	public void onPermissionsDenied(int requestCode, List<String> perms) {
		Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

		// Handle negative button on click listener
		DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Let's show a toast
				Toast.makeText(getContext(), R.string.settings_dialog_canceled, Toast.LENGTH_SHORT).show();
			}
		};

		//检查用户是否拒绝了权限，并且检查永不再询问。这将显示一个对话框，在app中设置这个权限。
		EasyPermissions.checkDeniedPermissionsNeverAskAgain(this, getString(R.string.rationale_ask_again), R.string.setting, R.string.cancel, onClickListener, perms);
	}
}
