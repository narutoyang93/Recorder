package com.naruto.recorder;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * @Purpose
 * @Author Naruto Yang
 * @CreateDate 2020/2/13 0013
 * @Note
 */
public class MyTool {
    /**
     * 检查并申请权限
     *
     * @param activity
     * @param permissionsRequestCode
     * @param permissions
     * @return 是否已经授权，无需申请
     */
    public static boolean checkPermissions(Activity activity, int permissionsRequestCode, String[] permissions) {
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }

        List<String> requestPermissionsList = new ArrayList<>();
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(activity, p) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionsList.add(p);
            }
        }
        if (!requestPermissionsList.isEmpty()) {
            String[] requestPermissionsArray = requestPermissionsList.toArray(new String[requestPermissionsList.size()]);
            ActivityCompat.requestPermissions(activity, requestPermissionsArray, permissionsRequestCode);
            return false;
        } else {
            return true;
        }
    }

    /**
     * 权限申请回调
     *
     * @param grantResults
     */
    public static void permissionRequestCallBack(int[] grantResults, OperationInterface... callBacks) {
        int positionDenied = -1;
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                positionDenied = i;
                break;
            }
        }
        if (callBacks != null && callBacks.length > 0) {
            if (positionDenied == -1) {//全部通过
                callBacks[0].done(null);
            } else if (callBacks.length > 1) {
                callBacks[1].done(positionDenied);
            }
        }
    }

    /**
     * @Purpose
     * @Author Naruto Yang
     * @CreateDate 2020/2/13 0013
     * @Note
     */
    public interface OperationInterface {
        void done(Object o);
    }
}
