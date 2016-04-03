<?php
require_once __DIR__.'/../src/keystream.class.php';

$password = '+01ILzqNnN36tm5+1OvxAc3WkoM=';
//$password = 'ab';

$enc  = base64_decode($password);
	$return = "";
	for($i = 0; $i < strlen($enc); $i++) {
	        $return .= ' '.bin2hex(substr($enc, $i, 1));
    	}
	echo "$return \n";
echo "$enc \n";

//$challengeData = array(0xf7, 0xe2, 0x86, 0xd2, 0x1c, 0xda, 0x51, 0x2c, 0xef, 0x9b, 0x65, 0xb8, 0xd2, 0x69, 0x20, 0xf9, 0x3c, 0x5d, 0x64, 0x87);
$challengeData = array(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
$challengeData = 'b';

//file_put_contents(__DIR__.'/filename.txt', print_r(mb_list_encodings(), true));

//      $keys = KeyStream::GenerateKeys($password, $challengeData);
//      $keys = KeyStream::GenerateKeys(base64_decode($password), $challengeData);

$enc = substr($enc, 0, 1);
//$enc = "ыMH/:ЌњЭъ¶n~ФлсНЦ’ѓ"; 
//$enc = "ы";
//echo "$enc \n";
//echo mb_detect_encoding( $enc, "UUENCODE", true );
echo ord($enc) + "  \n";

/* 
echo mb_detect_encoding($enc,
"UUENCODE" ) + "\n";
echo implode(", ", mb_detect_order()) + "\n";
echo mb_internal_encoding(); */

echo "$enc \n";
      $keys = KeyStream::GenerateKeys($enc, $challengeData);

	$return = "";
	for($i = 0; $i < strlen($keys[0]); $i++) {
	        $return .= ' '.bin2hex(substr($keys[0], $i, 1));
    	}
	echo "$return \n";
	$return = "";
	for($i = 0; $i < strlen($keys[1]); $i++) {
	        $return .= ' '.bin2hex(substr($keys[1], $i, 1));
    	}
	echo "$return \n";
	$return = "";
	for($i = 0; $i < strlen($keys[2]); $i++) {
	        $return .= ' '.bin2hex(substr($keys[2], $i, 1));
    	}
	echo "$return \n";
	$return = "";
	for($i = 0; $i < strlen($keys[3]); $i++) {
	        $return .= ' '.bin2hex(substr($keys[3], $i, 1));
    	}
	echo "$return \n";
?>