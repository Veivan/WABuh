package helper;

public class AccountInfo {
	private String Status; 
	private String Kind;
	private String Creation; 
	private String Expiration; 

    public AccountInfo(String status, String kind, String creation, String expiration)
    {
        this.Status = status;
        this.Kind = kind;
        this.Creation = creation;
        this.Expiration = expiration;
    }

    public String getStatus()
    {
    	return this.Status;
    }
    
    public String getKind()
    {
    	return this.Kind;
    }
    
    public String getCreation()
    {
    	return this.Creation;
    }
    
    public String getExpiration()
    {
    	return this.Expiration;
    }
    
    public String ToString()
    {
        return String.format("Status: %s, Kind: %s, Creation: %s, Expiration: %s",
                             this.Status,
                             this.Kind,
                             this.Creation,
                             this.Expiration);
    }

}
