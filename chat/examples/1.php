<?php
require_once __DIR__.'/../src/keystream.class.php';

	$key = 'a';
	$inputKey = new KeyStream($key, 'a');

/*      $array = "\0\0\0\0".$this->phoneNumber.
$this->parent->getChallengeData().''.time().'000'.hex2bin('00').'000'.hex2bin('00')
       .Constants::OS_VERSION.hex2bin('00').Constants::MANUFACTURER.hex2bin('00').Constants::DEVICE.hex2bin('00').Constants::BUILD_VERSION;
*/
	$buffer = "\0\0\0\0"."79250069542";

	 $return = "";
	for($i = 0; $i < strlen($buffer); $i++) {
	        $return .= ' '.bin2hex(substr($buffer, $i, 1)).';';
    	}
	echo "$return \n";

	$out = $inputKey->EncodeMessage($buffer, 0, 4,  strlen($buffer) - 4);
	echo "$out  \n";

	 $return = "";
	for($i = 0; $i < strlen($out); $i++) {
	        $return .= ' '.bin2hex(substr($out, $i, 1)).';';
    	}
	echo $return;
?>