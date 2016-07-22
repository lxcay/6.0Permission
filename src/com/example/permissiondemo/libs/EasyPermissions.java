/*
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.permissiondemo.libs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Utility to request and check System permissions for apps targeting Android M
 * (API >= 23).
 */
/**
 * Utility to request and check System permissions for apps targeting Android M
 * (API >= 23).
 */
public class EasyPermissions {

	public static final int SETTINGS_REQ_CODE = 16061;

	private static final String TAG = "EasyPermissions";

	/**
	 * 权限修改回调 接口
	 * @author lixiang
	 *
	 */
	public interface PermissionCallbacks {

		void onPermissionsGranted(int requestCode, List<String> perms);

		void onPermissionsDenied(int requestCode, List<String> perms);

	}

	/**
	 * 检查调用的上下文中，是否有一个权限
	 * 
	 * @param context
	 *            上下文.
	 * @param perms
	 *            一个或多个权限参数 such as
	 *            <br>
	 *            {@code android.Manifest.permission.CAMERA}.
	 * @return 如果所有权限都获取到了 true ，如果一个权限都没有获取到 false.
	 */
	public static boolean hasPermissions(Context context, String... perms) {
		// 如果SDK < M，默认，总是返回 true,交给系统去处理
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			Log.w(TAG, "hasPermissions: API version < M, returning true by default");
			return true;
		}

