#!/bin/bash
rm -f log*.txt
ssh -i /home/004/j/jx/jxl130131/.ssh/id_rsa jxl130131@net01.utdallas.edu '/usr/local/bin/java -Xmx1024m  -classpath /home/004/j/jx/jxl130131/CE6378_Project2/bin edu.utdallas.ce6378.project2.Main 0 >& log0.txt' &
ssh -i /home/004/j/jx/jxl130131/.ssh/id_rsa jxl130131@net02.utdallas.edu '/usr/local/bin/java -Xmx1024m  -classpath /home/004/j/jx/jxl130131/CE6378_Project2/bin edu.utdallas.ce6378.project2.Main 1 >& log1.txt' &
ssh -i /home/004/j/jx/jxl130131/.ssh/id_rsa jxl130131@net03.utdallas.edu '/usr/local/bin/java -Xmx1024m  -classpath /home/004/j/jx/jxl130131/CE6378_Project2/bin edu.utdallas.ce6378.project2.Main 2 >& log2.txt' &
ssh -i /home/004/j/jx/jxl130131/.ssh/id_rsa jxl130131@net04.utdallas.edu '/usr/local/bin/java -Xmx1024m  -classpath /home/004/j/jx/jxl130131/CE6378_Project2/bin edu.utdallas.ce6378.project2.Main 3 >& log3.txt' &
ssh -i /home/004/j/jx/jxl130131/.ssh/id_rsa jxl130131@net05.utdallas.edu '/usr/local/bin/java -Xmx1024m  -classpath /home/004/j/jx/jxl130131/CE6378_Project2/bin edu.utdallas.ce6378.project2.Main 4 >& log4.txt' &
ssh -i /home/004/j/jx/jxl130131/.ssh/id_rsa jxl130131@net06.utdallas.edu '/usr/local/bin/java -Xmx1024m  -classpath /home/004/j/jx/jxl130131/CE6378_Project2/bin edu.utdallas.ce6378.project2.Main 5 >& log5.txt' &
ssh -i /home/004/j/jx/jxl130131/.ssh/id_rsa jxl130131@net07.utdallas.edu '/usr/local/bin/java -Xmx1024m  -classpath /home/004/j/jx/jxl130131/CE6378_Project2/bin edu.utdallas.ce6378.project2.Main 6 >& log6.txt' &
