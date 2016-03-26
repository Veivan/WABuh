<?php
require_once __DIR__.'/../src/keystream.class.php';

	$inputKey = new KeyStream('0', '0');
	$buffer = "hello";

	 $return = "";
	for($i = 0; $i < strlen($buffer); $i++) {
	        $return .= ' '.bin2hex(substr($buffer, $i, 1)).';';
    	}
	echo "$return \n";

	$out = $inputKey->EncodeMessage($buffer, 0, 4, 1);
	echo "$out  \n";

	 $return = "";
	for($i = 0; $i < strlen($out); $i++) {
	        //$return .= '&#x'.bin2hex(substr($out, $i, 1)).';';
	        $return .= ' '.bin2hex(substr($out, $i, 1)).';';
    	}
	echo $return;

?>