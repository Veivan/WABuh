package base;

import java.util.Date;
import java.util.HashMap;

import chatapi.ProtocolNode;

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

	// ////////////////////////////////////////////////////
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

	// ////////////////////////////////////////////////////
	public interface StringDelegate {
		void fire(String data);
	}

	public StringDelegate OnLoginFailed;

	protected void fireOnLoginFailed(String data) {
		if (this.OnLoginFailed != null) {
			this.OnLoginFailed.fire(data);
		}
	}

	// TODO event////////////////////////////////////////////////////
	public interface OnGetMessageDelegate {
		void fire(ProtocolNode messageNode, String from, String id,
				String name, String message, Boolean receipt_sent);
	}

	public OnGetMessageDelegate OnGetMessage;

	protected void fireOnGetMessage(ProtocolNode messageNode, String from,
			String id, String name, String message, Boolean receipt_sent) {
		if (this.OnGetMessage != null) {
			this.OnGetMessage.fire(messageNode, from, id, name, message,
					receipt_sent);
		}
	}

	// ////////////////////////////////////////////////////
	public interface OnGetMediaDelegate {
		void fire(ProtocolNode mediaNode, String from, String id,
				String fileName, int fileSize, String url, byte[] preview,
				String name);
	}

	public OnGetMediaDelegate OnGetMessageImage;

	protected void fireOnGetMessageImage(ProtocolNode mediaNode, String from,
			String id, String fileName, int fileSize, String url,
			byte[] preview, String name) {
		if (this.OnGetMessageImage != null) {
			this.OnGetMessageImage.fire(mediaNode, from, id, fileName,
					fileSize, url, preview, name);
		}
	}

	// ////////////////////////////////////////////////////
	public OnGetMediaDelegate OnGetMessageVideo;

	protected void fireOnGetMessageVideo(ProtocolNode mediaNode, String from,
			String id, String fileName, int fileSize, String url,
			byte[] preview, String name) {
		if (this.OnGetMessageVideo != null) {
			this.OnGetMessageVideo.fire(mediaNode, from, id, fileName,
					fileSize, url, preview, name);
		}
	}

	// ////////////////////////////////////////////////////
	public OnGetMediaDelegate OnGetMessageAudio;

	protected void fireOnGetMessageAudio(ProtocolNode mediaNode, String from,
			String id, String fileName, int fileSize, String url,
			byte[] preview, String name) {
		if (this.OnGetMessageAudio != null) {
			this.OnGetMessageAudio.fire(mediaNode, from, id, fileName,
					fileSize, url, preview, name);
		}
	}

	// ////////////////////////////////////////////////////
	public interface OnGetLocationDelegate {
		void fire(ProtocolNode locationNode, String from, String id,
				double lon, double lat, String url, String name,
				byte[] preview, String User);
	}

	public OnGetLocationDelegate OnGetMessageLocation;

	protected void fireOnGetMessageLocation(ProtocolNode locationNode,
			String from, String id, double lon, double lat, String url,
			String name, byte[] preview, String User) {
		if (this.OnGetMessageLocation != null) {
			this.OnGetMessageLocation.fire(locationNode, from, id, lon, lat,
					url, name, preview, User);
		}
	}

	// ////////////////////////////////////////////////////
	public interface OnGetVcardDelegate {
		void fire(ProtocolNode vcardNode, String from, String id, String name,
				byte[] data);
	}

	public OnGetVcardDelegate OnGetMessageVcard;

	protected void fireOnGetMessageVcard(ProtocolNode vcardNode, String from,
			String id, String name, byte[] data) {
		if (this.OnGetMessageVcard != null) {
			this.OnGetMessageVcard.fire(vcardNode, from, id, name, data);
		}
	}

	// ////////////////////////////////////////////////////
	public interface OnErrorDelegate {
		void fire(String id, String from, int code, String text);
	}

	public OnErrorDelegate OnError;

	protected void fireOnError(String id, String from, int code, String text) {
		if (this.OnError != null) {
			this.OnError.fire(id, from, code, text);
		}
	}

	// ////////////////////////////////////////////////////
	public interface OnNotificationPictureDelegate {
		void fire(String type, String jid, String id);
	}

	public OnNotificationPictureDelegate OnNotificationPicture;

	protected void fireOnNotificationPicture(String type, String jid, String id) {
		if (this.OnNotificationPicture != null) {
			this.OnNotificationPicture.fire(type, jid, id);
		}
	}

	// ////////////////////////////////////////////////////
	public interface OnGetMessageReceivedDelegate {
		void fire(String from, String id);
	}

	public OnGetMessageReceivedDelegate OnGetMessageReceivedServer;

	protected void fireOnGetMessageReceivedServer(String from, String id) {
		if (this.OnGetMessageReceivedServer != null) {
			this.OnGetMessageReceivedServer.fire(from, id);
		}
	}

	// ////////////////////////////////////////////////////
	public OnGetMessageReceivedDelegate OnGetMessageReceivedClient;

	protected void fireOnGetMessageReceivedClient(String from, String id) {
		if (this.OnGetMessageReceivedClient != null) {
			this.OnGetMessageReceivedClient.fire(from, id);
		}
	}

	// ////////////////////////////////////////////////////
	public OnGetMessageReceivedDelegate OnGetMessageReadedClient;

	protected void fireOnGetMessageReadedClient(String from, String id) {
		if (this.OnGetMessageReadedClient != null) {
			this.OnGetMessageReadedClient.fire(from, id);
		}
	}

	// ////////////////////////////////////////////////////
	public interface OnGetPresenceDelegate {
		void fire(String from, String type);
	}

	public OnGetPresenceDelegate OnGetPresence;

	protected void fireOnGetPresence(String from, String type) {
		if (this.OnGetPresence != null) {
			this.OnGetPresence.fire(from, type);
		}
	}

	// ////////////////////////////////////////////////////
	public interface OnGetGroupParticipantsDelegate {
		void fire(String gjid, String[] jids);
	}

	public OnGetGroupParticipantsDelegate OnGetGroupParticipants;

	protected void fireOnGetGroupParticipants(String gjid, String[] jids) {
		if (this.OnGetGroupParticipants != null) {
			this.OnGetGroupParticipants.fire(gjid, jids);
		}
	}

	// ////////////////////////////////////////////////////
	public interface OnGetLastSeenDelegate {
		void fire(String from, Date lastSeen);
	}

	public OnGetLastSeenDelegate OnGetLastSeen;

	protected void fireOnGetLastSeen(String from, Date lastSeen) {
		if (this.OnGetLastSeen != null) {
			this.OnGetLastSeen.fire(from, lastSeen);
		}
	}

	// ////////////////////////////////////////////////////
	public interface OnGetChatStateDelegate {
		void fire(String from);
	}

	public OnGetChatStateDelegate OnGetTyping;

	protected void fireOnGetTyping(String from) {
		if (this.OnGetTyping != null) {
			this.OnGetTyping.fire(from);
		}
	}

	// ////////////////////////////////////////////////////
	public OnGetChatStateDelegate OnGetPaused;

	protected void fireOnGetPaused(String from) {
		if (this.OnGetPaused != null) {
			this.OnGetPaused.fire(from);
		}
	}

	// ////////////////////////////////////////////////////
	public interface OnGetPictureDelegate {
		void fire(String from, String id, byte[] data);
	}

	public OnGetPictureDelegate OnGetPhoto;

	protected void fireOnGetPhoto(String from, String id, byte[] data) {
		if (this.OnGetPhoto != null) {
			this.OnGetPhoto.fire(from, id, data);
		}
	}

	// ////////////////////////////////////////////////////
	public OnGetPictureDelegate OnGetPhotoPreview;

	protected void fireOnGetPhotoPreview(String from, String id, byte[] data) {
		if (this.OnGetPhotoPreview != null) {
			this.OnGetPhotoPreview.fire(from, id, data);
		}
	}

	/*
	 * / TODO kkk WaGroupInfo
	 * //////////////////////////////////////////////////// public interface
	 * OnGetGroupsDelegate { void fire(String from, String id, byte[] data); }
	 * public OnGetGroupsDelegate OnGetGroups; protected void
	 * fireOnGetGroups(WaGroupInfo[] groups) { if (this.OnGetGroups != null) {
	 * this.OnGetGroups.fire(groups); } }
	 */

	// ////////////////////////////////////////////////////
	public interface OnContactNameDelegate {
		void fire(String from, String contactName);
	}

	public OnContactNameDelegate OnGetContactName;

	protected void fireOnGetContactName(String from, String contactName) {
		if (this.OnGetContactName != null) {
			this.OnGetContactName.fire(from, contactName);
		}
	}

	// ////////////////////////////////////////////////////
	public interface OnGetStatusDelegate {
		void fire(String from, String type, String name, String status);
	}

	public OnGetStatusDelegate OnGetStatus;

	protected void fireOnGetStatus(String from, String type, String name,
			String status) {
		if (this.OnGetStatus != null) {
			this.OnGetStatus.fire(from, type, name, status);
		}
	}

	// ////////////////////////////////////////////////////
	public interface OnGetSyncResultDelegate {
		void fire(int index, String sid, HashMap<String, String> existingUsers,
				String[] failedNumbers);
	}

	public OnGetSyncResultDelegate OnGetSyncResult;

	protected void fireOnGetSyncResult(int index, String sid,
			HashMap<String, String> existingUsers, String[] failedNumbers) {
		if (this.OnGetSyncResult != null) {
			this.OnGetSyncResult.fire(index, sid, existingUsers, failedNumbers);
		}
	}

	// ////////////////////////////////////////////////////
	public interface OnGetPrivacySettingsDelegate {
		void fire(HashMap<VisibilityCategory, VisibilitySetting> settings);
	}

	public OnGetPrivacySettingsDelegate OnGetPrivacySettings;

	protected void fireOnGetPrivacySettings(
			HashMap<VisibilityCategory, VisibilitySetting> settings) {
		if (this.OnGetPrivacySettings != null) {
			this.OnGetPrivacySettings.fire(settings);
		}
	}

	// ////////////////////////////////////////////////////
	public interface OnGetParticipantAddedDelegate {
		void fire(String gjid, String jid, Date time);
	}

	public OnGetParticipantAddedDelegate OnGetParticipantAdded;

	protected void fireOnGetParticipantAdded(String gjid, String jid, Date time) {
		if (this.OnGetParticipantAdded != null) {
			this.OnGetParticipantAdded.fire(gjid, jid, time);
		}
	}

	// ////////////////////////////////////////////////////
	public interface OnGetParticipantRemovedDelegate {
		void fire(String gjid, String jid, String author, Date time);
	}

	public OnGetParticipantRemovedDelegate OnGetParticipantRemoved;

	protected void fireOnGetParticipantRemoved(String gjid, String jid,
			String author, Date time) {
		if (this.OnGetParticipantRemoved != null) {
			this.OnGetParticipantRemoved.fire(gjid, jid, author, time);
		}
	}

	// ////////////////////////////////////////////////////
	public interface OnGetParticipantRenamedDelegate {
		void fire(String gjid, String oldJid, String newJid, Date time);
	}

	public OnGetParticipantRenamedDelegate OnGetParticipantRenamed;

	protected void fireOnGetParticipantRenamed(String gjid, String oldJid,
			String newJid, Date time) {
		if (this.OnGetParticipantRenamed != null) {
			this.OnGetParticipantRenamed.fire(gjid, oldJid, newJid, time);
		}
	}

	// ////////////////////////////////////////////////////
	public interface OnGetGroupSubjectDelegate {
		void fire(String gjid, String jid, String username, String subject,
				Date time);
	}

	public OnGetGroupSubjectDelegate OnGetGroupSubject;

	protected void fireOnGetGroupSubject(String gjid, String jid,
			String username, String subject, Date time) {
		if (this.OnGetGroupSubject != null) {
			this.OnGetGroupSubject.fire(gjid, jid, username, subject, time);
		}
	}
}
