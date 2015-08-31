// this is a comment

[[

_ = [ \t\r\n]*
Expr = _ Sum _
Sum = Mul ( _ ('+' / '-' ) _ Mul )*
Mul = Num ( _ ('*' / '/' ) _ Num )*
Int : int = '0' / [1-9][0-9]*
Num = Int / '(' _ Expr _ ')'

Hour : int = [0-2][1-9]
Minute  : int = [0-5][0-9]
Second : int = [0-5][0-9]
Time = _  $h : Hour ':' $m : Minute ':' $s : Second _

]]




case Time {
    state b = true
}

case Expr {
    state a = 23
    true; 12; 3.14
}
