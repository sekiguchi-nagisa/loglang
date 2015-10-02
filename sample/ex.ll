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
    state a = 345
    print 34
    print a
}

$b : Str NL {
    var a = true
    print a
    print 'hello'
    print "world"
}
