# What is this?

This is a minimum reproducible example (MRE) of media buttons not working in Android while the
activity is not in the foreground.

# Steps to reproduce

1. Run the app as-is.
2. Plug a headset/headphones to the device. Press its Play/Pause button.
3. See how the button label switches from "Play" to "Pause" and back to "Play".
4. Now pull down the notification drawer (such that it blocks the activity's visibility, at least
   partially).
5. Press the notification button to play/pause the player. See that it's working.
6. Try pressing again the headphones' Play/Pause drawer (with the notification drawer pulled down)
7. You can see that neither the notification button nor the activity button gets updated while
   the notification drawer is pulled down (while the activity's visibility is blocked)
8. Similarly, if you minimize the activity by pressing the Home button, the media buttons won't
   work.