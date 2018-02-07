package com.toolslab.quickcode.view.about;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.danielstone.materialaboutlibrary.ConvenienceBuilder;
import com.danielstone.materialaboutlibrary.MaterialAboutActivity;
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem;
import com.danielstone.materialaboutlibrary.items.MaterialAboutItemOnClickAction;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;
import com.toolslab.quickcode.BuildConfig;
import com.toolslab.quickcode.R;
import com.toolslab.quickcode.db.DatabaseReferenceWrapper;
import com.toolslab.quickcode.util.ClipboardUtil;
import com.toolslab.quickcode.util.IOUtils;
import com.toolslab.quickcode.util.log.Tracker;
import com.toolslab.quickcode.view.base.BaseActivity;

import java.io.InputStream;
import java.util.Locale;

import static com.toolslab.quickcode.util.CompatUtil.getDrawableCompat;

public class AboutActivity extends MaterialAboutActivity {

    public static void start(BaseActivity context) {
        Intent intent = new Intent(context, AboutActivity.class);
        context.startActivity(intent);
    }

    @NonNull
    @Override
    protected MaterialAboutList getMaterialAboutList(@NonNull Context context) {
        Context appContext = context.getApplicationContext();
        return new MaterialAboutList.Builder()
                .addCard(createAppTitleCard(appContext))
                .addCard(createAppVersionCard(appContext))
                .addCard(createAppDescriptionCard(appContext))
                .addCard(createAppFeedbackCard(this))
                .addCard(createAppAuthorCard(this))
                .build();
    }

    @Override
    protected CharSequence getActivityTitle() {
        return getString(R.string.menu_about);
    }

    public static String createNavigationFooterTextWithUser() {
        String format = "%1$s%2$s";
        return String.format(Locale.getDefault(), format,
                createNavigationFooterText(),
                DatabaseReferenceWrapper.getUser());
    }

    private static String createNavigationFooterText() {
        String format = "Version: %1$s (%2$s)";
        return String.format(Locale.getDefault(), format,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE);
    }

    private MaterialAboutCard createAppTitleCard(Context context) {
        return new MaterialAboutCard.Builder()
                .addItem(ConvenienceBuilder.createAppTitleItem(context)
                        .setDesc(getString(R.string.about_title_description)))
                .build();
    }

    private MaterialAboutCard createAppVersionCard(final Context context) {
        final View view = findViewById(com.danielstone.materialaboutlibrary.R.id.mal_recyclerview);
        final MaterialAboutActionItem item = ConvenienceBuilder
                .createVersionActionItem(context, null, getString(R.string.about_version), true)
                .setShouldShowIcon(false)
                .setOnClickAction(new MaterialAboutItemOnClickAction() {
                    @Override
                    public void onClick() {
                        Tracker.trackOnClick(context, R.string.about_version_long_tap_to_copy);
                        Snackbar.make(view, R.string.about_version_long_tap_to_copy, Snackbar.LENGTH_SHORT).show();
                    }
                })
                .setOnLongClickAction(new MaterialAboutItemOnClickAction() {
                    @Override
                    public void onClick() {
                        Tracker.trackOnLongClick(context, R.string.message_copied_to_clipboard);
                        if (ClipboardUtil.copyToClipboard(view.getContext(), "Version Text", createNavigationFooterText())) {
                            Snackbar.make(view, R.string.message_copied_to_clipboard, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
        return new MaterialAboutCard.Builder()
                .addItem(item)
                .build();
    }

    private MaterialAboutCard createAppDescriptionCard(Context context) {
        return new MaterialAboutCard.Builder()
                .addItem(new MaterialAboutActionItem.Builder()
                        .showIcon(false)
                        .text(createAppDescriptionText(context))
                        .build())
                .build();
    }

    private MaterialAboutCard createAppFeedbackCard(Context context) {
        return new MaterialAboutCard.Builder()
                .title(R.string.about_feedback_title)
                .addItem(ConvenienceBuilder.createWebsiteActionItem(context,
                        getDrawableCompat(context, R.drawable.ic_comment_black_24dp),
                        getString(R.string.about_give_feedback),
                        false,
                        Uri.parse(getString(R.string.about_feedback_url))))
                .addItem(ConvenienceBuilder.createRateActionItem(context,
                        getDrawableCompat(context, R.drawable.ic_star_black_24dp),
                        getString(R.string.about_rate),
                        null))
                .addItem(ConvenienceBuilder.createWebsiteActionItem(context,
                        getDrawableCompat(context, R.drawable.ic_early_access_black_24dp),
                        getString(R.string.about_early_access),
                        false,
                        Uri.parse(getString(R.string.about_early_access_url))))
                .build();
    }

    private MaterialAboutCard createAppAuthorCard(Context context) {
        return new MaterialAboutCard.Builder()
                .title(R.string.about_author_title)
                .addItem(new MaterialAboutActionItem.Builder()
                        .text(R.string.about_author_name)
                        .icon(R.drawable.ic_person_black_24dp)
                        .build())
                .addItem(new MaterialAboutActionItem.Builder()
                        .text(R.string.about_hosted_on)
                        .icon(R.drawable.ic_code_black_24dp)
                        .setOnClickAction(ConvenienceBuilder.createWebsiteOnClickAction(context, Uri.parse(getString(R.string.about_hosted_url))))
                        .build())
                .build();
    }

    @NonNull
    private String createAppDescriptionText(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.app_description);
        return IOUtils.inputStreamToString(inputStream);
    }

}
