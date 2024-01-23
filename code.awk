BEGIN {
    print "BEGIN: Starting to test if statements: ";
    greaterThanTen = 0;
    equalToTen = 0;
    lessThanTen = 0;
}

{
    if ($2 > 10) {
        greaterThanTen++;
    } else if ($2 == 10) {
        equalToTen++;
    } else if ($2 < 10) {
        lessThanTen++;
    }
}

END {
    print " Values greater than 10:", greaterThanTen;
    print " Values equal to 10:", equalToTen;
    print " Values less than 10:", lessThanTen;
}