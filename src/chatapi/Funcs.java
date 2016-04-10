package chatapi;

import java.util.ArrayList;
import java.util.Random;

public class Funcs {

    /**
     * Returns a psuedo-random number between min and max, inclusive.
     * The difference between min and max can be at most
     * <code>Integer.MAX_VALUE - 1</code>.
     *
     * @param min Minimim value
     * @param max Maximim value.  Must be greater than min.
     * @return Integer between min and max, inclusive.
     * @see java.util.Random#nextInt(int)
     * 
     * http://stackoverflow.com/questions/20389890/generating-a-random-number-between-1-and-10-java
     */
    public static int randInt(int min, int max) {

        // Usually this can be a field rather than a method variable
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
    
    public static String preprocessProfilePicture(String path)
    {
 /* TODO kkk
    	list($width, $height) = getimagesize($path);
        if ($width > $height) {
            $y = 0;
            $x = ($width - $height) / 2;
            $smallestSide = $height;
        } else {
            $x = 0;
            $y = ($height - $width) / 2;
            $smallestSide = $width;
        }

        $size = 639;
        $image = imagecreatetruecolor($size, $size);
        $img = imagecreatefromstring(file_get_contents($path));

        imagecopyresampled($image, $img, 0, 0, $x, $y, $size, $size, $smallestSide, $smallestSide);
        ob_start();
        imagejpeg($image);
        $i = ob_get_contents();
        ob_end_clean();

        imagedestroy($image);
        imagedestroy($img); */
    	
    	String i = null;
        return i;
    }

    public static ArrayList<String> ExtractNumber(String from)
    {
    	ArrayList<String> arr = new ArrayList<String>();
    	arr.add(from.replaceAll("[@s.whatsapp.net,@g.us]", ""));
    	return arr;
    } 
    
    public static String bin2hex(String intstr) {
    	 String hexStr = Integer.toString(Integer.parseInt(intstr, 2),16);
        return hexStr;
    }
    
    public static String hex2bin(String hexstr) {
    	 String binStr = Integer.toString(Integer.parseInt(hexstr, 16),2);        
    	 return binStr;
    }
 
	public static String GetHexArray(byte[] data) {
		return GetHexArray(data, true);
	}
	
	public static String GetHexArray(byte[] data, boolean isDelimited) {
		String delimiter = " ";
		if (!isDelimited) delimiter = "";
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			String hexStr = Integer.toString(data[i] & 0xFF, 16);
			if (hexStr.length() == 1)
				buff.append("0" + hexStr + delimiter);
			else
				buff.append(hexStr + delimiter);
			// buff.append(data[i] + " "); // returns int
		}
		return buff.toString();
	}


}
