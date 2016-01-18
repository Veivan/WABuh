package chatapi;

import helper.KeyStream;
import chatapi.*;

public class Login {

	  private String phoneNumber;
	  private String password;
	  private String challengeData;
	  private KeyStream inputKey;
	  private KeyStream outputKey;
	  private WhatsProt parent;

	  public Login(WhatsProt parent, String password)
	  {
	    this.parent = parent;
	    this.password = password;
	    this.phoneNumber = this.parent.getMyNumber();
	  }
	  
	  /**
	   * Send the nodes to the WhatsApp server to log in.
	   *
	   * @throws Exception
	   */
	  public boolean doLogin()
	  {
	      if (this.parent.isLoggedIn()) {
	          return true;
	      }

	      /**
	  	 * TODO kkk	      $this->parent->writer->resetKey();
	      $this->parent->reader->resetKey();
	      $resource = Constants::WHATSAPP_DEVICE . '-' . Constants::WHATSAPP_VER . '-' . Constants::PORT;
	      $data = $this->parent->writer->StartStream(Constants::WHATSAPP_SERVER, $resource);
*/
	      
	      ProtocolNode feat = this.createFeaturesNode();
	      ProtocolNode auth = this.createAuthNode();
/**
	  	 * TODO kkk	      $this->parent->sendData($data);
	      $this->parent->sendNode($feat);
	      $this->parent->sendNode($auth);

	      $this->parent->pollMessage();
	      $this->parent->pollMessage();
	      $this->parent->pollMessage();

	      if ($this->parent->getChallengeData() != null) {
	          $data = $this->createAuthResponseNode();
	          $this->parent->sendNode($data);
	          $this->parent->reader->setKey($this->inputKey);
	          $this->parent->writer->setKey($this->outputKey);
	          while (!$this->parent->pollMessage()) {};
	      }

	      if ($this->parent->getLoginStatus() === Constants::DISCONNECTED_STATUS) {
	          throw new LoginFailureException();
	      }

	      $this->parent->logFile('info', '{number} successfully logged in', array('number' => $this->phoneNumber));
	      $this->parent->sendAvailableForChat();
	      $this->parent->setMessageId(substr(base64_encode(mcrypt_create_iv(64, MCRYPT_DEV_URANDOM)), 0, 12));

	      if (extension_loaded('curve25519') || extension_loaded('protobuf')) {
	        if (file_exists(__DIR__ . DIRECTORY_SEPARATOR . Constants::DATA_FOLDER . DIRECTORY_SEPARATOR . "axolotl-" . $this->phoneNumber . ".db"))
	        {
	          if (empty($this->parent->getAxolotlStore()->loadPreKeys()))
	          {
	            $this->parent->sendSetPreKeys();
	            $this->parent->logFile('info', 'Sending prekeys to WA server');
	          }
	        }
	      }
*/
	      return true;
	  }
	  
	  /**
	   * Add stream features.
	   *
	   * @return ProtocolNode Return itself.
	   */
	  private ProtocolNode createFeaturesNode()
	  {
		  /**
		  	 * TODO kkk	      $readreceipts = new ProtocolNode("readreceipts", null, null, null);
	      $groupsv2     = new ProtocolNode("groups_v2", null, null, null);
	      $privacy      = new ProtocolNode("privacy", null, null, null);
	      $presencev2   = new ProtocolNode("presence", null, null, null);
	      $parent       = new ProtocolNode("stream:features", null, array($readreceipts, $groupsv2, $privacy, $presencev2), null);

	      return $parent; */
		  
		  return new ProtocolNode("presence", null, null, null);
	  }
	  

	  /**
	   * Add the authentication nodes.
	   *
	   * @return ProtocolNode Returns an authentication node.
	   */
	  private ProtocolNode createAuthNode()
	  {
		  /**
		  	 * TODO kkk
	      $data = $this->createAuthBlob();
	      $node = new ProtocolNode("auth", array(
	          'mechanism' => 'WAUTH-2',
	          'user'      => $this->phoneNumber
	      ), null, $data);

	      return $node;*/
		  
		  return new ProtocolNode("presence", null, null, null);
	  }

	  private String createAuthBlob()
	  {
	      if (this.parent.getChallengeData() != null) {
	    	  
	    	  /**
			  	 * TODO kkk
	          $key = wa_pbkdf2('sha1', base64_decode($this->password), $this->parent->getChallengeData(), 16, 20, true);
	          $this->inputKey = new KeyStream($key[2], $key[3]);
	          $this->outputKey = new KeyStream($key[0], $key[1]);
	          $this->parent->reader->setKey($this->inputKey);
	          //$this->writer->setKey($this->outputKey);
	          $array = "\0\0\0\0" . $this->phoneNumber . $this->parent->getChallengeData() . time();
	          $this->parent->setChallengeData(null);
	          return $this->outputKey->EncodeMessage($array, 0, strlen($array), false); */
	    	  
	    	  return this.outputKey.EncodeMessage("", "", "", "");
	      }
	      return null;
	  }
	  
	   /**
	   * Add the auth response to protocoltreenode.
	   *
	   * @return ProtocolNode Returns a response node.
	   */
	  protected ProtocolNode createAuthResponseNode()
	  {
	      return new ProtocolNode("response", null, null, this.authenticate());
	  }

	  /**
	   * Authenticate with the WhatsApp Server.
	   *
	   * @return string Returns binary string
	   */
	  protected byte[] authenticate()
	  {
    	  /**
		  	 * TODO kkk
	      $keys = KeyStream::GenerateKeys(base64_decode($this->password), $this->parent->getChallengeData());
	      $this->inputKey = new KeyStream($keys[2], $keys[3]);
	      $this->outputKey = new KeyStream($keys[0], $keys[1]);
	      $array = "\0\0\0\0" . $this->phoneNumber . $this->parent->getChallengeData();// . time() . Constants::WHATSAPP_USER_AGENT . " MccMnc/" . str_pad($phone["mcc"], 3, "0", STR_PAD_LEFT) . "001";
	      $response = $this->outputKey->EncodeMessage($array, 0, 4, strlen($array) - 4);
	      $this->parent->setOutputKey($this->outputKey);

	      return $response; */
		  
		  return new byte[0];
	  }

}
