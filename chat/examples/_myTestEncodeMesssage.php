<?php
require_once __DIR__.'/../src/keystream.class.php';
require_once __DIR__.'/../src/Constants.php';


	$key = 'a';
	$inputKey = new KeyStream($key, 'a');

/*      $array = "\0\0\0\0".$this->phoneNumber.$this->parent->getChallengeData().''.time().'000'
.hex2bin('00').'000'.hex2bin('00')
       .Constants::OS_VERSION.hex2bin('00').Constants::MANUFACTURER.hex2bin('00').Constants::DEVICE.hex2bin('00').Constants::BUILD_VERSION;
*/

$ChallengeData = array("48", "db", "01", "bb", "01", "b1", "9e", "45", "d7", "fa", "71", "1b", 
"e0", "c3", "93", "8f", "89", "05", "a7", "58");

	$buffer = "\0\0\0\0"."79250069542";

	for($i = 0; $i < count($ChallengeData); $i++) {
	        $buffer .= hex2bin($ChallengeData[$i]);
    	}
	
	$buffer .= ''."1459222834"; // time
	$buffer .= "000";
	$buffer .= hex2bin('00').'000'.hex2bin('00')
	.Constants::OS_VERSION.hex2bin('00').Constants::MANUFACTURER.hex2bin('00').Constants::DEVICE.hex2bin('00').Constants::BUILD_VERSION;

	$return = "";
	for($i = 0; $i < strlen($buffer); $i++) {
	        $return .= ' '.bin2hex(substr($buffer, $i, 1));
    	}
	echo "$return \n";

	$out = $inputKey->EncodeMessage($buffer, 0, 4,  strlen($buffer) - 4);
	echo "$out  \n";

	 $return = "";
	for($i = 0; $i < strlen($out); $i++) {
	        $return .= ' '.bin2hex(substr($out, $i, 1));
    	}
	echo $return;
?>