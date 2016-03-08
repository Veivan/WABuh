package whatstest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;

import chatapi.WhatsProt;

public class WhatsTest {

	public static void main(String[] args) {
		String nickname = "chipok";
		String sender = "79250069542"; // Mobile number with country code (but
										// without + or 00)
		String password = "+01ILzqNnN36tm5+1OvxAc3WkoM=";// v2 password
		String target = "79165753623";// Mobile number to send the message to

		try {
			WhatsProt wa = new WhatsProt(sender, nickname, false, false, "");

			// event bindings 
			WhatsProt.NullDelegate wa_OnConnectSuccess = new WhatsProt.NullDelegate(){
				@Override
				public void fire() {
					System.out.println("Connected!");				
				}
			};
			
			WhatsProt.ExceptionDelegate wa_OnConnectFailed = new WhatsProt.ExceptionDelegate(){
				@Override
				public void fire(Exception ex) {
					System.out.println(ex.toString());					
				}
			};
			
			WhatsProt.LoginSuccessDelegate wa_OnLoginSuccess = new WhatsProt.LoginSuccessDelegate() {
				@Override
				public void fire(String phoneNumber, byte[] data) {
					// Console.WriteLine("Login success. Next password:");
					String sdata = Base64.getEncoder().encodeToString(data);
					System.out.println(sdata);

					/*
					 * try { File.WriteAllText(getDatFileName(phoneNumber),
					 * sdata); } catch (Exception) { }
					 */
				}
			};
			
			wa.OnConnectSuccess = wa_OnConnectSuccess;
			wa.OnConnectFailed = wa_OnConnectFailed;
			wa.OnLoginSuccess = wa_OnLoginSuccess;

			wa.Connect();
			wa.loginWithPassword(password); // Login

/*			wa.sendGetPrivacyBlockedList();
			wa.sendGetClientConfig();
			wa.sendGetServerProperties();
			wa.sendGetGroups(); // Get groups (participating)
			wa.sendGetBroadcastLists(); // Get broadcasts lists

			ArrayList<String> numbers = new ArrayList<String>();
			numbers.add(target);
			wa.sendSync(numbers, null, 0); // Sync all contacts. 0 - first
											// login, 1 - others logins
*/
			
			/*
			 * for (All contacts) { $w->sendPresenceSubscription(contact); //
			 * subscribe to the user}
			 * 
			 * $w->sendGetStatuses(All contacts);
			 * 
			 * for (All contacts) { $w->sendGetProfilePicture(contact); //
			 * preview profile picture of every contact }
			 */
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
