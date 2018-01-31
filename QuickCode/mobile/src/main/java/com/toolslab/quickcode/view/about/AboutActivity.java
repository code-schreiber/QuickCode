package com.toolslab.quickcode.view.about;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.danielstone.materialaboutlibrary.ConvenienceBuilder;
import com.danielstone.materialaboutlibrary.MaterialAboutActivity;
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;
import com.toolslab.quickcode.R;
import com.toolslab.quickcode.view.base.BaseActivity;

public class AboutActivity extends MaterialAboutActivity {

    public static void start(BaseActivity context) {
        Intent intent = new Intent(context, AboutActivity.class);
        context.startActivity(intent);
    }

    @NonNull
    @Override
    protected MaterialAboutList getMaterialAboutList(@NonNull Context context) {
        return new MaterialAboutList.Builder()
                .addCard(createAppTitleCard(context))
                .addCard(createAppDescriptionCard())
                .addCard(createAppFeedbackCard(context))
                .addCard(createAppAuthorCard())
                .build();
    }

    @Override
    protected CharSequence getActivityTitle() {
        return getString(R.string.menu_about);
    }

    private MaterialAboutCard createAppTitleCard(Context context) {
        return new MaterialAboutCard.Builder()
                .addItem(ConvenienceBuilder.createAppTitleItem(context))
                .addItem(ConvenienceBuilder.createVersionActionItem(context, null, getString(R.string.about_version), true))
                .build();
    }

    private MaterialAboutCard createAppDescriptionCard() {
        return new MaterialAboutCard.Builder()
                .addItem(new MaterialAboutActionItem.Builder()
                        .showIcon(false)
                        .text(createAppDescriptionText())
                        .build())
                .build();
    }

    private MaterialAboutCard createAppFeedbackCard(Context context) {
        return new MaterialAboutCard.Builder()
                .title(R.string.about_feedback_title)
                .addItem(ConvenienceBuilder.createWebsiteActionItem(context,
                        getResources().getDrawable(R.drawable.ic_comment_black_24dp),
                        getString(R.string.about_give_feedback),
                        true,
                        Uri.parse(getString(R.string.about_feedback_url))))
                .addItem(ConvenienceBuilder.createRateActionItem(context,
                        getResources().getDrawable(R.drawable.ic_star_black_24dp),
                        getString(R.string.about_rate),
                        null))
                .addItem(ConvenienceBuilder.createWebsiteActionItem(context,
                        getResources().getDrawable(R.drawable.ic_early_access_black_24dp),
                        getString(R.string.about_early_access),
                        true,
                        Uri.parse(getString(R.string.about_early_access_url))))
                .build();
    }

    private MaterialAboutCard createAppAuthorCard() {
        return new MaterialAboutCard.Builder()
                .title(R.string.about_author_title)
                .addItem(new MaterialAboutActionItem.Builder()
                        .text(R.string.about_author_name)
                        .icon(R.drawable.ic_person_black_24dp)
                        .build())
                .addItem(new MaterialAboutActionItem.Builder()
                        .text(R.string.about_hosted_on)
                        .icon(R.drawable.ic_code_black_24dp)
                        .build())
                .build();
    }

    @NonNull
    private String createAppDescriptionText() {
        // TODO Extract to file until word Aztec
        return "Where do you usually put a code that will be scanned soon? \uD83E\uDD14\n";
    }

}
