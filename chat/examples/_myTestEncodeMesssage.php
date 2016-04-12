<?php
require_once __DIR__.'/../src/keystream.class.php';
require_once __DIR__.'/../src/Constants.php';
require_once __DIR__.'/../src/protocol.class.php';
require_once __DIR__.'/../src/BinTreeNodeWriter.php';

require_once __DIR__.'/../src/tokenmap.class.php';

$password = ' +01ILzqNnN36tm5+1OvxAc3WkoM='; 
$ChallengeData0 = 'a';
$challengeFilename = __DIR__.'/nextChallenge.79250069542.dat'; 
if (is_readable($challengeFilename)) 
            $ChallengeData1 = file_get_contents($challengeFilename); 
$ChallengeData2= hex2bin("f1288b84cfcadabb72d9de9503ad0471591a89bd");
$ChallengeData = hex2bin("f673553e6eca565fab2c13f5e476ce44daa135c3");
//	$key = 'a';
//	$outputKey = new KeyStream($key, 'a');

      $keys = KeyStream::GenerateKeys(base64_decode($password), $ChallengeData);

PrintHex($ChallengeData);
	echo "$ChallengeData \n";


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
//	file_put_contents(__DIR__.'/log.txt', $return."\n", FILE_APPEND);
}
      $inputKey = new KeyStream($keys[2], $keys[3]);
      $outputKey = new KeyStream($keys[0], $keys[1]);


/*      $array = "\0\0\0\0".$this->phoneNumber.$this->parent->getChallengeData().''.time().'000'
.hex2bin('00').'000'.hex2bin('00')
       .Constants::OS_VERSION.hex2bin('00').Constants::MANUFACTURER.hex2bin('00').Constants::DEVICE.hex2bin('00').Constants::BUILD_VERSION;
*/

	$buffer = "\0\0\0\0"."79250069542";

/*	for($i = 0; $i < count($ChallengeData); $i++) {
	        $buffer .=sprintf('%02.x', $ChallengeData[$i]);
    	}  */

        $buffer .= $ChallengeData;
	
	//uffer .= ''."1460340975"; // time
	$buffer .= ''."1460432318"; // time

	$buffer .= "000";
	$buffer .= hex2bin('00').'000'.hex2bin('00')
	.Constants::OS_VERSION.hex2bin('00').Constants::MANUFACTURER.hex2bin('00').Constants::DEVICE.hex2bin('00').Constants::BUILD_VERSION;

	$return = CustGetHex($buffer);
	file_put_contents(__DIR__.'/log.txt', $return."\n", FILE_APPEND);

	$out = $outputKey->EncodeMessage($buffer, 0, 4,  strlen($buffer) - 4);
	//echo "$out  \n";

	$return = CustGetHex($out);
	file_put_contents(__DIR__.'/log.txt', $return."\n", FILE_APPEND);
	echo $return;

	$node =  new ProtocolNode('response', null, null, $out);
       	$writer = new BinTreeNodeWriter(); 
	$encdata =  $writer->write($node, true);
	$return = CustGetHex($encdata);
	file_put_contents(__DIR__.'/log.txt', $return."\n", FILE_APPEND);
	echo $return;

?>