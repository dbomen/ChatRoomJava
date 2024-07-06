# CHATROOMJAVA

My first "BIG" personal project. This is a Server-Client chatroom application. It was originally a school project, where we had to make a command-line based chatroom with public and private messages. I have since greatly expanded on it and added:

> **Server Side**
> - Custom database (file-based), which contains:
>     - users (name, password, other logic)
>     - histories (contains all private messages sent, *50 message limit*)
> - Logic to interpret revamped Client JSON requests / other requests and new features and commands
> - Database / file managment logic

> **Client side**
> - Login GUI (Swift), *login / singup*
> - Main GUI (javafx)
>     - actively updating GUI with multithreading and other logic
>     - button and/or command usability
>     - other user-friendly abilities

The application is now in a functional and acceptable state. I will slowly add some features from the *Goals* list, but this project is not my main focus anymore, as I will start other ones.

> [!NOTE]
> If you want to know more about how to use this application, see the [MANUAL](Manual.md)\
> If you want to know more about how this applications works, see the [DEV MANUAL](devManual.md)

### Goals

- **REPO:** Finish Manual
- **REPO:** Finish DevManual
- **FUNCTIONALITY:** Add settings to the Client side and add logic on the Server side so it saves the settings into the database, making the settings Server based (saved to the account). The Server has to send Response, carrying a JSON which holds the client's settings, to the Client on login and the Client has to interpret that correctly. For the first part of this functionality, only make it so the Client can have its GUI color saved. Will add other things later (ideas: font family, font size, ...)
- **REPO:** Revamp the project into Maven / Gradle, not sure yet