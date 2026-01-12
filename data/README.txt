Name of data file represents: (League Size) - (Number of Leagues) - (Number of Clubs) - (Version)

Data files are organized as follows:

(Number of Teams)   (Number of Leagues) (Number of Clubs)	(League Size)  
----------Empty Line---------(after this clubs are presented)
(Number of teams in club 1) (Club 1 Capacity) (Teams in club 1: x1,...,y1)
.                               .               .                   
.                               .               .
.                               .               .
(Number of teams in club n) (Club n Capacity)   (Teams in club n: xn,...,yn)
----------Empty Line----------(after this leagues are presented)
(League Number: 1) (Teams in league 1: a1,....,b1)
.                   .                   
.                   .
.                   .
(League Number: m)  (Teams in league m: am,...,bm)                 
---------Empty Line--------------(after this parameter U_h,r as specified corresponding to the HAP of the corresponding league size)
(This is a binary parameter that has value 1 if the corresponding HAP states that the team assigned to it plays at home in a specific round, zero otherwise)
(Matrix with dimension: (league size)x(2*(league size - 1)))
