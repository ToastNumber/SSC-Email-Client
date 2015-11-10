SSC Email Client
================
`filter-rules.txt`
------------------
This file stores the current filtering/flagging rules for the client. For example, the file might store
```
spam: lucky winner, x factor
uni: university of birmingham
```
which would indicate that emails containing `lucky winner` or `x factor` would be flagged as `spam`, and emails containing `university of birmingham` would be flagged as `uni`.

Naming Convention
-----------------
Throughout my code I use the variable identifier `svar`, which is the Norweigan word for `answer`. For example, if I am finding the sum of the elements of an array, I will usually do something like:
```java
public static int sum(int[] arr) {
  int svar = 0;
  for (int i = 0; i < arr.length; ++i) {
    svar += arr[i];
  }

  return svar;
}
```
