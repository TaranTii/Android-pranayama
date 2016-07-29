package it.techies.pranayama.modules.launcher;

import it.techies.pranayama.infrastructure.BaseView;

/**
 * Created by jagdeep on 29/07/16.
 */
public interface LauncherView extends BaseView {

    void showSetupView(boolean show);
    void showToastMessage(String message);

}
