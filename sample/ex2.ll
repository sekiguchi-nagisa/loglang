[[

_ = [ \t]*
Hour : int = [0-2][1-9]
Minute  : int = [0-5][0-9]
Second : int = [0-5][0-9]
STR : string = [_a-zA-Z][_a-zA-Z0-9]*
NUM : int = '0' / [1-9][0-9]*
Misc1 : pair = $key : STR _ '=' _ $value : NUM
Misc2 = STR
]]

%
 _  $h : Hour ':' $m : Minute ':' $s : Second _
%

$a : Misc1 {

}

$b : Misc2 {

}
