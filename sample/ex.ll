// this is a comment

[[

_ = [ \t\r\n]*
Expr = _ Sum _
Sum = Mul ( _ ('+' / '-') _ Mul)*
Mul = Num ( _ ('*' / '/') _ Num)*
Num = '0' / [1-9][0-9]*
    / '(' _ Expr _ ')'

Time = [0-2][1-9] ':' [0-5][0-9] ':' [0-5][0-9]

]]


case Expr {
    state a = 23
    true; 12; 3.14
}

case Time {
    state b = true
}
