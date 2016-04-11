<?php
require_once __DIR__.'/../src/keystream.class.php';
require_once __DIR__.'/../src/Constants.php';

$password = ' +01ILzqNnN36tm5+1OvxAc3WkoM='; 
//{0xe9, 0x85, 0x75, 0x43, 0x07, 0xf1, 0x6a, 0x84, 0xf2, 0x01, 0xeb, 0x3b, 0xcc, 0xe9, 0xbc, 0xfa, 0x98, 0x88, 0x5d, 0x2b};
$ChallengeData0 = array("e9", "85", "75", "43", "07", "f1", "6a", "84", "f2", "01", "eb", "3b", "cc", 
"e9", "bc", "fa", "98", "88", "5d", "2b");
$ChallengeData = array(233, 133, 117, 67, 7, 241, 106, 132, 242, 1, 235, 59, 204, 233, 188, 250, 152, 136, 93, 43);

//	$key = 'a';
//	$outputKey = new KeyStream($key, 'a');

      $keys = KeyStream::GenerateKeys(base64_decode($password), $ChallengeData);

PrintHex($keys[0]);
PrintHex($keys[1]);
PrintHex($keys[2]);
PrintHex($keys[3]);

function PrintHex($from)
{
	$return = "";
	for($i = 0; $i < strlen($from); $i++) {
	        $return .= ' '.bin2hex(substr($from, $i, 1));
    	}
	echo "$return \n";
}
      $inputKey = new KeyStream($keys[2], $keys[3]);
      $outputKey = new KeyStream($keys[0], $keys[1]);


/*      $array = "\0\0\0\0".$this->phoneNumber.$this->parent->getChallengeData().''.time().'000'
.hex2bin('00').'000'.hex2bin('00')
       .Constants::OS_VERSION.hex2bin('00').Constants::MANUFACTURER.hex2bin('00').Constants::DEVICE.hex2bin('00').Constants::BUILD_VERSION;
*/

	$buffer = "\0\0\0\0"."79250069542";

/*	for($i = 0; $i < count($ChallengeData); $i++) {
	        $buffer .= hex2bin($ChallengeData[$i]);
    	} */

        $buffer .= $ChallengeData;
	
	$buffer .= ''."1460340975"; // time
	$buffer .= "000";
	$buffer .= hex2bin('00').'000'.hex2bin('00')
	.Constants::OS_VERSION.hex2bin('00').Constants::MANUFACTURER.hex2bin('00').Constants::DEVICE.hex2bin('00').Constants::BUILD_VERSION;

	$return = CustGetHex($buffer);
	file_put_contents(__DIR__.'/log.txt', $return."\n", FILE_APPEND);

	$out = $outputKey->EncodeMessage($buffer, 0, 4,  strlen($buffer) - 4);
	//echo "$out  \n";

	 $return = "";
	$return = CustGetHex($out);
	file_put_contents(__DIR__.'/log.txt', $return."\n", FILE_APPEND);
	echo $return;
?>