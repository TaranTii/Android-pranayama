package it.techies.pranayama.modules.setup;

/**
 * Created by jagdeep on 29/07/16.
 */
public class SetupPresenterImpl implements SetupPresenter {

    private SetupView mView;

    public SetupPresenterImpl(SetupView view)
    {
        mView = view;
    }

    @Override
    public void onDestroy()
    {
        mView = null;
    }
}
