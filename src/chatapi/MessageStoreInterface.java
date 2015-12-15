package chatapi;

public interface MessageStoreInterface {
    public void saveMessage(String from, String to, String txt, String id, String t);
}
