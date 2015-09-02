// this is a comment

[[

_ = [ \t\r\n]*

Hour : int = [0-2][1-9]
Minute  : int = [0-5][0-9]
Second : int = [0-5][0-9]
Time: time = _  $h : Hour ':' $m : Minute ':' $s : Second _

]]




case Time {
    state b = true
}


