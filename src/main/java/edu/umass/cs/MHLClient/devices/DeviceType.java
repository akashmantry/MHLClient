package edu.umass.cs.MHLClient.devices;

/**
 * Defines acceptable devices and their metadata. Currently, only the
 * device name is exposed.
 *
 * @author Sean Noran
 */
public enum DeviceType {
    METAWEAR {
        @Override
        public String getName(){
            return "Metawear";
        }
    },
    MOBILE_ANDROID {
        @Override
        public String getName(){
            return "Android Phone";
        }
    },
    MOBILE_IOS {
        @Override
        public String getName(){
            return "Apple Phone";
        }
    },
    ANDROID_WEAR {
        @Override
        public String getName(){
            return "Android Wear";
        }
    };
    public String getName(){
        return name();
    }
}
