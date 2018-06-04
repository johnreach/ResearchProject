# Java Lamba Analysis Research Paper

*Cade Mcdougal, Brenda Staufer, John Reach, and Donghoon Kim, ”The Evolution of Java Involving Lambda”, CCSC:Mid-South 2017, Fifteenth Annual Consortium for Computing Sciences in Colleges Mid-South Conference In CooperationWith ACM/SIGCSE, Batesville, AR, April 2017*

We analyzed 6 large open source Java projects that were active during the release of Java 8 to determine how quickly lambda functions were adopted by developers. 

The analysis was done in two parts. First, we employed software written by our professor to find occurrences of lambda functions and record details such as the date that they were committed into a MySQL database. After we completed this, we hypothesized that lambda functions were being used for testing and were not making to it the commits. To test this, we wrote a simple bash script to scan the the entire commit details including the staging area to determine if there were more occurrences there than were in the final commit.
