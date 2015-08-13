// this is a comment

[[

_ = [ \t\r\n]*
Expr = _ Sum _
Sum = Mul {@ _ ('+' #Add / '-' #Sub) _ @Mul}*
Mul = Num {@ _ ('*' #Mul / '/' #Div) _ @Num}*
Num = { '0' / [1-9][0-9]* #Int }
    / '(' _ Expr _ ')'

Time = _ { [0-2][1-9] ':' [0-5][0-9] ':' [0-5][0-9] #Time } _

]]




case Time {
    state b = true
}

case Expr {
    state a = 23
    true; 12; 3.14
}
