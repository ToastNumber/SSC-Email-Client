SSC Email Client
================
Features
--------
1. Getting subjects of all emails from inbox and display them. The user can select a subject to display the email content (only text/plain type).
2. Displaying and setting email flags appropriately. For example, if an email has been read (Flags.Flag.SEEN), the email client can display the flag appropriately.
3. A simple editor for inputting email address (including cc), email body and subject. The user can also select files as attachments in the editor.
4. Sending emails with attachments.
Searching email that contain the specified string in the header or body of the message.
5. A simple rule-based email filter to set custom flags to emails based on some keywords set by the user. For example, the user can set the flag of an email as ‘spam’ if its body contains “lucky winner”. The email client can display these custom flags.
6. A Graphic User Interface (GUI) for all the features listed above.

Limitations
-----------
Currently, only emails with `TEXT/PLAIN` or `MULTIPART` as the content type can be displayed. For example, `TEXT/HTML` cannot be displayed.

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
Throughout my code I use the variable identifier `svar`, which is the Norweigan word for `answer`. For example, if I am finding the sum of the elements of an array, I will usually do something like the following:

```java
public static int sum(int[] arr) {
  int svar = 0;
  for (int i = 0; i < arr.length; ++i) {
    svar += arr[i];
  }

  return svar;
}
```

Attachments
-----------
The firewall may block certain files from being attached, e.g. `.jar` files.