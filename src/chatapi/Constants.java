package chatapi;

public final class Constants {
    /**
     * Constants declarations.
     */
	public static final String CONNECTED_STATUS = "connected";                        // Describes the connection status with the WhatsApp server.
	public static final String DISCONNECTED_STATUS = "disconnected";                  // Describes the connection status with the WhatsApp server.
    public static final String MEDIA_FOLDER = "media";                                // The relative folder to store received media files
    public static final String PICTURES_FOLDER = "pictures";                          // The relative folder to store picture files
    public static final String DATA_FOLDER = "wadata";                                // The relative folder to store cache files.
    public static final int PORT = 443;                                               // The port of the WhatsApp server.
    public static final int TIMEOUT_SEC = 2;                                          // The timeout for the connection with the WhatsApp servers.
    public static final int TIMEOUT_USEC = 0;
    public static final String WHATSAPP_CHECK_HOST = "v.whatsapp.net/v2/exist";       // The check credentials host.
    public static final String WHATSAPP_GROUP_SERVER = "g.us";                        // The Group server hostname
    public static final String WHATSAPP_REGISTER_HOST = "v.whatsapp.net/v2/register"; // The register code host.
    public static final String WHATSAPP_REQUEST_HOST = "v.whatsapp.net/v2/code";      // The request code host.
    public static final String WHATSAPP_SERVER = "s.whatsapp.net";                    // The hostname used to login/send messages.
    public static final String WHATSAPP_DEVICE = "S40";                               // The device name.
    public static final String WHATSAPP_VER = "2.13.21";                              // The WhatsApp version.
    public static final String WHATSAPP_USER_AGENT = 
    		"WhatsApp/2.13.21 S40Version/14.26 Device/Nokia302";        // User agent used in request/registration code.
    public static final String WHATSAPP_VER_CHECKER = 
    		"https://coderus.openrepos.net/whitesoft/whatsapp_scratch"; // Check WhatsApp version
    
    private Constants() {} // make clacc abstract
}
