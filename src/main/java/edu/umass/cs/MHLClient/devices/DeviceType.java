package edu.umass.cs.MHLClient.devices;

/**
 * Defines acceptable devices and their metadata. Use {@link #getName()} to
 * get the device string identifier. Use {@link #toString()} to get its
 * human-readable name.
 *
 * @author Sean Noran
 */
public enum DeviceType {
    METAWEAR {
        @Override
        public String toString(){
            return "Metawear";
        }
    },
    MOBILE_ANDROID {
        @Override
        public String toString(){
            return "Android Phone";
        }
    },
    MOBILE_IOS {
        @Override
        public String toString(){
            return "Apple Phone";
        }
    },
    ANDROID_WEAR {
        @Override
        public String toString(){
            return "Android Wear";
        }
    };
    public String getName(){
        return name();
    }
}
