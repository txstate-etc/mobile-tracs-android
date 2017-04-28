Tracs App - Android
========
IntegrationServer
----
The URL to the dispatch server is set here in the default constructor.

Notifications
----
Notifications are built off the TracsAppNotification interface. Notifications
received from TRACS require their own class (i.e. TracsAnnouncement.java) and
extend the abstract class TracsNotificationAbs.

Storage of notifications happens in a NotificationsBundle: an ArrayList of
TracsAppNotification objects. ArrayList was chosen because it is easy to
integrate with a ListView in Android.

To Run
----
1. Install Android Studio (must get 2.4 or later)
2. Import the entire folder
3. That should be it, just run the project
