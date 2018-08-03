package com.square.renov.swipevoicechat.Util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.WindowManager;

import com.afollestad.materialdialogs.MaterialDialog;

public class DialogUtils {
    public static void initDialogView(MaterialDialog reportDialog, Context context) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        Point deviceSize = Utils.getDisplaySize(context);
        layoutParams.width = (deviceSize.x * 2) / 3;
        layoutParams.height = Utils.dpToPx(280);
        layoutParams.dimAmount = 0.65f;
        reportDialog.getWindow().setAttributes(layoutParams);
        reportDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        reportDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }
}
