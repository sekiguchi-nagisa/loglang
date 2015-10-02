[[

_ = [ \t]*
NL = [ \t\r\n]*
Hour : int = [0-2][1-9]
Minute  : int = [0-5][0-9]
Second : int = [0-5][0-9]
Str : string = [_a-zA-Z][_a-zA-Z0-9]*
Num : int = '0' / [1-9][0-9]*

]]

%
$h : Hour ':' $m : Minute ':' $s : Second _
%

$key : Str _ '=' _ $value : Num NL {
    print 34
}

$b : Str NL {

}
