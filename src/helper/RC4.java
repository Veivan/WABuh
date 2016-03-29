package helper;

public class RC4 {

	private int i = 0;
	private int j = 0;
	private int[] s = new int[256];

	public RC4(byte[] key, int drop)
    {
		StringBuffer buff = new StringBuffer();

		int j = 0; 
		for (int i = 0; i < this.s.length; i++)
            s[i] = i;
		for (int i = 0; i < this.s.length; i++)
		{
          	j = (j + key[i % key.length] + s[i]) & 0xff;         	
            Swap(i, j);         
		}

		/*        while (this.i < 0x100)
        {
            this.j = ((this.j + key[this.i % key.length]) + this.s[this.i]) & 0xff;
            Swap(this.i, this.j);
            this.i++;
        } */


/* TODO kkk debug
  		for (int i = 0; i < this.s.length; i++)
		{
    		buff.append(s[i] + " ");
		}
		System.out.println(buff.toString());
*/       
        this.i = this.j = 0;
        this.Cipher(new byte[drop]);

        /* TODO kkk debug
        buff.setLength(0);       
		for (int i = 0; i < this.s.length; i++)
		{
    		buff.append(s[i] + " ");
		}
		System.out.println(buff.toString());
        System.out.println(""+this.i + " " + this.j);
*/
        }

	public byte[] Cipher(byte[] data) {
		return this.Cipher(data, 0, data.length);
	}

	public byte[] Cipher(byte[] data, int offset, int length)
    {
        int index = offset;
        for (int i = length; i > 0; i--)
        {
            this.i = (this.i + 1) & 0xff;
            this.j = (this.j + this.s[this.i]) & 0xff;
            Swap(this.i, this.j);
            
            int d = data[index];
            data[index] = (byte)(d ^ this.s[(this.s[this.i] + this.s[this.j]) & 0xff]);
            
//            $out[$offset] = chr($d ^ $this->s[($this->s[$this->i] + $this->s[$this->j]) & 0xff]);
            index++;
            
        }
        return data;
    }

	private void Swap(int i, int j) {
		int num = this.s[i];
		this.s[i] = this.s[j];
		this.s[j] = num;
	}
}