		for (String perm : perms) {
			//PERMISSION_GRANTED 已授权，PERMISSION_DENIED 没授权
			boolean hasPerm = (context.checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED);
			if (!hasPerm) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Request a set of permissions, showing rationale if the system requests
	 * it.
	 * 
	 * @param object
	 *            Activity or Fragment requesting permissions. Should implement
	 *            {@link android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback}
	 *            or
	 *            {@link android.support.v13.app.FragmentCompat.OnRequestPermissionsResultCallback}
	 * @param rationale
	 *            a message explaining why the application needs this set of
	 *            permissions, will be displayed if the user rejects the request
	 *            the first time.
	 * @param requestCode
	 *            request code to track this request, must be < 256.
	 * @param perms
	 *            a set of permissions to be requested.
	 */
	public static void requestPermissions(final Object object, String rationale, final int requestCode, final String... perms) {
		requestPermissions(object, rationale, android.R.string.ok, android.R.string.cancel, requestCode, perms);
	}

	/**
	 * Request a set of permissions, showing rationale if the system requests
	 * it.
	 * 
	 * @param object
	 *            Activity or Fragment requesting permissions. Should implement
	 *            {@link android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback}
	 *            or
	 *            {@link android.support.v13.app.FragmentCompat.OnRequestPermissionsResultCallback}
	 * @param rationale
	 *            a message explaining why the application needs this set of
	 *            permissions, will be displayed if the user rejects the request
	 *            the first time.
	 * @param positiveButton
	 *            custom text for positive button
	 * @param negativeButton
	 *            custom text for negative button
	 * @param requestCode
	 *            request code to track this request, must be < 256.
	 * @param perms
	 *            a set of permissions to be requested.
	 */
	public static void requestPermissions(final Object object, String rationale, @StringRes int positiveButton, @StringRes int negativeButton, final int requestCode,
			final String... perms) {

		checkCallingObjectSuitability(object);

		boolean shouldShowRationale = false;
		for (String perm : perms) {
			shouldShowRationale = shouldShowRationale || shouldShowRequestPermissionRationale(object, perm);
		}

		//这段好像永远执行不到，不过可以把条件改为否，但是这样仅仅只是告诉用户，你要获取某某某权限了
		if (shouldShowRationale) {
			Activity activity = getActivity(object);
			if (null == activity) {
				return;
			}

			AlertDialog dialog = new AlertDialog.Builder(activity).setMessage(rationale).setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					executePermissionsRequest(object, perms, requestCode);
				}
			}).setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// act as if the permissions were denied
					if (object instanceof PermissionCallbacks) {
						((PermissionCallbacks) object).onPermissionsDenied(requestCode, Arrays.asList(perms));
					}
				}
			}).create();
			dialog.show();
		} else {
			executePermissionsRequest(object, perms, requestCode);
		}
	}

	/**
	 * Handle the result of a permission request, should be called from the
	 * calling Activity's
	 * {@link android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback#onRequestPermissionsResult(int, String[], int[])}
	 * method.
	 * <p>
	 * If any permissions were granted or denied, the Activity will receive the
	 * appropriate callbacks through {@link PermissionCallbacks} and methods
	 * annotated with {@link AfterPermissionGranted} will be run if appropriate.
	 * 
	 * @param requestCode
	 *            requestCode argument to permission result callback.
	 * @param permissions
	 *            permissions argument to permission result callback.
	 * @param grantResults
	 *            grantResults argument to permission result callback.
	 * @param object
	 *            the calling Activity or Fragment.
	 * @throws IllegalArgumentException
	 *             if the calling Activity does not implement
	 *             {@link PermissionCallbacks}.
	 */
	public static void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults, Object object) {

		checkCallingObjectSuitability(object);

		// Make a collection of granted and denied permissions from the request.
		ArrayList<String> granted = new ArrayList<>();
		ArrayList<String> denied = new ArrayList<>();
		for (int i = 0; i < permissions.length; i++) {
			String perm = permissions[i];
			if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
				granted.add(perm);
			} else {
				denied.add(perm);
			}
		}

		// Report granted permissions, if any.
		if (!granted.isEmpty()) {
			// Notify callbacks
			if (object instanceof PermissionCallbacks) {
				((PermissionCallbacks) object).onPermissionsGranted(requestCode, granted);
			}
		}

		// Report denied permissions, if any.
		if (!denied.isEmpty()) {
			if (object instanceof PermissionCallbacks) {
				((PermissionCallbacks) object).onPermissionsDenied(requestCode, denied);
			}
		}

		// 如果权限通过，并且没有拒绝的权限，调用注解方法
		if (!granted.isEmpty() && denied.isEmpty()) {
			runAnnotatedMethods(object, requestCode);
		}
	}

	/**
	 * Calls
	 * {@link #checkDeniedPermissionsNeverAskAgain(Object, String, int, int, DialogInterface.OnClickListener, List)}
	 * with a {@code null} argument for the negatieb buttonOnClickListener.
	 */
	public static boolean checkDeniedPermissionsNeverAskAgain(final Object object, String rationale, @StringRes int positiveButton, @StringRes int negativeButton,
			List<String> deniedPerms) {
		return checkDeniedPermissionsNeverAskAgain(object, rationale, positiveButton, negativeButton, null, deniedPerms);
	}

	/**
	 * If user denied permissions with the flag NEVER ASK AGAIN, open a dialog
	 * explaining the permissions rationale again and directing the user to the
	 * app settings. After the user returned to the app,
	 * {@link Activity#onActivityResult(int, int, Intent)} or
	 * {@link Fragment#onActivityResult(int, int, Intent)} or
	 * {@link android.app.Fragment#onActivityResult(int, int, Intent)} will be
	 * called with {@value #SETTINGS_REQ_CODE} as requestCode
	 * <p>
	 * NOTE: use of this method is optional, should be called from
	 * {@link PermissionCallbacks#onPermissionsDenied(int, List)}
	 * 
	 * @param object
	 *            the calling Activity or Fragment.
	 * @param deniedPerms
	 *            the set of denied permissions.
	 * @param negativeButtonOnClickListener
	 *            negative button on click listener. If the user click the
	 *            negative button, then this listener will be called. Pass null
	 *            if you don't want to handle it.
	 * @return {@code true} if user denied at least one permission with the flag
	 *         NEVER ASK AGAIN.
	 */
	public static boolean checkDeniedPermissionsNeverAskAgain(final Object object, String rationale, @StringRes int positiveButton, @StringRes int negativeButton,
			@Nullable DialogInterface.OnClickListener negativeButtonOnClickListener, List<String> deniedPerms) {
		boolean shouldShowRationale;
		for (String perm : deniedPerms) {
			shouldShowRationale = shouldShowRequestPermissionRationale(object, perm);
			if (!shouldShowRationale) {
				final Activity activity = getActivity(object);
				if (null == activity) {
					return true;
				}

				AlertDialog dialog = new AlertDialog.Builder(activity).setMessage(rationale).setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
						Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
						intent.setData(uri);
						//打开系统的设置界面
						startAppSettingsScreen(object, intent);
					}
				}).setNegativeButton(negativeButton, negativeButtonOnClickListener).create();
				dialog.show();

				return true;
			}
		}

		return false;
	}

	@TargetApi(23)
	private static boolean shouldShowRequestPermissionRationale(Object object, String perm) {
		if (object instanceof Activity) {
			return ((Activity) object).shouldShowRequestPermissionRationale(perm);
		} else if (object instanceof Fragment) {
			return ((Fragment) object).shouldShowRequestPermissionRationale(perm);
		} else if (object instanceof android.app.Fragment) {
			return ((android.app.Fragment) object).shouldShowRequestPermissionRationale(perm);
		} else {
			return false;
		}
	}

	@TargetApi(23)
	private static void executePermissionsRequest(Object object, String[] perms, int requestCode) {
		checkCallingObjectSuitability(object);

		if (object instanceof Activity) {
			((Activity) object).requestPermissions(perms, requestCode);
		} else if (object instanceof Fragment) {
			((Fragment) object).requestPermissions(perms, requestCode);
		} else if (object instanceof android.app.Fragment) {
			((android.app.Fragment) object).requestPermissions(perms, requestCode);
		}
	}

	@TargetApi(11)
	private static Activity getActivity(Object object) {
		if (object instanceof Activity) {
			return ((Activity) object);
		} else if (object instanceof Fragment) {
			return ((Fragment) object).getActivity();
		} else if (object instanceof android.app.Fragment) {
			return ((android.app.Fragment) object).getActivity();
		} else {
			return null;
		}
	}

	@TargetApi(11)
	private static void startAppSettingsScreen(Object object, Intent intent) {
		if (object instanceof Activity) {
			((Activity) object).startActivityForResult(intent, SETTINGS_REQ_CODE);
		} else if (object instanceof Fragment) {
			((Fragment) object).startActivityForResult(intent, SETTINGS_REQ_CODE);
		} else if (object instanceof android.app.Fragment) {
			((android.app.Fragment) object).startActivityForResult(intent, SETTINGS_REQ_CODE);
		}
	}

	private static void runAnnotatedMethods(Object object, int requestCode) {
		Class<?> clazz = object.getClass();
		if (isUsingAndroidAnnotations(object)) {
			clazz = clazz.getSuperclass();
		}
		for (Method method : clazz.getDeclaredMethods()) {
			if (method.isAnnotationPresent(AfterPermissionGranted.class)) {
				// Check for annotated methods with matching request code.
				AfterPermissionGranted ann = method.getAnnotation(AfterPermissionGranted.class);
				if (ann.value() == requestCode) {
					// Method must be void so that we can invoke it
					if (method.getParameterTypes().length > 0) {
						throw new RuntimeException("Cannot execute non-void method " + method.getName());
					}

					try {
						// Make method accessible if private
						if (!method.isAccessible()) {
							method.setAccessible(true);
						}
						method.invoke(object);
					} catch (IllegalAccessException e) {
						Log.e(TAG, "runDefaultMethod:IllegalAccessException", e);
					} catch (InvocationTargetException e) {
						Log.e(TAG, "runDefaultMethod:InvocationTargetException", e);
					}
				}
			}
		}
	}

	// 确保对象一个Activity，或者Fragment
	private static void checkCallingObjectSuitability(Object object) {
		boolean isActivity = object instanceof Activity;
		boolean isSupportFragment = object instanceof Fragment;
		boolean isAppFragment = object instanceof android.app.Fragment;
		boolean isMinSdkM = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

		if (!(isSupportFragment || isActivity || (isAppFragment && isMinSdkM))) {
			if (isAppFragment) {
				throw new IllegalArgumentException("Target SDK needs to be greater than 23 if caller is android.app.Fragment");
			} else {
				throw new IllegalArgumentException("dyongzhe an Activity or a Fragment.");
			}
		}
	}

	private static boolean isUsingAndroidAnnotations(Object object) {
		if (!object.getClass().getSimpleName().endsWith("_")) {
			return false;
		}

		try {
			Class<?> clazz = Class.forName("org.androidannotations.api.view.HasViews");
			return clazz.isInstance(object);
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
}
