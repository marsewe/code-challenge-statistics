# code-challenge-statistics

Start app: "  ./gradlew run".

|||||||||||||||||||||||||||||||||||||||||||||||

I could not find a way to fulfill the O(1) requirements:

i) /transactions: executes in constant time but memory consumption depends on amount of transactions in the last minute.


ii) /statistics: memory consumption depends on amount of transactions in the last minute. 
 Calculating the sum of a collection is O(n) as it has to look at
 each of its members. 
 
 
 
 Alternatives thought of:
 
 a) Calculating a snapshot from time to time and returning that one - would 
 violate the "in the last 60 seconds"-condition.
 
 b) Updating the sum every time a new transaction is added - would not help as we'd need to keep
 track of the expiring transactions to be able to deduct their values, e.g. by storing them in a DelayQueue. 
 Which would not solve it as poll() and offer() to the DelayQueue are O(log(n)).
 
