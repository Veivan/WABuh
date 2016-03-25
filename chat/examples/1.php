<?php
require_once __DIR__.'/../src/keystream.class.php';

	$inputKey = new KeyStream([1,2], [3,4]);
	$buffer = 'hello';
	$out = EncodeMessage($buffer, 0, 4, 5)
	echo $out;
?>