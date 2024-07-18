# RELEASES

This section explains the release process for this application.

## EXPLAINED

The release numbers follow a specific logic. Each release is represented by X.Y.Z, where X, Y, and Z fall within specific ranges:
- X: 1
- Y: 0 - 99 (2 new features)
- Z: 0 - 9 (1 new feature)

The next release will increment the Z value by 1, i.e., X.Y.(Z + 1). When Z reaches 9, the next increment will reset Z to 0 and increment Y by 1, i.e., X.(Y + 1).0. In this case the release will contain 2 new features. Work on new features and fixes is done on a feature branch before being merged into the main branch, where the release is made official.

### RELEASE BLUEPRINT

Each release includes:

- New functionality/functionality
- Other minor changes or changes to existing functionalities
- Quality of Life (QOL) improvements and bug fixes
- [MANUAL](Manual.md) and [RELEASES](Releases.md) update

## HISTORY

>### 1.0.1 (CURRENT)
---

# RELEASE 1.0.1

**RELEASE 1.0.1 IS HERE!** This is the 2nd release of my ChatRoomJava application.

- [NEW FEATURES](#new-features)
  - [FRIENDS ARE HERE!](#friends-are-here)
  - [NEW COLOR!](#new-color)
- [QOL AND BUG FIXES](#qol-and-bug-fixes)
  - [QOL](#qol-quality-of-life)
  - [BUG FIXES](#bug-fixes)

## NEW FEATURES:

### FRIENDS ARE HERE!

**Big new feature!** We are excited to introduce the ability to add friends in our application. You can now send friend requests and, if accepted, you'll be friends! Your friends will be easily identifiable by a special icon next to their names in the online user list. This friend functionality is one of the biggest features we've introduced and it's now fully functional!

![alt text](/Manuals/media/actions6.png)

### NEW COLOR!

**Almondy orange!** Part of the new Set of colors called `LIGHTER`. The first 5 are from the Set `LIGHT`.

![alt text](/src/Client/media/colors/color6.png)

## QOL AND BUG FIXES

### QOL (Quality of Life)

- Added a back button on the login screen. Previously, you had to reset the client-side application to go back.
- Added a big (5px) red seperator on login, visually separating login (reminder/private offline) messages for better clarity.

### BUG FIXES

- Fixed a private message command bug in the private message command that allowed users to send private messages to themselves using the "@" command.
- Fixed an offline message bug where, if the receiver's name was a substring of the sender's name, the receiver's name would be displayed incorrectly upon the sender's login. For example, if "domen2" sent a message to "domen" while "domen" was offline, "domen" would see "2domen" as the sender upon logging in.
- Fixed an account creation bug that allowed users to create accounts with illegal names (e.g., "PUBLIC" and ".gitkeep").
- Fixed command bugs that caused the client-side application to crash if commands were not used perfectly (e.g., "@", "@ ", "@ /", "@name", "@name "). Now, improper command usage simply clears the input field with no other effect.
- Fixed an online user list bug that allowed users to open a context menu on themselves, the "PUBLIC" item, or a null item, particularly when another user was leaving the application. This bug could be exploited to send friend requests or perform other actions on oneself.
- Other fixes / anti exploitation barrires

---

For detailed instructions on using the application, refer to the [MANUAL](/Manuals/Manual.md)\
1.0.1 video: [link](https://www.youtube.com/watch?v=Am_PUWN49Yo)

---
---

>### 1.0.0
---

I am excited to announce the first release of this application, which is now fully functional and stable.

Functionalities:
- **Account Creation**
- **Public Messages:**
- **Private Messages**
- **Message History Save File:**
  - View your private message history with any user.
  - Access messages sent to you while you were offline upon your next login.
- **Customizable GUI, colors:**
  - Buttery Yellow
  - Twilight Blue
  - Pearly Red
  - Hint Of Green
  - Mercury Purple

Other:
- Repository initialization
  - [README](/README.md)
  - [MANUAL](Manual.md)
  - [RELEASES](Releases.md)

For detailed instructions on using the application, refer to the [MANUAL](Manual.md)\
1.0.0 video: [link](https://youtu.be/JDjVa-9h8oU)

---
