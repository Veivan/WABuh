package base;

public class WhatsEventBase extends ApiBase {

	// ////////////////////////////////////////////////////
	public interface ExceptionDelegate {
		void fire(Exception ex);
	}

	public ExceptionDelegate OnDisconnect;

	protected void fireOnDisconnect(Exception ex) {
		if (this.OnDisconnect != null) {
			this.OnDisconnect.fire(ex);
		}
	}

	public ExceptionDelegate OnConnectFailed;

	protected void fireOnConnectFailed(Exception ex) {
		if (this.OnConnectFailed != null) {
			this.OnConnectFailed.fire(ex);
		}
	}

	// ////////////////////////////////////////////////////
	public interface NullDelegate {
		void fire();
	}

	public NullDelegate OnConnectSuccess;

	protected void fireOnConnectSuccess() {
		if (this.OnConnectSuccess != null) {
			this.OnConnectSuccess.fire();
		}
	}

	// ////////////////////////////////////////////////////
	public interface LoginSuccessDelegate {
		void fire(String phoneNumber, byte[] data);
	}

	public LoginSuccessDelegate OnLoginSuccess;

	protected void fireOnLoginSuccess(String pn, byte[] data) {
		if (this.OnLoginSuccess != null) {
			this.OnLoginSuccess.fire(pn, data);
		}
	}

}
