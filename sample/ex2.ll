// comment

[[

Month : enum
    = 'Jan' / 'Feb' / 'Mar' / 'Apr' / 'May' / 'Jun'
    / 'Jul' / 'Aug' / 'Sep' / 'Oct' / 'Nov' / 'Dec'

Day : int
    = [0-3][0-9]

Num : int
    = [0-9][0-9]

Time
    = hour : Num ':' minute : Num ':' second : Num

Host : string
    = (!' ' . )+

Process : string
    = (!' ' . )+

Pid : int
    = [0-9]+


Data
    = (!('\n' (MONTH / !. )) . )*


]]


m : Month ' ' d : Day ' ' t : Time ' ' h : Host ' ' p : Process '[' pid : Pid ']' ':'
(json : Json {

}
 / xml : Xml {
    if(month == Jan) { }
}
)




// json
