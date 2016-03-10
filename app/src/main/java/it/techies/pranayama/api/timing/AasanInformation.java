package it.techies.pranayama.api.timing;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by jdtechies on 03/12/2015.
 */
public class AasanInformation implements Parcelable {

    private int currentAasanIndex;
    private int currentSetIndex;
    private ArrayList<AasanTime> aasanTimes;

    public AasanInformation(int currentAasanIndex, int currentSetIndex, ArrayList<AasanTime> aasanTimes)
    {
        this.currentAasanIndex = currentAasanIndex;
        this.currentSetIndex = currentSetIndex;
        this.aasanTimes = aasanTimes;
    }

    public int getCurrentAasanIndex()
    {
        return currentAasanIndex;
    }

    public void setCurrentAasanIndex(int currentAasanIndex)
    {
        this.currentAasanIndex = currentAasanIndex;
    }

    public int getCurrentSetIndex()
    {
        return currentSetIndex;
    }

    public void setCurrentSetIndex(int currentSetIndex)
    {
        this.currentSetIndex = currentSetIndex;
    }

    public ArrayList<AasanTime> getAasanTimes()
    {
        return aasanTimes;
    }

    public void setAasanTimes(ArrayList<AasanTime> aasanTimes)
    {
        this.aasanTimes = aasanTimes;
    }

    // ******************************* Parcelable ********************************************* //

    protected AasanInformation(Parcel in) {
        currentAasanIndex = in.readInt();
        currentSetIndex = in.readInt();
        if (in.readByte() == 0x01) {
            aasanTimes = new ArrayList<AasanTime>();
            in.readList(aasanTimes, AasanTime.class.getClassLoader());
        } else {
            aasanTimes = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(currentAasanIndex);
        dest.writeInt(currentSetIndex);
        if (aasanTimes == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(aasanTimes);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<AasanInformation> CREATOR = new Parcelable.Creator<AasanInformation>() {
        @Override
        public AasanInformation createFromParcel(Parcel in) {
            return new AasanInformation(in);
        }

        @Override
        public AasanInformation[] newArray(int size) {
            return new AasanInformation[size];
        }
    };

    // ******************************* Parcelable ********************************************* //
}