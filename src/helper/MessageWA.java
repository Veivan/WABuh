package helper;

public class MessageWA {
	private String from; 
	private String to;
	private String message; 
	private String id; 
	private String t; 

    public MessageWA(String from, String to, String message, String id, String t)
    {
        this.from = from;
        this.to = to;
        this.message = message;
        this.id = id;
        this.t = t; 
    }

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	public String getMessage() {
		return message;
	}

	public String getId() {
		return id;
	}

	public String getTime() {
		return t;
	}

}
